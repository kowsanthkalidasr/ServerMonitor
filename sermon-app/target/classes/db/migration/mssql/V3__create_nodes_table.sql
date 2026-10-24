-- -- create table: nodes
CREATE TABLE nodes
(
    id           UNIQUEIDENTIFIER  NOT NULL,
    name         NVARCHAR(100)     NOT NULL,
    os_name      NVARCHAR(100)     NOT NULL,
    ip_address   VARCHAR(45)       NOT NULL,
    type         VARCHAR(30)       NOT NULL,
    op_status    VARCHAR(20)       NOT NULL,
    is_deleted   BIT               NOT NULL DEFAULT 0,

    created_by   UNIQUEIDENTIFIER  NOT NULL,
    created_time DATETIMEOFFSET(2) NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_by   UNIQUEIDENTIFIER  NOT NULL,
    updated_time DATETIMEOFFSET(2) NOT NULL DEFAULT SYSDATETIMEOFFSET(),

    CONSTRAINT PK_nodes_id PRIMARY KEY (id),
    CONSTRAINT CK_nodes_type CHECK (type IN ('Linux_Server', 'Windows_Server', 'Docker_Hosts', 'Database_Server')),
    CONSTRAINT CK_nodes_op_status CHECK (op_status IN ('UP', 'DOWN', 'UNREACHABLE', 'ON_MAINTENANCE')),
    CONSTRAINT FK_nodes_users_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT FK_nodes_users_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)

)

CREATE UNIQUE NONCLUSTERED INDEX UQ_nodes_active_ip_address on nodes (ip_address) where is_deleted = 0;

CREATE NONCLUSTERED INDEX IX_nodes_op_status ON nodes (is_deleted, op_status);
CREATE NONCLUSTERED INDEX IX_nodes_active ON nodes (op_status) INCLUDE (name, ip_address) where is_deleted = 0;


-- CONFIGURE CDC

IF EXISTS (SELECT *
           FROM sys.tables
           WHERE name = 'nodes'
             AND is_tracked_by_cdc = 0)
    BEGIN
        EXEC sys.sp_cdc_enable_table
             @source_schema = N'dbo',
             @source_name = N'nodes',
             @role_name = NULL,
             @supports_net_changes = 1,
            @captured_column_list = N'id,name,os_name,ip_address,type,op_status,is_deleted'
    END
    GO



CREATE TABLE node_monitoring_config (
    node_id UNIQUEIDENTIFIER NOT NULL,

)
















