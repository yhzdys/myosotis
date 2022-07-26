package com.yhzdys.myosotis.constant;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;

public final class NetConst {

    public static final String support_yes = "y";

    // myosotis-client-ip
    public static final String client_ip = "myosotis-ci";
    // myosotis-client-language
    public static final String client_language = "myosotis-cl";

    // myosotis-compress-support 是否支持压缩，服务端返回的数据是否经过压缩还要依据原始数据大小是否超过阀值
    public static final String compress_support = "myosotis-cs";
    // myosotis-origin-data-length 原始数据的长度，用于数据的解压缩
    public static final String origin_data_length = "myosotis-odl";

    // myosotis-serialize-type 序列化方式
    public static final String serialize_type = "myosotis-st";

    public static final BasicHeader header_long_connection = new BasicHeader("Connection", "keep-alive");
    // 连接最终关闭时间由系统MSL(Max Segment Lifetime)决定
    public static final BasicHeader header_short_connection = new BasicHeader("Connection", "close");

    // long-polling config
    public static final RequestConfig long_polling_config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).build();

    public static final class URL {

        public static final String polling = "/polling";

        private static final String query_namespace = "/query/namespace/%s";
        private static final String query_config = "/query/config/%s/%s";

        public static String query(String namespace, String configKey) {
            if (configKey == null) {
                return String.format(query_namespace, namespace);
            }
            return String.format(query_config, namespace, configKey);
        }
    }
}
