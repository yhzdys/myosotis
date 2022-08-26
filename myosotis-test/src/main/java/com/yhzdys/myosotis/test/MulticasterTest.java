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
    public void testAddNamespaceListener() throws Exception {
        EventMulticaster multicaster = new EventMulticaster();

        TestNamespaceListener listener1 = new TestNamespaceListener("1", "namespace");
        TestNamespaceListener listener2 = new TestNamespaceListener("2", "namespace");
        multicaster.addNamespaceListener(listener1);
        multicaster.addNamespaceListener(listener1);
        multicaster.addNamespaceListener(listener1);
        multicaster.addNamespaceListener(listener2);
        listener1.setName("111");

        MyosotisEvent event = new MyosotisEvent("namespace", "test", EventType.UPDATE);
        multicaster.multicast(event);
        Thread.sleep(3000);
    }

    @Test
    public void testAddConfigListener() throws Exception {
        EventMulticaster multicaster = new EventMulticaster();

        TestConfigListener listener1 = new TestConfigListener("1", "namespace", "test");
        TestConfigListener listener2 = new TestConfigListener("2", "namespace", "test");
        multicaster.addConfigListener(listener1);
        multicaster.addConfigListener(listener1);
        multicaster.addConfigListener(listener1);
        multicaster.addConfigListener(listener2);
        listener1.setName("111");

        MyosotisEvent event = new MyosotisEvent("namespace", "test", EventType.UPDATE);
        multicaster.multicast(event);
        Thread.sleep(3000);
    }

    @Test
    public void testConfigMulticaster() throws Exception {
        EventMulticaster multicaster = new EventMulticaster();

        multicaster.addConfigListener(new TestConfigListener("1", "namespace", "test0"));
        multicaster.addConfigListener(new TestConfigListener("1", "namespace", "test1"));
        multicaster.addConfigListener(new TestConfigListener("1", "namespace", "test2"));
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

        multicaster.addNamespaceListener(new TestNamespaceListener("1", "namespace1"));
        multicaster.addNamespaceListener(new TestNamespaceListener("1", "namespace2"));

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

    static class TestNamespaceListener implements NamespaceListener {

        private final String namespace;
        private String name;

        public TestNamespaceListener(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String namespace() {
            return namespace;
        }

        public void handle(MyosotisEvent event) throws Exception {
            Thread.sleep(1000);
            System.out.println(name + "> event: " + JsonUtil.toString(event));
        }
    }

    static class TestConfigListener implements ConfigListener {

        private final String namespace;
        private final String configKey;
        private String name;

        public TestConfigListener(String name, String namespace, String configKey) {
            this.name = name;
            this.namespace = namespace;
            this.configKey = configKey;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String configKey() {
            return configKey;
        }

        public String namespace() {
            return namespace;
        }

        public void handle(MyosotisEvent event) throws Exception {
            Thread.sleep(1000);
            System.out.println(name + "> event: " + JsonUtil.toString(event));
        }
    }
}
