package com.yhzdys.myosotis;

import com.yhzdys.myosotis.constant.SysConst;
import com.yhzdys.myosotis.exception.MyosotisException;
import org.apache.commons.lang3.StringUtils;

public class InfraConst {

    public static final String default_time_format = "yyyy-MM-dd HH:mm:ss";
    private static final String home_dir = homeDir();
    public static final String default_log_dir = home_dir + "log";
    public static final String default_sqlite_path = home_dir + "database" + SysConst.separator + "myosotis.db";
    public static final String datasource_config_path = home_dir + "config" + SysConst.separator + "datasource.conf";
    public static final String console_config_path = home_dir + "config" + SysConst.separator + "console.conf";
    public static final String server_config_path = home_dir + "config" + SysConst.separator + "server.conf";
    public static final String cluster_config_path = home_dir + "config" + SysConst.separator + "cluster.conf";

    private static String homeDir() {
        String home = System.getProperty("myosotis.home");
        if (StringUtils.isEmpty(home)) {
            throw new MyosotisException("Can not find variable [myosotis.home] in your environment");
        }
        if (home.endsWith(SysConst.separator)) {
            return home;
        }
        return home + SysConst.separator;
    }
}
