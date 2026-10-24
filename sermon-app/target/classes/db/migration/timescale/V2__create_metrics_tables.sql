-- FOR PERFORMANCE

-- node_id should be mapped by an int
-- foreign key removed. spring should handle check
-- add check constraint for all REAL columns


-- create PING Metrics Table

CREATE TABLE ping_metrics
(
    time TIMESTAMPZ NOT NULL,
    node_key    INTEGER NOT NULL REFERENCES nodes(node_key),

    rtt REAL ,
    packet_loss REAL NOT NULL CHECK (packet_loss BETWEEN 0.0 AND 1.0),
    is_reachable BOOLEAN NOT NULL,

    CONSTRAINT pk_ping_metrics PRIMARY KEY (node_key,time)
);

SELECT create_hypertable(
               'ping_metrics',
                by_range('time','1 day')
      );

SELECT add_retention_policy(
       'ping_metrics',
       INTERVAL '7 days'
       );


CREATE MATERIALIZED VIEW ping_metrics_hourly
    WITH (timescaledb.continuous) AS
    SELECT time_bucket('1 hour', time) AS bucket,
            node_key,
            ROUND(AVG(is_reachable::int)::numeric,4)::real AS uptime
            ROUND()
    FROM ping_metrics
    GROUP BY bucket, node_key;





-- create CPU Metrics TABLE

CREATE TABLE cpu_metrics
(
    time            TIMESTAMPTZ NOT NULL,
    node_key        INTEGER     NOT NULL REFERENCES nodes (node_key),


    total_cpu_util  REAL        NOT NULL CHECK (total_cpu_util BETWEEN 0.0 AND 1.0),
    user_cpu_util   REAL        NOT NULL CHECK (user_cpu_util BETWEEN 0.0 AND 1.0),
    system_cpu_util REAL        NOT NULL CHECK (system_cpu_util BETWEEN 0.0 AND 1.0),
    io_wait         REAL        NOT NULL CHECK (io_wait BETWEEN 0.0 AND 1.0),

    CONSTRAINT pk_cpu_metrics PRIMARY KEY (node_key,time)
);

-- All util values are in percentages. they are to be stored as 0.00 to 1.00 only; eg: 75% is 0.75.
-- Note that the precision is 6 digits for REAL. so effective precision is up to 4 decimal points.

SELECT create_hypertable(
               'cpu_metrics',
               by_range('time', INTERVAL '1 day')
       );


SELECT add_retention_policy('cpu_metrics', INTERVAL '7 days');

CREATE MATERIALIZED VIEW cpu_metrics_hourly
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', time) AS bucket,
       node_key,

       -- Total CPU Metrics
       AVG(total_cpu_util)         AS avg_total_cpu,
       MAX(total_cpu_util)         AS max_total_cpu,
       MIN(total_cpu_util)         AS min_total_cpu,

       -- User CPU Metrics
       AVG(user_cpu_util)          AS avg_user_cpu,
       MAX(user_cpu_util)          AS max_user_cpu,
       MIN(user_cpu_util)          AS min_user_cpu,

       -- System CPU Metrics
       AVG(system_cpu_util)        AS avg_system_cpu,
       MAX(system_cpu_util)        AS max_system_cpu,
       MIN(system_cpu_util)        AS min_system_cpu,

       -- I/O Wait Metrics
       AVG(io_wait)                AS avg_io_wait,
       MAX(io_wait)                AS max_io_wait,
       MIN(io_wait)                AS min_io_wait

FROM cpu_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('cpu_metrics_hourly',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '1 hours',
                                       schedule_interval => INTERVAL '1 hour'
       );

-- Keep hourly rollups for 90 days
SELECT add_retention_policy('cpu_metrics_hourly', INTERVAL '90 days');

-- CPU daily continuous aggregate
CREATE MATERIALIZED VIEW cpu_metrics_daily
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 day', time) AS bucket,
       node_key,

       AVG(total_cpu_util)        AS avg_total_cpu,
       MAX(total_cpu_util)        AS max_total_cpu,
       MIN(total_cpu_util)        AS min_total_cpu,

       AVG(user_cpu_util)         AS avg_user_cpu,
       MAX(user_cpu_util)         AS max_user_cpu,
       MIN(user_cpu_util)         AS min_user_cpu,

       AVG(system_cpu_util)       AS avg_system_cpu,
       MAX(system_cpu_util)       AS max_system_cpu,
       MIN(system_cpu_util)       AS min_system_cpu,

       AVG(io_wait)               AS avg_io_wait,
       MAX(io_wait)               AS max_io_wait,
       MIN(io_wait)               AS min_io_wait

