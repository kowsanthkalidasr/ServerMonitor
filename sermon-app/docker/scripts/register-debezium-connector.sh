#!/usr/bin/env bash
set -eo pipefail

DEBEZIUM_URL="http://debezium:8083/connectors"

echo "Checking Debezium availability..."

# Wait for Kafka Connect REST API
until curl -fsS "${DEBEZIUM_URL}" >/dev/null; do
    echo "Debezium not ready. Waiting..."
    sleep 2
done

echo "Debezium is ready."



register_connector() {
  local CONNECTOR_NAME="$1"
  local CONFIG_FILE="$2"

  echo "---------------------------------------------"
  echo "checking if connector : ${CONNECTOR_NAME}... exists"

  local STATUS
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${DEBEZIUM_URL}/${CONNECTOR_NAME}")

  if [[ "$STATUS" == "200" ]]; then
    echo "Connector ${CONNECTOR_NAME} already exists."
    return 0
  fi

  local MAX_RETRIES=10
  local RETRY_COUNT=0

  until [ $RETRY_COUNT -ge $MAX_RETRIES ]; do
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${DEBEZIUM_URL}" \
                  -H "Content-Type: application/json" \
                  -d @"${CONFIG_FILE}")

    BODY=$(echo "${RESPONSE}" | head -n -1)
    STATUS=$(echo "${RESPONSE}" | tail -n 1)


    if [ "$STATUS" -eq "201" ]; then
        echo "✔ Connector '${CONNECTOR_NAME}' registered successfully! (HTTP Status: ${STATUS})"
        return 0
    fi

        RETRY_COUNT=$((${RETRY_COUNT}+1))
        echo "✖ Registration pending (HTTP ${STATUS}). Retrying... (${RETRY_COUNT}/${MAX_RETRIES})"
        sleep 5
  done

      echo "FAILED: Could not register connector '${CONNECTOR_NAME}'."
      echo "Last Response: ${BODY}"
      exit 1
    }

    # 1. Register Source Connector
    register_connector "sermondb-nodes-connector" "/connectors/mssql-debezium-cdc-nodes-connector.json"

    # 2. Register Sink Connector
    register_connector "timescaledb-nodes-sink-connector" "/connectors/timescale-sink-nodes.json"

    echo "All connectors registered and healthy."

