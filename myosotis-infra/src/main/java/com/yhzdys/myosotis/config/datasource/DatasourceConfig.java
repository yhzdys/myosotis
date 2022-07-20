package com.yhzdys.myosotis.config.datasource;

import com.yhzdys.myosotis.config.BaseConfig;

/**
 * customized configs
 */
public class DatasourceConfig extends BaseConfig {

    private String mysqlUrl;

    private String mysqlUsername;

    private String mysqlPassword;

    public String getMysqlUrl() {
        return mysqlUrl;
    }

    public void setMysqlUrl(String mysqlUrl) {
        this.mysqlUrl = mysqlUrl;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public void setMysqlUsername(String mysqlUsername) {
        this.mysqlUsername = mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }
}
