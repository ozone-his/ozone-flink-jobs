# Ozone Analytics ETL.
#
# Builds one image per pipeline. The pipeline is fixed at build time via the PIPELINE build
# argument, which becomes the image's baked-in command:
#
#   docker build --build-arg PIPELINE=streaming-flatten -t ozone-flink-jobs .
#   docker build --build-arg PIPELINE=batch-flatten     -t ozone-flink-jobs-batch .
#   docker build --build-arg PIPELINE=file-export       -t ozone-flink-parquet-export .
#
# Each image runs in Flink application mode and serves both cluster roles: the JobManager runs the
# default command below, and TaskManagers run the same image with the command overridden to
# `taskmanager`.
ARG FLINK_VERSION=2.3.0
ARG JAVA_VERSION=17

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
# A BuildKit cache mount keeps ~/.m2 warm across builds. This replaces `dependency:go-offline`,
# which re-resolved every plugin and profile on each build and could not be made reliable.
COPY pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package

FROM flink:${FLINK_VERSION}-java${JAVA_VERSION}
ARG PIPELINE=streaming-flatten
ARG JAR_VERSION=3.0.0-SNAPSHOT

# Flink ships S3 support under opt/ but only loads it from plugins/.
RUN mkdir -p "${FLINK_HOME}/plugins/s3-fs-presto" \
    && cp "${FLINK_HOME}"/opt/flink-s3-fs-presto-*.jar "${FLINK_HOME}/plugins/s3-fs-presto/"

# usrlib is where the standalone-job entrypoint looks for the application JAR.
COPY --from=builder /build/target/flink-jobs-${JAR_VERSION}-etl.jar ${FLINK_HOME}/usrlib/analytics-etl.jar

# Baked in so the running container cannot be pointed at a different pipeline by accident.
ENV OZONE_PIPELINE=${PIPELINE}
CMD ["/bin/bash", "-c", "exec /docker-entrypoint.sh standalone-job --job-classname com.ozonehis.analytics.AnalyticsJob ${OZONE_PIPELINE}"]
