package com.yhzdys.myosotis.database;

import com.yhzdys.myosotis.InfraConst;
import com.yhzdys.myosotis.config.datasource.DatasourceConfig;
import com.yhzdys.myosotis.config.datasource.DatasourceConfigLoader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatasourceFactoryBean implements FactoryBean<DataSource> {

    @Override
    public DataSource getObject() {
        DatasourceConfig config = DatasourceConfigLoader.get();
        if (StringUtils.isNotEmpty(config.getMysqlUrl())) {
            return this.mysqlDatasource(config);
        }
        return this.innerDatasource();
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    private DataSource mysqlDatasource(DatasourceConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("Myosotis-MySQL");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getMysqlUrl());
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(256);
        hikariConfig.setConnectionTimeout(2000);
        return new HikariDataSource(hikariConfig);
    }

    private DataSource innerDatasource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("Myosotis-SQLite");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + InfraConst.default_sqlite_path);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(1);
        hikariConfig.setConnectionTimeout(2000);
        return new HikariDataSource(hikariConfig);
    }
}
