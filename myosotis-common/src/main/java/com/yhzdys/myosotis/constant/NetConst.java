package com.yhzdys.myosotis.constant;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;

/***
 * network common constant
 */
public final class NetConst {

    /*
      myosotis features
     */
    public static final String support_yes = "Y";
    public static final String support_no = "N";

    // myosotis-client-ip
    public static final String client_host_ip = "Myosotis-CIP";

    // myosotis-client-language
    public static final String client_language = "Myosotis-CL";

    // myosotis-client-version
    public static final String client_version = "Myosotis-CV";

    // myosotis-compress-support 是否支持压缩,服务端返回的数据是否经过压缩还要依据原始数据大小是否超过阀值
    public static final String compress_support = "Myosotis-CS";
    // myosotis-origin-data-length 原始数据的长度，用于数据的解压缩
    public static final String origin_data_length = "Myosotis-ODL";

    // myosotis-serialize-type 序列化方式
    public static final String serialize_type = "Myosotis-ST";
    public static final String serialize_avro_support = "Myosotis-SAS";
    public static final String serialize_protostuff_support = "Myosotis-SPS";

    // default
    public static final RequestConfig default_config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
    // long-polling config
    public static final RequestConfig long_polling_config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(20000).build();

    // json content-type
    public static Header header_content_json = new BasicHeader("Content-Type", "application/json");

    public static final class URL {
        public static final String polling = "/polling";

        public static final String query_namespace = "/query/namespace/:namespace";
        public static final String query_config = "/query/config/:namespace/:config_key";
        public static final String query_configs = "/query/configs";

        private static final String namespace = ":namespace";
        private static final String config_key = ":config_key";

        public static String configQuery(String namespace, String configKey) {
            if (configKey == null) {
                return query_namespace.replace(URL.namespace, namespace);
            }
            return query_config.replace(URL.namespace, namespace).replace(config_key, configKey);
        }
    }

}
