package com.yhzdys.myosotis.test;

import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.MyosotisClient;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ApplicationTest {

    @Test
    public void testApplications() throws Exception {
        for (int i = 0; i < 128; i++) {
            MyosotisApplication application = new MyosotisApplication("http://127.0.0.1:7777");
            MyosotisClient client = application.getClient("default");
            client.getString("test");
        }
        Thread.sleep(TimeUnit.HOURS.toMillis(1));
    }
}
