FROM maven:3.8.6-eclipse-temurin-8 as builder
# add pom.xml and source code
ADD ./pom.xml pom.xml
#cache dependencies
RUN mvn dependency:go-offline
ADD ./src src/
RUN mvn clean package

FROM flink:1.14.5-scala_2.12-java8
ARG JAR_VERSION=1.0.0-SNAPSHOT
RUN  wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-connector-jdbc_2.12/1.14.5/flink-connector-jdbc_2.12-1.14.5.jar -O /opt/flink/lib/flink-connector-jdbc_2.12-1.14.5.jar 
RUN  wget https://repo1.maven.org/maven2/org/apache/flink/flink-parquet_2.12/1.14.5/flink-parquet_2.12-1.14.5.jar -O /opt/flink/lib/flink-parquet_2.12-1.14.5.jar
RUN  wget https://repo.maven.apache.org/maven2/org/apache/flink/flink-shaded-hadoop-2-uber/2.8.3-10.0/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar -O /opt/flink/lib/flink-shaded-hadoop-2-uber-2.8.3-10.0.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/parquet/parquet-hadoop/1.12.2/parquet-hadoop-1.12.2.jar -O /opt/flink/lib/parquet-hadoop-1.12.2.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/parquet/parquet-common/1.12.2/parquet-common-1.12.2.jar -O /opt/flink/lib/parquet-common-1.12.2.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar -O /opt/flink/lib/httpclient-4.5.13.jar
RUN  wget https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.15/httpcore-4.4.15.jar -O /opt/flink/lib/httpcore-4.4.15.jar
RUN  wget https://repo1.maven.org/maven2/com/ecwid/consul/consul-api/1.4.5/consul-api-1.4.5.jar -O /opt/flink/lib/consul-api-1.4.5.jar
RUN  wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.9.0/gson-2.9.0.jar  -O /opt/flink/lib/gson-2.9.0.jar
COPY --from=builder target/ozone-etl-flink-${JAR_VERSION}-etl-streaming.jar /opt/flink/usrlib/streaming-etl-job.jar
COPY --from=builder target/ozone-etl-flink-${JAR_VERSION}-etl-migrations.jar /opt/flink/usrlib/ozone-etl-migrations.jar
COPY run.sh /run.sh
RUN chmod +x /run.sh
ENTRYPOINT ["/docker-entrypoint.sh"]