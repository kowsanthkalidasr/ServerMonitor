IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'serMonDb')
BEGIN
    CREATE DATABASE serMonDB;
END
GO

USE serMonDB;
GO

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'sermon_admin')
BEGIN
    CREATE LOGIN sermon_admin WITH PASSWORD = 'Admin@123';
END
GO

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'sermon_admin')
BEGIN
    CREATE USER sermon_admin FOR LOGIN sermon_admin;
END
GO


-- ENABLE CDC

IF (SELECT is_cdc_enabled
    FROM sys.databases
    WHERE name = 'serMonDB') = 0
    BEGIN
        EXEC sys.sp_cdc_enable_db;
        PRINT 'CDC enabled on Database level.';
    END
GO


-- Grant db_owner so Debezium can read the cdc schema and transaction log
ALTER ROLE [db_owner] ADD MEMBER [sermon_admin];
GO

-- (Optional but recommended) Grant VIEW DATABASE STATE permissions
GRANT VIEW DATABASE STATE TO [sermon_admin];
GO

