set -e
CONNECT_OPENMRS="${CONNECT_OPENMRS:-1}"
CONNECT_ODOO="${CONNECT_ODOO:-1}"

echo "Waiting for connect to be ready-----"
/opt/wait-for-it.sh $CONNECT_HOST:8083
if [[ $CONNECT_OPENMRS = "1" ]]
then
echo "Waiting for OpenMRS database to be ready-----"
/opt/wait-for-it.sh $SOURCE_DB_HOST:$SOURCE_DB_PORT
curl --fail -i -X PUT -H "Accept:application/json" -H "Content-Type:application/json" http://${CONNECT_HOST}:8083/connectors/openmrs-connector/config/ \
        -d '{
               "connector.class": "io.debezium.connector.mysql.MySqlConnector",
               "tasks.max": "1",
               "database.hostname": "${file:/kafka/config/connect-distributed.properties:mysql.hostname}",
               "database.port": "${file:/kafka/config/connect-distributed.properties:mysql.port}",
               "database.user": "${file:/kafka/config/connect-distributed.properties:mysql.username}",
               "database.password": "${file:/kafka/config/connect-distributed.properties:mysql.password}",
               "database.server.id": "${file:/kafka/config/connect-distributed.properties:mysql.server.id}",
               "database.server.name": "${file:/kafka/config/connect-distributed.properties:mysql.server.name}",
               "database.include.list": "${file:/kafka/config/connect-distributed.properties:mysql.include.list}",
               "table.exclude.list": "${file:/kafka/config/connect-distributed.properties:table.exclude.list}",
               "database.history.kafka.bootstrap.servers": "${file:/kafka/config/connect-distributed.properties:mysql.kafka.bootstrap.servers}",
               "database.history.kafka.topic": "${file:/kafka/config/connect-distributed.properties:mysql.histroy.topic}",
               "converters": "timestampConverter,boolean",
               "boolean.type": "io.debezium.connector.mysql.converters.TinyIntOneToBooleanConverter",
               "timestampConverter.type": "oryanmoshe.kafka.connect.util.TimestampConverter",
               "timestampConverter.format.time": "HH:mm:ss",
               "timestampConverter.format.date": "YYYY-MM-dd",
               "timestampConverter.format.datetime": "yyyy-MM-dd HH:mm:ss",
               "timestampConverter.debug": "false",
               "snapshot.mode": "when_needed"
     }'
fi 


if [[ $CONNECT_ODOO = "1" ]]
then
echo "Waiting for Odoo database to be ready-----"
/opt/wait-for-it.sh $ODOO_DB_HOST:$ODOO_DB_PORT
curl --fail -i -X PUT -H "Accept:application/json" -H "Content-Type:application/json" http://${CONNECT_HOST}:8083/connectors/odoo-connector/config/ \
        -d  '{
                "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
                "tasks.max": "1",
                "database.hostname": "${file:/kafka/config/connect-distributed.properties:odoo.db.hostname}",
                "database.port": "${file:/kafka/config/connect-distributed.properties:odoo.db.port}",
                "database.user": "${file:/kafka/config/connect-distributed.properties:odoo.db.username}",
                "database.password": "${file:/kafka/config/connect-distributed.properties:odoo.db.password}",
                "database.dbname" : "odoo",
                "topic.prefix": "odoo",
                "plugin.name": "pgoutput",
                "database.server.name": "odoo",
                "table.include.list": "public.(.*)",
                "converters": "timestampConverter",
                "timestampConverter.type": "oryanmoshe.kafka.connect.util.TimestampConverter",
                "timestampConverter.format.time": "HH:mm:ss",
                "timestampConverter.format.date": "YYYY-MM-dd",
                "timestampConverter.format.datetime": "yyyy-MM-dd HH:mm:ss",
                "timestampConverter.debug": "false",
                "key.converter": "org.apache.kafka.connect.json.JsonConverter",
                "key.converter.schemas.enable": "true",
                "heartbeat.interval.ms": "5000",
                "slot.name": "odoo_debezium",
                "publication.name": "odoo_publication",
                "decimal.handling.mode": "double"
    }'
fi