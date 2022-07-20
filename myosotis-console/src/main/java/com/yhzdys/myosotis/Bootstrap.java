package com.yhzdys.myosotis;

import com.yhzdys.myosotis.config.console.ConsoleConfig;
import com.yhzdys.myosotis.config.console.ConsoleConfigLoader;
import com.yhzdys.myosotis.exception.MyosotisException;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

public class Bootstrap implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        try {
            // load customized config
            ConsoleConfig config = ConsoleConfigLoader.get();
            // init log dir before application start
            System.setProperty("myosotis.log.dir", config.getLogDir());
        } catch (MyosotisException e) {
            throw e;
        } catch (Exception e) {
            throw new MyosotisException("Unknown error when init configs", e);
        }
    }
}
