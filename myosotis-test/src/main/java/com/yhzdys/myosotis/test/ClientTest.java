package com.yhzdys.myosotis.test;

import com.yhzdys.myosotis.Config;
import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.MyosotisClient;
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

//        Config config = new Config("http://myosotis-server.yhzdys.com");
        Config config = new Config("http://127.0.0.1:7777");
        config.serializeType(SerializeType.AVRO);
        config.enableSnapshot(false);
        config.enableCompress(true);
        config.compressThreshold(10);
        MyosotisApplication application = new MyosotisApplication(config);

        MyosotisClient client = application.getClient("default");

        application.addNamespaceListener(new NamespaceListener() {
            @Override
            public String namespace() {
                return "default";
            }

            @Override
            public void handle(MyosotisEvent event) {
                System.out.println("g event: " + JsonUtil.toString(event));
            }
        });
        application.addConfigListener(new ConfigListener() {
            @Override
            public String configKey() {
                return "123";
            }

            @Override
            public String namespace() {
                return "default";
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
            String config1 = client.getString(key1);
            System.out.println("##### " + key1 + ":" + config1);
            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000) + 1);
        }
    }
}
