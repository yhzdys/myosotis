package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.MyosotisApplication;
import com.yhzdys.myosotis.MyosotisClient;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.spring.annotation.Myosotis;
import com.yhzdys.myosotis.spring.annotation.MyosotisValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyosotisValueAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MyosotisValueAutoConfiguration.class);

    /**
     * <namespace + configKey, autoConfigListener>
     */
    private Map<String, AutoConfigListener> cache;
    private MyosotisApplication application;
    private String defaultNamespace = null;

    @SuppressWarnings("deprecation")
    private static void setFieldValue(Object object, Field field, String configValue) throws Exception {
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        field.set(object, castType(field.getType(), configValue));
        if (!accessible) {
            field.setAccessible(false);
        }
    }

    private static Object castType(Class<?> type, String configValue) {
        if (configValue == null) {
            return null;
        }
        if (type == String.class) {
            return configValue;
        }
        if (StringUtils.isBlank(configValue)) {
            return null;
        }
        return JsonUtil.toObject(configValue, type);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        try {
            application = applicationContext.getBean(MyosotisApplication.class);
        } catch (Exception e) {
            logger.warn("Can not find bean of MyosotisApplication.class, please check your configuration");
            return;
        }
        Map<String, Object> configBeanMap = applicationContext.getBeansWithAnnotation(Myosotis.class);
        if (configBeanMap.isEmpty()) {
            return;
        }
        List<MyosotisClient> clients = new ArrayList<>(application.clients());
        if (clients.size() == 1) {
            this.defaultNamespace = clients.get(0).getNamespace();
        }
        cache = new ConcurrentHashMap<>(2);
        for (Object bean : configBeanMap.values()) {
            this.initMyosotisBean(bean);
        }
    }

    private void initMyosotisBean(Object object) {
        if (object == null) {
            return;
        }
        Myosotis myosotis = object.getClass().getAnnotation(Myosotis.class);
        String namespace = StringUtils.isEmpty(myosotis.namespace()) ? this.defaultNamespace : myosotis.namespace();

        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                this.initFieldValue(namespace, object, field);
            } catch (Exception e) {
                logger.error("Init myosotis value [{}.{}] failed, {}", object.getClass().getName(), field.getName(), e.getMessage());
            }
        }
    }

    private void initFieldValue(String namespace, Object object, Field field) {
        MyosotisValue myosotisValue = field.getAnnotation(MyosotisValue.class);
        if (myosotisValue == null) {
            return;
        }
        if (namespace == null && StringUtils.isEmpty(myosotisValue.namespace())) {
            return;
        }

        String namespace4Init = namespace;
        // MyosotisValue的namespace优先级高
        if (StringUtils.isNotEmpty(myosotisValue.namespace())) {
            namespace4Init = myosotisValue.namespace();
        }

        String configKey4Init = myosotisValue.configKey();
        if (StringUtils.isEmpty(configKey4Init)) {
            configKey4Init = field.getName();
        }
        MyosotisClient client = application.getClient(namespace4Init);
        application.addConfigListener(this.getListener(namespace4Init, configKey4Init, object, field));
        String configValue = null;
        try {
            configValue = client.getString(configKey4Init);
        } catch (Exception e) {
            logger.error("Get config value of " + namespace4Init + ":" + configKey4Init + " failed", e);
        }
        String defaultValue = myosotisValue.defaultValue();
        if (configValue == null && StringUtils.isNotEmpty(defaultValue)) {
            configValue = defaultValue;
        }
        try {
            setFieldValue(object, field, configValue);
        } catch (Exception e) {
            logger.error("Init config value of " + namespace4Init + "." + configKey4Init + " failed", e);
        }
    }

    private ConfigListener getListener(String namespace, String configKey, Object object, Field field) {
        String key = namespace + ":" + configKey;
        AutoConfigListener listener = cache.get(key);
        if (listener != null) {
            listener.addField(object, field);
            return null;
        }
        listener = new AutoConfigListener(namespace, configKey);
        listener.addField(object, field);
        cache.put(key, listener);
        return listener;
    }

    private static final class AutoConfigListener implements ConfigListener {

        private static final Logger logger = LoggerFactory.getLogger(AutoConfigListener.class);

        private final String namespace;
        private final String configKey;

        private final Map<Object, List<Field>> cache = new ConcurrentHashMap<>(2);

        private AutoConfigListener(String namespace, String configKey) {
            this.namespace = namespace;
            this.configKey = configKey;
        }

        @Override
        public String namespace() {
            return namespace;
        }

        @Override
        public String configKey() {
            return configKey;
        }

        @Override
        public void handle(MyosotisEvent event) {
            String configKey = event.getConfigKey();
            String configValue = event.getConfigValue();

            for (Map.Entry<Object, List<Field>> entry : cache.entrySet()) {
                Object object = entry.getKey();
                List<Field> fields = entry.getValue();
                for (Field field : fields) {
                    if (configValue == null) {
                        MyosotisValue myosotisValue = field.getAnnotation(MyosotisValue.class);
                        if (StringUtils.isNotEmpty(myosotisValue.defaultValue())) {
                            configValue = myosotisValue.defaultValue();
                        }
                    }
                    try {
                        setFieldValue(object, field, configValue);
                    } catch (Exception e) {
                        logger.error("Update config value failed, configKey: {}", configKey, e);
                    }
                }
            }
        }

        private void addField(Object object, Field field) {
            List<Field> fields = cache.computeIfAbsent(object, k -> new CopyOnWriteArrayList<>());
            if (fields.contains(field)) {
                return;
            }
            fields.add(field);
        }
    }
}
