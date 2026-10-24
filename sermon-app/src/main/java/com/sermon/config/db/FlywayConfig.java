package com.sermon.config.db;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway mssqlFlyWay(@Qualifier("mssqlDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/mssql")
                .load();
    }


    @Bean(initMethod = "migrate")
    public Flyway timescaleFlyWay(@Qualifier("timescaleDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/timescale")
                .executeInTransaction(false)
                .load();
    }




}
