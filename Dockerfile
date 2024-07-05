FROM maven:3.8.6-eclipse-temurin-11 as builder
# add pom.xml and source code
ADD ./pom.xml pom.xml
#cache dependencies
RUN mvn dependency:go-offline
ADD ./src src/
RUN mvn clean package -DskipTests --batch-mode

FROM eclipse-temurin:11-jre
ARG JAR_VERSION=2.5.0
RUN mkdir -p /app
WORKDIR /app
COPY --from=builder target/flink-jobs-${JAR_VERSION}-etl-streaming.jar streaming-etl-job.jar
COPY streamimg-etl.sh streamimg-etl.sh
RUN chmod +x streamimg-etl.sh
ENTRYPOINT ["/app/streamimg-etl.sh"]