FROM cpu_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('cpu_metrics_daily',
                                       start_offset => INTERVAL '3 days',
                                       end_offset => INTERVAL '1 day',
                                       schedule_interval => INTERVAL '1 day'
       );

-- Keep daily rollups for 365 days
SELECT add_retention_policy('cpu_metrics_daily', INTERVAL '365 days');




-----------------------------------------------------------------------------------------



-- CREATE memory_metrics TABLE

CREATE TABLE memory_metrics
(
    time                   TIMESTAMPTZ NOT NULL,
    node_key               INTEGER     NOT NULL REFERENCES nodes (node_key),

    used_memory_bytes      BIGINT      NOT NULL,
    available_memory_bytes BIGINT      NOT NULL,
    swap_rate_kbps         REAL        NOT NULL,

    CONSTRAINT pk_memory_metrics PRIMARY KEY (node_key,time)


);

SELECT create_hypertable(
               'memory_metrics',
               by_range('time', INTERVAL '1 day')
       );


CREATE INDEX IX_memory_metrics_node_time ON memory_metrics (node_key, time DESC);



SELECT add_retention_policy('memory_metrics', INTERVAL '7 days');


-- MEMORY hourly / daily continuous aggregates
CREATE MATERIALIZED VIEW memory_metrics_hourly
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', time) AS bucket,
       node_key,

       AVG(used_memory_bytes)      AS avg_used_memory_bytes,
       MAX(used_memory_bytes)      AS max_used_memory_bytes,
       MIN(used_memory_bytes)      AS min_used_memory_bytes,

       AVG(available_memory_bytes) AS avg_available_memory_bytes,
       MAX(available_memory_bytes) AS max_available_memory_bytes,
       MIN(available_memory_bytes) AS min_available_memory_bytes,

       AVG(swap_rate_kbps)         AS avg_swap_rate_kbps,
       MAX(swap_rate_kbps)         AS max_swap_rate_kbps,
       MIN(swap_rate_kbps)         AS min_swap_rate_kbps

FROM memory_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('memory_metrics_hourly',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '1 hours',
                                       schedule_interval => INTERVAL '1 hour'
       );

SELECT add_retention_policy('memory_metrics_hourly', INTERVAL '90 days');


CREATE MATERIALIZED VIEW memory_metrics_daily
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 day', time)  AS bucket,
       node_key,

       AVG(used_memory_bytes)      AS avg_used_memory_bytes,
       MAX(used_memory_bytes)      AS max_used_memory_bytes,
       MIN(used_memory_bytes)      AS min_used_memory_bytes,

       AVG(available_memory_bytes) AS avg_available_memory_bytes,
       MAX(available_memory_bytes) AS max_available_memory_bytes,
       MIN(available_memory_bytes) AS min_available_memory_bytes,

       AVG(swap_rate_kbps)         AS avg_swap_rate_kbps,
       MAX(swap_rate_kbps)         AS max_swap_rate_kbps,
       MIN(swap_rate_kbps)         AS min_swap_rate_kbps

FROM memory_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('memory_metrics_daily',
                                       start_offset => INTERVAL '3 days',
                                       end_offset => INTERVAL '1 day',
                                       schedule_interval => INTERVAL '1 day'
       );


SELECT add_retention_policy('memory_metrics_daily', INTERVAL '365 days');





-----------------------------------------------------------------------------------------








-- CREATE disk_metrics TABLE

CREATE TABLE disk_metrics
(
    time        TIMESTAMPTZ NOT NULL,
    node_key    INTEGER     NOT NULL REFERENCES nodes (node_key),


    disk_util   REAL        NOT NULL CHECK (disk_util BETWEEN 0 AND 1),
    iops        INTEGER     NOT NULL,
    queue_depth INTEGER     NOT NULL,

    CONSTRAINT pk_disk_metrics PRIMARY KEY(node_key,time)

);

SELECT create_hypertable(
               'disk_metrics',
               by_range('time', INTERVAL '1 day')
       );


CREATE INDEX IX_disk_metrics_node_time ON disk_metrics (node_key, time DESC);


SELECT add_retention_policy('disk_metrics', INTERVAL '7 days');


