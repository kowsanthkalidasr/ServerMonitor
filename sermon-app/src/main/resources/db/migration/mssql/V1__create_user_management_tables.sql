-- Creating User Management Tables

-- Table : User

CREATE TABLE users
(
    id       UNIQUEIDENTIFIER PRIMARY KEY ,
    username     VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(320) NOT NULL,
    status        VARCHAR(1)   NOT NULL,

    CONSTRAINT CK_USERS_Status CHECK (status IN ('A', 'D'))
);

CREATE UNIQUE NONCLUSTERED INDEX UQ_users_active_username  ON users(username) where status = 'A';
CREATE UNIQUE NONCLUSTERED INDEX UQ_users_active_email ON users(email) where status = 'A';
CREATE NONCLUSTERED INDEX IX_users_jwt_active_check ON users (id) INCLUDE(status) WHERE status = 'A';


-- Table : User_Info

CREATE TABLE user_info
(
    id      UNIQUEIDENTIFIER PRIMARY KEY,
    first_name   VARCHAR(100)     NOT NULL,
    last_name    VARCHAR(100),
    description  VARCHAR(1000),
    created_by   UNIQUEIDENTIFIER NOT NULL,
    created_time DATETIMEOFFSET(2)   NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    updated_by   UNIQUEIDENTIFIER NOT NULL,
    updated_time DATETIMEOFFSET(2) NOT NULL DEFAULT SYSDATETIMEOFFSET(),

    CONSTRAINT FK_USER_INFO_USER_User_id FOREIGN KEY (id) REFERENCES USERS (id),
    CONSTRAINT FK_USER_INFO_USER_Created_by FOREIGN KEY (created_by) REFERENCES USERS (id),
    CONSTRAINT FK_USER_INFO_USER_Updated_by FOREIGN KEY (updated_by) REFERENCES USERS (id)
);

-- Table : Roles

CREATE TABLE roles
(
    id     BIGINT IDENTITY(1,1) PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_ROLES_RoleName UNIQUE (name)
);


-- Table : Users_Roles

CREATE TABLE users_roles
(
    user_id       UNIQUEIDENTIFIER NOT NULL,
    role_id       BIGINT           NOT NULL,
    assigned_by   UNIQUEIDENTIFIER NOT NULL,
    assigned_time DATETIMEOFFSET(2)   NOT NULL DEFAULT SYSDATETIMEOFFSET(),

    CONSTRAINT PK_USERS_ROLES PRIMARY KEY CLUSTERED (user_id,role_id),
    CONSTRAINT FK_USERS_ROLES_User_id FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    CONSTRAINT FK_USERS_ROLES_Role_id FOREIGN KEY (role_id) REFERENCES ROLES (id) ON DELETE CASCADE,
    CONSTRAINT FK_USERS_ROLES_Assigned_by FOREIGN KEY (assigned_by) REFERENCES USERS (id)
);
-- Table : Permissions

CREATE TABLE permissions
(
    id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description     VARCHAR(100) NOT NULL,

    CONSTRAINT UQ_PERMISSIONS_Permission_name UNIQUE (name)
);

-- Table : users_permissions

CREATE TABLE users_permissions
(
    user_id       UNIQUEIDENTIFIER NOT NULL,
    permission_id BIGINT           NOT NULL,
    assigned_by   UNIQUEIDENTIFIER,
    assigned_time DATETIMEOFFSET(2) DEFAULT SYSDATETIMEOFFSET(),

    CONSTRAINT PK_USERS_PERMISSIONS PRIMARY KEY (user_id, permission_id),

    CONSTRAINT FK_USERS_PERMISSIONS_User_id FOREIGN KEY (user_id) REFERENCES USERS (id) ON DELETE CASCADE,
    CONSTRAINT FK_USERS_PERMISSIONS_Permission_id FOREIGN KEY (permission_id) REFERENCES PERMISSIONS (id) ON DELETE CASCADE
);


-- Table : roles_permissions

CREATE TABLE roles_permissions
(
    role_id         BIGINT                  NOT NULL,
    permission_id   BIGINT                  NOT NULL,
    assigned_by     UNIQUEIDENTIFIER        NOT NULL,
    assigned_time   DATETIMEOFFSET(2)       NOT NULL DEFAULT SYSDATETIMEOFFSET(),

    CONSTRAINT PK_ROLES_PERMISSIONS PRIMARY KEY
        (role_id, permission_id),
    CONSTRAINT FK_ROLES_PERMISSIONS_Role_id FOREIGN KEY (role_id) REFERENCES ROLES (id),
    CONSTRAINT FK_ROLES_PERMISSIONS_Permission_id FOREIGN KEY (permission_id) REFERENCES PERMISSIONS (id),
    CONSTRAINT FK_ROLES_PERMISSIONS_Assigned_by FOREIGN KEY (assigned_by) REFERENCES USERS (id)
);

