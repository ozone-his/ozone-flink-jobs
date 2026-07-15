# Ozone Analytics ETL

Flink SQL pipelines that flatten data from the Ozone HIS components into tables that are
straightforward to query and report on.

## The ETL

```
  EXTRACT                    TRANSFORM                  LOAD                    SERVE
  ───────                    ─────────                  ────                    ─────

  OpenMRS (MySQL) ─┐  Debezium   ┌─ Kafka topics ──┐                       ┌─ PostgreSQL
                   ├─ CDC ──────▶│ (debezium-json) │─▶ streaming-flatten ─▶│  (analytics)
  Odoo (Postgres) ─┘             └─────────────────┘         │             └──────┬──────
                   │                                         │                    │
                   └─ JDBC (bounded read) ─────────▶ batch-flatten ───────────────┤
                                                                                  │
                                                      file-export ◀───────────────┘
                                                           │
                                                           ▼
                                                   Parquet/CSV ─▶ MinIO ─▶ Drill / Superset
```

Three pipelines, all expressed as SQL:

|      Pipeline       |          Extracts from          |                         Purpose                         |
|---------------------|---------------------------------|---------------------------------------------------------|
| `streaming-flatten` | Debezium change events on Kafka | Keeps the analytics tables continuously up to date      |
| `batch-flatten`     | The source databases over JDBC  | Backfills in one pass, without replaying the change log |
| `file-export`       | The flattened analytics tables  | Exports Parquet/CSV for a central warehouse             |

## Architecture

The guiding split is **what** the ETL computes versus **how** Flink runs it.

```
com.ozonehis.analytics
├── AnalyticsJob        entry point; selects the pipeline named by its argument
├── config/             the pipeline definition, as validated immutable records
├── sql/                loading .sql files and rendering connector options
├── pipeline/           the three pipelines — pure SQL builders, no Flink
└── runtime/            the only code that talks to Flink
```

A `Pipeline` reads configuration and SQL files and returns statements. It never touches Flink and
never executes anything, so the SQL it generates is asserted on directly in unit tests — no
cluster, no containers. `FlinkRunner` is what executes it.

### Runtime model

Jobs run on a standalone Flink cluster in **application mode**; they do not start a cluster of
their own. All INSERT statements are submitted together as a single `StatementSet`, so Flink plans
them as one job: sinks reading the same source share one scan, and the pipeline has a single
checkpoint and recovery boundary. The trade-off is a shared failure domain — a failure in one sink
restarts the job.

### Configuration

Two files, and the split is the point:

|      File       |                      Owns                       |                             Example                              |
|-----------------|-------------------------------------------------|------------------------------------------------------------------|
| Pipeline config | Catalogs, sources, sinks — **what**             | [`development/data/config.yaml`](development/data/config.yaml)   |
| Cluster config  | Parallelism, checkpointing, state, S3 — **how** | [`development/flink/config.yaml`](development/flink/config.yaml) |

The pipeline config resolves `${VAR}` and `${VAR:-default}` against the environment, so no
credential is written to a file or baked into an image. Substitution happens after the YAML is
parsed, so a secret containing a quote or a colon cannot corrupt the document. Unknown keys are
rejected rather than ignored.

`ANALYTICS_CONFIG_FILE` locates the pipeline config (default `/etc/analytics/config.yaml`).

## Building

Requires JDK 17. Artifacts are compiled with `--release 17`, so they load on any JVM ≥ 17; Flink
itself supports Java 11/17/21 only ([21 experimental, 25 unsupported][java-compat]), and 17 is what
ships.

```bash
mvn clean verify
```

Which pipeline an image runs is fixed at build time by the `PIPELINE` build argument:

```bash
docker build --build-arg PIPELINE=streaming-flatten -t ozone-flink-jobs .
docker build --build-arg PIPELINE=batch-flatten     -t ozone-flink-jobs-batch .
docker build --build-arg PIPELINE=file-export       -t ozone-flink-parquet-export .
```

Each image serves both cluster roles: the JobManager runs the baked-in command, and TaskManagers
run the same image with the command overridden to `taskmanager`.

## Running locally

The project assumes a running Ozone HIS instance — see [ozone-docker][ozone-docker] — and the
flattening SQL and migrations from the [distro][distro]'s `analytics_config` directory.

```bash
export ANALYTICS_SOURCE_TABLES_PATH=~/ozonepro-distro/analytics_config/dsl/flattening/tables
export ANALYTICS_QUERIES_PATH=~/ozonepro-distro/analytics_config/dsl/flattening/queries
export ANALYTICS_DESTINATION_TABLES_MIGRATIONS_PATH=~/ozonepro-distro/analytics_config/liquibase/analytics

cd development
docker compose up -d
```

The Flink web UI is at <http://localhost:8081>, Prometheus metrics at <http://localhost:9250>, and
Kafka/Connect are browsable via Kowl at <http://localhost:8282>.

## Testing

The suite is deliberately hermetic: no Docker, no private artifacts, no network. The pipelines are
pure SQL builders, so they are tested by asserting on the SQL they generate.

```bash
mvn test
```

## Code formatting

```bash
mvn spotless:apply -Pspotless
```

## Gotchas

When streaming from PostgreSQL, see [consuming data produced by the Debezium Postgres
connector][dbz-postgres].

[java-compat]: https://nightlies.apache.org/flink/flink-docs-stable/docs/deployment/java_compatibility/
[ozone-docker]: https://github.com/ozone-his/ozone-docker
[distro]: https://github.com/ozone-his/ozonepro-distro
[dbz-postgres]: https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/formats/debezium/#consuming-data-produced-by-debezium-postgres-connector

