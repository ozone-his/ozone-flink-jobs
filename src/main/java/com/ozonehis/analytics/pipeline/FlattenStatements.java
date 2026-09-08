package com.ozonehis.analytics.pipeline;

import com.ozonehis.analytics.config.AnalyticsConfig;
import com.ozonehis.analytics.sql.SqlScript;
import com.ozonehis.analytics.sql.SqlScripts;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The flattening INSERTs, shared by the streaming and batch pipelines.
 *
 * <p>Both read the same queries and write to the same destination tables; they differ only in
 * where the source rows come from.
 */
final class FlattenStatements {

    private FlattenStatements() {}

    /**
     * A flattening INSERT paired with the destination table it targets. The destination name gives
     * each job a stable identity and, for streaming, namespaces its Kafka consumer groups.
     */
    record Insert(String destination, String sql) {}

    /**
     * Wraps each flattening query in an INSERT into its destination table. The query file's name is
     * the destination table name.
     */
    static List<Insert> forSinks(AnalyticsConfig config) {
        List<Insert> statements = new ArrayList<>();
        for (AnalyticsConfig.TableSink sink : config.sinks().tables()) {
            // Fail early if the sink names a catalog that was never configured.
            config.catalog(sink.catalog());
            for (SqlScript query : SqlScripts.loadAll(Path.of(sink.queries()))) {
                statements.add(new Insert(
                        query.name(),
                        "INSERT INTO " + qualify(sink.catalog(), sink.database(), query.name()) + "\n"
                                + query.content()));
            }
        }
        return List.copyOf(statements);
    }

    /** Back-quotes each identifier so names that collide with SQL keywords still resolve. */
    private static String qualify(String catalog, String database, String table) {
        return "`" + catalog + "`.`" + database + "`.`" + table + "`";
    }
}
