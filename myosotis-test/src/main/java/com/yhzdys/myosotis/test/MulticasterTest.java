package com.yhzdys.myosotis.test;

import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.enums.EventType;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.event.listener.NamespaceListener;
import com.yhzdys.myosotis.event.multicast.EventMulticaster;
import com.yhzdys.myosotis.misc.JsonUtil;
import org.junit.Test;

public class MulticasterTest {

    @Test
    public void testConfigMulticaster() throws Exception {
        EventMulticaster multicaster = new EventMulticaster();
        multicaster.addConfigListener(new ConfigListener() {
            @Override
            public String configKey() {
                return "test";
            }

            @Override
            public String namespace() {
                return "namespace";
            }

            @Override
            public void handle(MyosotisEvent event) {
                System.out.println("event: " + JsonUtil.toString(event));
            }
        });


        for (int i = 0; i < 10000; i++) {
            MyosotisEvent event = new MyosotisEvent("namespace", "test", EventType.UPDATE);
            event.setVersion(i);

            multicaster.multicast(event);
        }

        Thread.sleep(5000);
    }

    @Test
    public void testNamespaceMulticaster() throws Exception {
        EventMulticaster multicaster = new EventMulticaster();
        multicaster.addNamespaceListener(new NamespaceListener() {
            @Override
            public String namespace() {
                return "namespace";
            }

            @Override
            public void handle(MyosotisEvent event) {
                System.out.println("event: " + JsonUtil.toString(event));
            }
        });


        for (int i = 0; i < 10000; i++) {
            MyosotisEvent event1 = new MyosotisEvent("namespace", "test1", EventType.UPDATE);
            event1.setVersion(i);
            multicaster.multicast(event1);
            MyosotisEvent event2 = new MyosotisEvent("namespace", "test2", EventType.UPDATE);
            event2.setVersion(i);
            multicaster.multicast(event2);
        }

        Thread.sleep(5000);
    }
}
