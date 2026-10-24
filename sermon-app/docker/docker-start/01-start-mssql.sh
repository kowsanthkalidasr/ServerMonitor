#!/usr/bin/env bash
set -e

# Define hardcoded password in one place to avoid mismatch bugs
SA_PASSWORD="Admin@123"

# Clear healthcheck flag from previous runs
rm -f /tmp/mssql-initialized

# 1. Start SQL Server in the background
/opt/mssql/bin/sqlservr &
SQL_PID=$!

# Trap SIGTERM/SIGINT so 'docker stop' shuts down SQL Server cleanly
trap 'kill -TERM $SQL_PID' SIGTERM SIGINT

echo "Waiting for SQL Server to accept connections..."

# 2. Wait loop with a safety counter (max 60s)
MAX_RETRIES=30
COUNTER=0

until /opt/mssql-tools18/bin/sqlcmd \
        -S localhost,1435 \
        -U sa \
        -P "$SA_PASSWORD" \
        -C \
        -Q "SELECT 1" > /dev/null 2>&1
do
  COUNTER=$((COUNTER+1))
  if [ $COUNTER -ge $MAX_RETRIES ]; then
    echo "Error: SQL Server engine took too long to respond."
    exit 1
  fi
  sleep 2
done

# 3. Run initialization script once server is ready
echo "SQL Server is ready. Running initialization script..."

/opt/mssql-tools18/bin/sqlcmd \
        -S localhost,1435 \
        -U sa \
        -P "$SA_PASSWORD" \
        -C \
        -i /docker-init/01-init-mssql.sql

# Mark as initialized for healthcheck
touch /tmp/mssql-initialized
echo "Initialization complete."

# 4. Wait for the main SQL Server process to maintain container lifecycle
wait $SQL_PID