package com.ozonehis.analytics.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ozonehis.analytics.runtime.RunningJobs.ActiveJob;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Talks to a stand-in for the Flink REST API over real HTTP. */
class RestRunningJobsTest {

    private HttpServer server;

    private final List<String> requests = new CopyOnWriteArrayList<>();

    /** How many more times {@code /jobs/overview} reports the job as still running. */
    private final AtomicInteger stillRunningFor = new AtomicInteger();

    private int cancelStatus = 202;

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/jobs", exchange -> {
            String path = exchange.getRequestURI().toString();
            requests.add(exchange.getRequestMethod() + " " + path);
            int status = 200;
            String body;
            if (path.equals("/jobs/overview")) {
                String state = stillRunningFor.getAndDecrement() > 0 ? "CANCELLING" : "CANCELED";
                body = "{\"jobs\":[{\"jid\":\"abc\",\"name\":\"streaming-patients@00000000\",\"state\":\"" + state
                        + "\"}," + "{\"jid\":\"def\",\"name\":\"streaming-visits@11111111\",\"state\":\"RUNNING\"}]}";
            } else {
                status = cancelStatus;
                body = "{}";
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private RestRunningJobs cluster() {
        return new RestRunningJobs("127.0.0.1:" + server.getAddress().getPort());
    }

    @Test
    void listsTheActiveJobsWithTheirIds() throws Exception {
        stillRunningFor.set(1);

        assertThat(cluster().active())
                .containsExactly(
                        new ActiveJob("abc", "streaming-patients@00000000"),
                        new ActiveJob("def", "streaming-visits@11111111"));
    }

    @Test
    void cancelReturnsOnlyOnceTheJobIsNoLongerActive() throws Exception {
        stillRunningFor.set(2);

        cluster().cancel("abc");

        assertThat(requests).first().isEqualTo("PATCH /jobs/abc?mode=cancel");
        // Polled until the job had left CANCELLING, and no further.
        assertThat(requests).filteredOn(r -> r.endsWith("/jobs/overview")).hasSize(3);
    }

    @Test
    void aJobThatAlreadyEndedIsNotAnError() throws Exception {
        cancelStatus = 404;

        cluster().cancel("abc");

        assertThat(requests).containsExactly("PATCH /jobs/abc?mode=cancel", "GET /jobs/overview");
    }

    @Test
    void aRefusedCancelFailsLoudly() {
        cancelStatus = 500;

        assertThatThrownBy(() -> cluster().cancel("abc")).hasMessageContaining("HTTP 500");
    }
}
