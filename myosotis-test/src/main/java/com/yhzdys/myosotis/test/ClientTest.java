package com.yhzdys.myosotis.test;

import com.yhzdys.myosotis.MyosotisClient;
import com.yhzdys.myosotis.MyosotisClientManager;
import com.yhzdys.myosotis.MyosotisCustomizer;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.enums.SerializeType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.misc.JsonUtil;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientTest {

    @Test
    public void testClient() throws Exception {

        MyosotisCustomizer customizer = new MyosotisCustomizer("http://myosotis-server.yhzdys.com");
//        MyosotisCustomizer customizer = new MyosotisCustomizer("http://127.0.0.1:7777");
        customizer.serializeType(SerializeType.JSON);
        customizer.enableCompress(true);
        customizer.compressThreshold(10);
        MyosotisClientManager clientManager = new MyosotisClientManager(customizer);

        MyosotisClient client = clientManager.getClient("default");

        clientManager.addNamespaceListener(new NamespaceListener() {
            @Override
            public String namespace() {
                return "default";
            }

            @Override
            public void handle(MyosotisEvent event) {
                System.out.println("g event: " + JsonUtil.toString(event));
            }
        });
        clientManager.addConfigListener(new ConfigListener() {
            @Override
            public String configKey() {
                return "test_key12";
            }

            @Override
            public String namespace() {
                return "test_namespace";
            }

            @Override
            public void handle(MyosotisEvent event) {
                System.out.println("s event: " + JsonUtil.toString(event));
            }
        });

        Random random = new Random();
        for (; ; ) {
            System.out.println("--------------------------------");
            String key1 = "123";
            String config1 = client.getConfig("test_key11");
            System.out.println("##### " + key1 + ":" + config1);
            String key2 = "test_key" + (random.nextInt(100) + 1);
            String config2 = client.getConfig(key2);
            System.out.println("##### " + key2 + ":" + config2);
            TimeUnit.MILLISECONDS.sleep(random.nextInt(5000) + 1);
        }
    }
}
