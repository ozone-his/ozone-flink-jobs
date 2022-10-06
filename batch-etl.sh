#!/bin/sh
: ${SOURCE_JDBC_URL?"Need to set SOURCE_JDBC_URL"}
: ${SOURCE_JDBC_USERNAME:?"Need to set SOURCE_JDBC_USERNAME"}
: ${SOURCE_JDBC_PASSWORD:?"Need to set SOURCE_JDBC_PASSWORD"}
: ${SINK_JDBC_URL?"Need to set SINK_JDBC_URL"}
: ${SINK_JDBC_USERNAME:?"Need to set SINK_JDBC_USERNAME"}
: ${SINK_JDBC_PASSWORD:?"Need to set SINK_JDBC_PASSWORD"}
: ${$PROPERTIES_FILE:?"Need to set $PROPERTIES_FILE"}
java -jar batch-etl.jar --properties-file $PROPERTIES_FILE --sink-url $SINK_JDBC_URL --sink-username $SINK_JDBC_USERNAME --sink-password $SINK_JDBC_PASSWORD --source-url $SOURCE_JDBC_URL --source-username $SOURCE_JDBC_USERNAME --source-password $SOURCE_JDBC_PASSWORD