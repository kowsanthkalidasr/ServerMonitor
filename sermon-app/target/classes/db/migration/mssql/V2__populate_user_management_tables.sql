-- Populate USERS Table with admin user

DECLARE @super_admin_id UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000001';

INSERT INTO users (id, username, password_hash, email, status)
VALUES (@super_admin_id, 'admin', '{argon2}$argon2id$v=19$m=16384,t=2,p=1$jtt019qcD8A/M9u78KwN7Q$hFdszLplflVcKpgiSUXyUxQsdjrl49e41P2d+evjqdg', 'admin@admin.com', 'A');


-- Populate USER_INFO table

INSERT INTO user_info (id, first_name, description, created_by,updated_by)
VALUES (@super_admin_id, 'admin', 'Super Admin User with all privileges',
        @super_admin_id,@super_admin_id);

-- Declare variables for role and permission ids

DECLARE @admin_role_id INT
DECLARE @sre_role_id INT
DECLARE @viewer_role_id INT

DECLARE @user_create_perm_id INT;
DECLARE @user_delete_perm_id INT;
DECLARE @user_modify_perm_id INT;
DECLARE @server_create_perm_id INT;
DECLARE @server_read_perm_id INT;
DECLARE @server_delete_perm_id INT;
DECLARE @config_write_perm_id INT;
DECLARE @config_read_perm_id INT;
DECLARE @alert_read_perm_id INT;
DECLARE @alert_delete_perm_id INT;


-- Populate ROLES table

INSERT INTO roles (name, description)
VALUES ('ROLE_ADMIN', 'Super Administrator with full CRUD privileges over users, scope modifications, system log visibility, and inherited SRE management capabilities.');
SET @admin_role_id = SCOPE_IDENTITY();

INSERT INTO roles (name, description)
VALUES ('ROLE_SRE', 'Site Reliability Engineer with authorization to manage monitored servers, configure metric collection intervals, and manipulate alert thresholds and states.');
SET @sre_role_id = SCOPE_IDENTITY();

INSERT INTO roles (name, description)
VALUES ('ROLE_VIEWER', 'Read-only access to monitor servers, view active system alerts, and inspect configured metric thresholds.');
SET @viewer_role_id = SCOPE_IDENTITY();



-- Populate PERMISSIONS table

-- Insert Permissions
INSERT INTO permissions (name, description)
VALUES ('user:create', 'Add new users to the system');
SET @user_create_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('user:delete', 'Remove users from the system');
SET @user_delete_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('user:modify', 'Modify a user''s roles/permissions');
SET @user_modify_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('server:create', 'Add new servers to monitor');
SET @server_create_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('server:read', 'View monitored servers');
SET @server_read_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('server:delete', 'Delete servers from monitoring');
SET @server_delete_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('config:write', 'Configure collection intervals and alert thresholds');
SET @config_write_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('config:read', 'View configured alert thresholds');
SET @config_read_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('alert:read', 'View triggered alerts');
SET @alert_read_perm_id = SCOPE_IDENTITY();

INSERT INTO permissions (name, description)
VALUES ('alert:delete', 'Dismiss or delete alerts');
SET @alert_delete_perm_id = SCOPE_IDENTITY();


-- Populate USERS_ROLES table for super admin

INSERT INTO users_roles (user_id, role_id, assigned_by)
VALUES ('00000000-0000-0000-0000-000000000001', 1, '00000000-0000-0000-0000-000000000001');


-- Populate ROLES_PERMISSIONS

-- ROLE_ADMIN: All permissions (1-10)
INSERT INTO roles_permissions (role_id, permission_id, assigned_by)
VALUES
    (@admin_role_id, @user_create_perm_id, @super_admin_id),
    (@admin_role_id, @user_delete_perm_id, @super_admin_id),
    (@admin_role_id, @user_modify_perm_id, @super_admin_id),
    (@admin_role_id, @server_create_perm_id, @super_admin_id),
    (@admin_role_id, @server_read_perm_id, @super_admin_id),
    (@admin_role_id, @server_delete_perm_id, @super_admin_id),
    (@admin_role_id, @config_write_perm_id, @super_admin_id),
    (@admin_role_id, @config_read_perm_id, @super_admin_id),
    (@admin_role_id, @alert_read_perm_id, @super_admin_id),
    (@admin_role_id, @alert_delete_perm_id, @super_admin_id);

-- ROLE_SRE: Server, Config, and Alert management (Permissions 4-10)
INSERT INTO roles_permissions (role_id, permission_id, assigned_by)
VALUES
    (@sre_role_id, @server_create_perm_id, @super_admin_id),
    (@sre_role_id, @server_read_perm_id, @super_admin_id),
    (@sre_role_id, @server_delete_perm_id, @super_admin_id),
    (@sre_role_id, @config_write_perm_id, @super_admin_id),
    (@sre_role_id, @config_read_perm_id, @super_admin_id),
    (@sre_role_id, @alert_read_perm_id, @super_admin_id),
    (@sre_role_id, @alert_delete_perm_id, @super_admin_id);

-- ROLE_VIEWER: Read-only access (Permissions 5, 8, 9)
INSERT INTO roles_permissions (role_id, permission_id, assigned_by)
VALUES
    (@viewer_role_id, @server_read_perm_id, @super_admin_id),
    (@viewer_role_id, @config_read_perm_id, @super_admin_id),
    (@viewer_role_id, @alert_read_perm_id, @super_admin_id);


