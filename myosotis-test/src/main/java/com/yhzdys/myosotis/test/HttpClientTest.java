package com.yhzdys.myosotis.test;

import com.yhzdys.myosotis.constant.NetConst;
import com.yhzdys.myosotis.constant.SysConst;
import com.yhzdys.myosotis.misc.MyosotisHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class HttpClientTest {

    @Test
    public void testShortConnection() throws Exception {
        MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

        HttpGet request = new HttpGet("http://127.0.0.1:7777/cluster/health");
        request.setHeader(NetConst.client_ip, SysConst.local_host);
        request.setHeader(NetConst.header_short_connection);
        CloseableHttpResponse response = myosotisHttpClient.execute(request);

        System.out.println(response.getFirstHeader("Connection"));
        System.out.println(EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testLongConnection() throws Exception {
        MyosotisHttpClient myosotisHttpClient = MyosotisHttpClient.getInstance();

        HttpGet request = new HttpGet("http://127.0.0.1:7777/cluster/health");
        request.setHeader(NetConst.client_ip, SysConst.local_host);
        request.setHeader(NetConst.header_long_connection);
        CloseableHttpResponse response = myosotisHttpClient.execute(request);

        System.out.println(response.getFirstHeader("Connection"));
        System.out.println(response.getFirstHeader("Keep-Alive"));
    }
}
