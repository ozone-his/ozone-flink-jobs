FROM maven:3.8.6-eclipse-temurin-11 as builder
# add pom.xml and source code
ADD ./pom.xml pom.xml
#cache dependencies
RUN mvn dependency:go-offline
ADD ./src src/
RUN mvn clean package

FROM flink:1.17.1-scala_2.12-java11
ARG JAR_VERSION=1.0.0-SNAPSHOT
RUN  wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc/3.1.1-1.17/flink-connector-jdbc-3.1.1-1.17.jar  -O /opt/flink/lib/flink-connector-jdbc-3.1.1-1.17.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/flink/flink-parquet/1.17.1/flink-parquet-1.17.1.jar -O /opt/flink/lib/flink-parquet-1.17.1.jar
RUN  wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/1.17.1/flink-sql-connector-kafka-1.17.1.jar -O /opt/flink/lib/flink-sql-connector-kafka-1.17.1.jar
RUN  wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.8.3-10.0/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar -O /opt/flink/lib/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/parquet/parquet-hadoop/1.13.1/parquet-hadoop-1.13.1.jar -O /opt/flink/lib/parquet-hadoop-1.13.1.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/parquet/parquet-common/1.13.1/parquet-common-1.13.1.jar -O /opt/flink/lib/parquet-common-1.13.1.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar -O /opt/flink/lib/httpclient-4.5.13.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.15/httpcore-4.4.15.jar -O /opt/flink/lib/httpcore-4.4.15.jar
RUN  wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar  -O /opt/flink/lib/gson-2.9.0.jar
RUN  wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/1.17.1/flink-json-1.17.1.jar
RUN  wget -P /opt/flink/lib/ https://jdbc.postgresql.org/download/postgresql-42.6.0.jar
COPY --from=builder target/flink-jobs-${JAR_VERSION}-etl-streaming.jar /opt/flink/usrlib/streaming-etl-job.jar
COPY run.sh /run.sh
RUN chmod +x /run.sh
ENTRYPOINT ["/docker-entrypoint.sh"]