-- DISK hourly / daily continuous aggregates
CREATE MATERIALIZED VIEW disk_metrics_hourly
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', time) AS bucket,
       node_key,

       AVG(disk_util)              AS avg_disk_util,
       MAX(disk_util)              AS max_disk_util,
       MIN(disk_util)              AS min_disk_util,

       AVG(iops)                   AS avg_iops,
       MAX(iops)                   AS max_iops,
       MIN(iops)                   AS min_iops,

       AVG(queue_depth)            AS avg_queue_depth,
       MAX(queue_depth)            AS max_queue_depth,
       MIN(queue_depth)            AS min_queue_depth

FROM disk_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('disk_metrics_hourly',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '1 hours',
                                       schedule_interval => INTERVAL '1 hour'
       );


SELECT add_retention_policy('disk_metrics_hourly', INTERVAL '90 days');


CREATE MATERIALIZED VIEW disk_metrics_daily
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 day', time) AS bucket,
       node_key,

       AVG(disk_util)             AS avg_disk_util,
       MAX(disk_util)             AS max_disk_util,
       MIN(disk_util)             AS min_disk_util,

       AVG(iops)                  AS avg_iops,
       MAX(iops)                  AS max_iops,
       MIN(iops)                  AS min_iops,

       AVG(queue_depth)           AS avg_queue_depth,
       MAX(queue_depth)           AS max_queue_depth,
       MIN(queue_depth)           AS min_queue_depth

FROM disk_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('disk_metrics_daily',
                                       start_offset => INTERVAL '3 days',
                                       end_offset => INTERVAL '1 day',
                                       schedule_interval => INTERVAL '1 day'
       );

SELECT add_retention_policy('disk_metrics_daily', INTERVAL '365 days');


-- CREATE network_metrics TABLE

CREATE TABLE network_metrics
(
    time             TIMESTAMPTZ NOT NULL,
    node_key         INTEGER     NOT NULL REFERENCES nodes (node_key),

    rx_bps           BIGINT      NOT NULL,
    tx_bps           BIGINT      NOT NULL,

    packet_drop_rate REAL        NOT NULL CHECK (packet_drop_rate BETWEEN 0 AND 1),

    CONSTRAINT pk_network_metrics PRIMARY KEY (node_key,time)
);

SELECT create_hypertable(
               'network_metrics',
               by_range('time', INTERVAL '1 day')
       );


SELECT add_retention_policy('network_metrics', INTERVAL '7 days');

CREATE INDEX IX_network_metrics_node_time ON network_metrics (node_key, time DESC);


-- NETWORK hourly / daily continuous aggregates
CREATE MATERIALIZED VIEW network_metrics_hourly
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', time) AS bucket,
       node_key,

       AVG(rx_bps)                 AS avg_rx_bps,
       MAX(rx_bps)                 AS max_rx_bps,
       MIN(rx_bps)                 AS min_rx_bps,

       AVG(tx_bps)                 AS avg_tx_bps,
       MAX(tx_bps)                 AS max_tx_bps,
       MIN(tx_bps)                 AS min_tx_bps,

       AVG(packet_drop_rate)       AS avg_packet_drop_rate,
       MAX(packet_drop_rate)       AS max_packet_drop_rate,
       MIN(packet_drop_rate)       AS min_packet_drop_rate

FROM network_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('network_metrics_hourly',
                                       start_offset => INTERVAL '3 hours',
                                       end_offset => INTERVAL '1 hours',
                                       schedule_interval => INTERVAL '1 hour'
       );



SELECT add_retention_policy('network_metrics_hourly', INTERVAL '90 days');



CREATE MATERIALIZED VIEW network_metrics_daily
    WITH (timescaledb.continuous) AS
SELECT time_bucket('1 day', time) AS bucket,
       node_key,

       AVG(rx_bps)                AS avg_rx_bps,
       MAX(rx_bps)                AS max_rx_bps,
       MIN(rx_bps)                AS min_rx_bps,

       AVG(tx_bps)                AS avg_tx_bps,
       MAX(tx_bps)                AS max_tx_bps,
       MIN(tx_bps)                AS min_tx_bps,

       AVG(packet_drop_rate)      AS avg_packet_drop_rate,
       MAX(packet_drop_rate)      AS max_packet_drop_rate,
       MIN(packet_drop_rate)      AS min_packet_drop_rate

FROM network_metrics
GROUP BY bucket, node_key;

SELECT add_continuous_aggregate_policy('network_metrics_daily',
                                       start_offset => INTERVAL '3 days',
                                       end_offset => INTERVAL '1 day',
                                       schedule_interval => INTERVAL '1 day'
       );

SELECT add_retention_policy('network_metrics_daily', INTERVAL '365 days');

