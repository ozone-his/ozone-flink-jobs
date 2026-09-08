package com.ozonehis.analytics.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Reads the running jobs from a session cluster's REST API.
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

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final URI overview;

    private final HttpClient http;

    public RestRunningJobs(String endpoint) {
        this.overview = URI.create("http://" + endpoint + "/jobs/overview");
        this.http =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** Reads the endpoint from the environment, falling back to the compose topology's default. */
    public static RestRunningJobs fromEnvironment() {
        String configured = System.getenv(ENDPOINT_ENV);
        return new RestRunningJobs(configured == null || configured.isBlank() ? DEFAULT_ENDPOINT : configured);
    }

    @Override
    public Set<String> names() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(overview)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Asking " + overview + " which jobs are running returned HTTP "
                    + response.statusCode() + ". Set " + ENDPOINT_ENV + " if the cluster is not at "
                    + DEFAULT_ENDPOINT + ".");
        }
        return activeNames(response.body());
    }

    /**
     * Extracts the names of non-terminal jobs from a {@code /jobs/overview} response. Package
     * private so the parsing can be asserted on without a cluster.
     */
    static Set<String> activeNames(String json) throws Exception {
        Set<String> names = new LinkedHashSet<>();
        for (JsonNode job : MAPPER.readTree(json).path("jobs")) {
            if (!TERMINAL.contains(job.path("state").asText(""))) {
                names.add(job.path("name").asText(""));
            }
        }
        names.remove("");
        return names;
    }
}
