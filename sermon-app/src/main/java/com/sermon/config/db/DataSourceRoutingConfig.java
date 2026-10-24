package com.sermon.config.db;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceRoutingConfig {
    @Primary
    @Bean(name = "mssqlDataSource")
    @ConfigurationProperties(prefix = "spring.mssql.datasource")
    public DataSource mssqlDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "timescaleDataSource")
    @ConfigurationProperties(prefix = "spring.timescaledb.datasource")
    public DataSource timescaleDataSource() {
        return new HikariDataSource();
    }

    @Bean(name = "timescaleJdbcTemplate")
    public JdbcTemplate timescaleJdbcTemplate(@Qualifier("timescaleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
