CREATE TABLE nodes
(

    node_key   INTEGER GENERATED ALWAYS AS IDENTITY,
    id         UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    os_name    VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45)  NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    op_status  VARCHAR(20)  NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT PK_nodes_id PRIMARY KEY (node_key),
    CONSTRAINT UQ_nodes_source_id UNIQUE (id)
);

CREATE INDEX IX_nodes_lookup ON nodes (is_deleted, op_status);



