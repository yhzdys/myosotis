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
                return "test0";
            }

            @Override
            public String namespace() {
                return "namespace";
            }

            @Override
            public void handle(MyosotisEvent event) throws Exception {
                Thread.sleep(1000);
                System.out.println("event: " + JsonUtil.toString(event));
            }
        });
        multicaster.addConfigListener(new ConfigListener() {
            @Override
            public String configKey() {
                return "test1";
            }

            @Override
            public String namespace() {
                return "namespace";
            }

            @Override
            public void handle(MyosotisEvent event) throws Exception {
                Thread.sleep(1000);
                System.out.println("event: " + JsonUtil.toString(event));
            }
        });
        multicaster.addConfigListener(new ConfigListener() {
            @Override
            public String configKey() {
                return "test2";
            }

            @Override
            public String namespace() {
                return "namespace";
            }

            @Override
            public void handle(MyosotisEvent event) throws Exception {
                Thread.sleep(1000);
                System.out.println("event: " + JsonUtil.toString(event));
            }
        });

        for (int i = 0; i < 100000; i++) {
            MyosotisEvent event = new MyosotisEvent("namespace", "test" + i % 3, EventType.UPDATE);
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
                return "namespace1";
            }

            @Override
            public void handle(MyosotisEvent event) throws Exception {
                Thread.sleep(1000);
                System.out.println("event1: " + JsonUtil.toString(event));
            }
        });
        multicaster.addNamespaceListener(new NamespaceListener() {
            @Override
            public String namespace() {
                return "namespace2";
            }

            @Override
            public void handle(MyosotisEvent event) throws Exception {
                Thread.sleep(1000);
                System.out.println("event2: " + JsonUtil.toString(event));
            }
        });

        for (int i = 0; i < 100000; i++) {
            MyosotisEvent event1 = new MyosotisEvent("namespace1", "test1", EventType.UPDATE);
            event1.setVersion(i);
            MyosotisEvent event2 = new MyosotisEvent("namespace1", "test2", EventType.UPDATE);
            event2.setVersion(i);
            MyosotisEvent event3 = new MyosotisEvent("namespace2", "test3", EventType.UPDATE);
            event3.setVersion(i);
            MyosotisEvent event4 = new MyosotisEvent("namespace2", "test4", EventType.UPDATE);
            event4.setVersion(i);

            multicaster.multicast(event1);
            multicaster.multicast(event2);
            multicaster.multicast(event3);
            multicaster.multicast(event4);
        }

        Thread.sleep(5000);
    }
}
