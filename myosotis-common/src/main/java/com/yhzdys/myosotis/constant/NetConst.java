package com.yhzdys.myosotis.constant;

import org.apache.http.client.config.RequestConfig;

public final class NetConst {

    public static final String support_yes = "y";

    // myosotis-client-ip
    public static final String client_host_ip = "myosotis-ip";
    // myosotis-client-language
    public static final String client_language = "myosotis-cl";

    // myosotis-compress-support 是否支持压缩，服务端返回的数据是否经过压缩还要依据原始数据大小是否超过阀值
    public static final String compress_support = "myosotis-cs";
    // myosotis-origin-data-length 原始数据的长度，用于数据的解压缩
    public static final String origin_data_length = "myosotis-odl";

    // myosotis-serialize-type 序列化方式
    public static final String serialize_type = "myosotis-st";
    public static final String serialize_avro_support = "myosotis-sas";
    public static final String serialize_protostuff_support = "myosotis-sps";

    // default
    public static final RequestConfig default_config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
    // long-polling config
    public static final RequestConfig long_polling_config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).build();

    public static final class URL {

        public static final String polling = "/polling";
        public static final String query_namespace = "/query/namespace/%s";
        public static final String query_config = "/query/config/%s/%s";

        public static String queryConfig(String namespace, String configKey) {
            if (configKey == null) {
                return String.format(query_namespace, namespace);
            }
            return String.format(query_config, namespace, configKey);
        }
    }
}
