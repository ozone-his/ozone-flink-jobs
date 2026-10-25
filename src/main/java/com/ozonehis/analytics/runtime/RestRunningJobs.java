package com.ozonehis.analytics.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads and cancels jobs through a session cluster's REST API.
 *
 * <p>Uses the JDK HTTP client and the {@code /jobs/overview} endpoint rather than Flink's
 * {@code RestClusterClient}: the client program is not given the cluster's client-side
 * configuration, and the shipped {@code rest.address} is a bind address ({@code 0.0.0.0}) that is
 * meaningless to connect to.
 */
public final class RestRunningJobs implements RunningJobs {

    /** Names the cluster to query, as {@code host:port}. */
    public static final String ENDPOINT_ENV = "ANALYTICS_JOBMANAGER_REST";

    /** Matches the address the job submitter already passes to {@code flink run -m}. */
    public static final String DEFAULT_ENDPOINT = "jobmanager:8081";

    /**
     * States a job never leaves. Everything else, including {@code CREATED},
     * {@code INITIALIZING}, {@code RESTARTING} and {@code RECONCILING}, counts as already running:
     * a job recovering from high-availability storage passes through those on its way back, and
     * treating it as absent is what submits a second copy of it.
     */
    private static final Set<String> TERMINAL = Set.of("FINISHED", "CANCELED", "FAILED");

    /**
     * How long a cancelled job may take to stop. Stopping includes disposing of its RocksDB state,
     * which takes a while for a large table.
     */
    private static final Duration CANCEL_TIMEOUT = Duration.ofMinutes(5);

    private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String base;

    private final HttpClient http;

    public RestRunningJobs(String endpoint) {
        this.base = "http://" + endpoint;
        this.http =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** Reads the endpoint from the environment, falling back to the compose topology's default. */
    public static RestRunningJobs fromEnvironment() {
        String configured = System.getenv(ENDPOINT_ENV);
        return new RestRunningJobs(configured == null || configured.isBlank() ? DEFAULT_ENDPOINT : configured);
    }

    @Override
    public List<ActiveJob> active() throws Exception {
        HttpResponse<String> response =
                send(HttpRequest.newBuilder(URI.create(base + "/jobs/overview")).GET());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Asking " + base + "/jobs/overview which jobs are running returned HTTP "
                    + response.statusCode() + ". Set " + ENDPOINT_ENV + " if the cluster is not at "
                    + DEFAULT_ENDPOINT + ".");
        }
        return activeJobs(response.body());
    }

    @Override
    public void cancel(String id) throws Exception {
        HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(base + "/jobs/" + id + "?mode=cancel"))
                .method("PATCH", HttpRequest.BodyPublishers.noBody()));
        // 404: the job ended on its own in the meantime, which is the outcome asked for.
        if (response.statusCode() / 100 != 2 && response.statusCode() != 404) {
            throw new IllegalStateException(
                    "Cancelling job " + id + " returned HTTP " + response.statusCode() + ": " + response.body());
        }
        Instant deadline = Instant.now().plus(CANCEL_TIMEOUT);
        while (active().stream().anyMatch(job -> job.id().equals(id))) {
            if (Instant.now().isAfter(deadline)) {
                throw new IllegalStateException("Job " + id + " was asked to cancel but is still active after "
                        + CANCEL_TIMEOUT.toMinutes() + " minutes");
            }
            Thread.sleep(POLL_INTERVAL.toMillis());
        }
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return http.send(request.timeout(Duration.ofSeconds(30)).build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Extracts the non-terminal jobs from a {@code /jobs/overview} response. Package private so the
     * parsing can be asserted on without a cluster.
     */
    static List<ActiveJob> activeJobs(String json) throws Exception {
        List<ActiveJob> jobs = new ArrayList<>();
        for (JsonNode job : MAPPER.readTree(json).path("jobs")) {
            String id = job.path("jid").asText("");
            String name = job.path("name").asText("");
            if (!TERMINAL.contains(job.path("state").asText("")) && !id.isEmpty() && !name.isEmpty()) {
                jobs.add(new ActiveJob(id, name));
            }
        }
        return List.copyOf(jobs);
    }
}
