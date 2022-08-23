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
     * <namespace + configKey, AutoConfigListener.class>
     */
    private Map<String, AutoConfigListener> listeners;
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
        return JsonUtil.toObject(configValue.trim(), type);
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
        listeners = new ConcurrentHashMap<>(2);
        for (Object bean : configBeanMap.values()) {
            this.initMyosotisBean(bean);
        }
    }

    private void initMyosotisBean(Object targetBean) {
        if (targetBean == null) {
            return;
        }
        Myosotis myosotis = targetBean.getClass().getAnnotation(Myosotis.class);
        String namespace = StringUtils.isEmpty(myosotis.namespace()) ? this.defaultNamespace : myosotis.namespace();

        Field[] fields = targetBean.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                this.initFieldValue(namespace, targetBean, field);
            } catch (Exception e) {
                logger.error("Init myosotis value [{}.{}] failed, {}", targetBean.getClass().getName(), field.getName(), e.getMessage());
            }
        }
    }

    private void initFieldValue(String namespace, Object targetBean, Field targetField) {
        MyosotisValue myosotisValue = targetField.getAnnotation(MyosotisValue.class);
        if (myosotisValue == null) {
            return;
        }
        if (namespace == null && StringUtils.isEmpty(myosotisValue.namespace())) {
            return;
        }

        String namespaceForInit = namespace;
        // MyosotisValue的namespace优先级高
        if (StringUtils.isNotEmpty(myosotisValue.namespace())) {
            namespaceForInit = myosotisValue.namespace();
        }

        String configKeyForInit = myosotisValue.configKey();
        if (StringUtils.isEmpty(configKeyForInit)) {
            configKeyForInit = targetField.getName();
        }
        MyosotisClient client = application.getClient(namespaceForInit);
        // add config listener
        application.addConfigListener(
                this.getListener(namespaceForInit, configKeyForInit, targetBean, targetField)
        );
        String configValue = null;
        try {
            configValue = client.getString(configKeyForInit);
        } catch (Exception e) {
            logger.error("Get config value of " + namespaceForInit + ":" + configKeyForInit + " failed", e);
        }
        String defaultValue = myosotisValue.defaultValue();
        if (configValue == null && StringUtils.isNotEmpty(defaultValue)) {
            configValue = defaultValue;
        }
        try {
            setFieldValue(targetBean, targetField, configValue);
        } catch (Exception e) {
            logger.error("Init config value of " + namespaceForInit + "." + configKeyForInit + " failed", e);
        }
    }

    private ConfigListener getListener(String namespace, String configKey, Object targetBean, Field targetField) {
        AutoConfigListener listener = listeners.computeIfAbsent(
                namespace + ":" + configKey, k -> new AutoConfigListener(namespace, configKey)
        );
        listener.addField(targetBean, targetField);
        return listener;
    }

    private static final class AutoConfigListener implements ConfigListener {

        private static final Logger logger = LoggerFactory.getLogger(AutoConfigListener.class);

        private final String namespace;
        private final String configKey;

        /**
         * <targetBean, targetFiled[]>
         */
        private final Map<Object, List<Field>> fieldMap = new ConcurrentHashMap<>(2);

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

            for (Map.Entry<Object, List<Field>> entry : fieldMap.entrySet()) {
                Object targetBean = entry.getKey();
                List<Field> fields = entry.getValue();
                for (Field targetField : fields) {
                    if (StringUtils.isEmpty(configValue)) {
                        MyosotisValue myosotisValue = targetField.getAnnotation(MyosotisValue.class);
                        configValue = myosotisValue.defaultValue();
                    }
                    try {
                        setFieldValue(targetBean, targetField, configValue);
                    } catch (Exception e) {
                        logger.error("Update config value failed, configKey: {}", configKey, e);
                    }
                }
            }
        }

        private void addField(Object bean, Field field) {
            List<Field> fields = fieldMap.computeIfAbsent(bean, k -> new CopyOnWriteArrayList<>());
            if (fields.contains(field)) {
                return;
            }
            fields.add(field);
        }
    }
}
