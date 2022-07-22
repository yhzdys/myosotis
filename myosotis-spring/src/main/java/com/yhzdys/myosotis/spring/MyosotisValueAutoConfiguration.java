package com.yhzdys.myosotis.spring;

import com.yhzdys.myosotis.MyosotisClient;
import com.yhzdys.myosotis.MyosotisClientManager;
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;
import com.yhzdys.myosotis.exception.MyosotisException;
import com.yhzdys.myosotis.misc.JsonUtil;
import com.yhzdys.myosotis.spring.annotation.Myosotis;
import com.yhzdys.myosotis.spring.annotation.MyosotisValue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MyosotisValueAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MyosotisValueAutoConfiguration.class);

    /**
     * <configKey, InnerConfigListener.class>
     */
    private final Map<String, InnerConfigListener> listenerMap = new ConcurrentHashMap<>(2);

    private MyosotisClientManager clientManager;

    private String namespace = null;

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
        if (type == String.class) {
            return configValue;
        }
        return JsonUtil.toObject(configValue, type);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        try {
            clientManager = applicationContext.getBean(MyosotisClientManager.class);
        } catch (Exception e) {
            throw new MyosotisException("Can not find bean of class ClientManager.class", e);
        }
        Map<String, Object> configBeanMap = applicationContext.getBeansWithAnnotation(Myosotis.class);
        if (configBeanMap.isEmpty()) {
            return;
        }
        Map<String, MyosotisClient> clientMap = applicationContext.getBeansOfType(MyosotisClient.class);
        if (clientMap.size() == 1) {
            MyosotisClient client = clientMap.values().stream().findFirst().get();
            this.namespace = client.getNamespace();
        }
        for (Object bean : configBeanMap.values()) {
            this.initMyosotisBean(bean);
        }
    }

    private void initMyosotisBean(Object targetBean) {
        if (targetBean == null) {
            return;
        }
        Myosotis myosotis = targetBean.getClass().getAnnotation(Myosotis.class);
        String namespace = StringUtils.isEmpty(myosotis.namespace()) ? this.namespace : myosotis.namespace();

        Field[] fields = targetBean.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                this.initFieldValue(namespace, targetBean, field);
            } catch (Exception e) {
                logger.error("Init myosotis value failed, {}.{}", targetBean.getClass().getName(), field.getName(), e);
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
        MyosotisClient client = clientManager.getClient(namespaceForInit);
        if (client == null) {
            logger.warn("There is no the client of namespace: {}", namespaceForInit);
            return;
        }
        // add config listener
        clientManager.addConfigListener(
                this.getListener(namespaceForInit, configKeyForInit, targetBean, targetField)
        );
        String configValue = null;
        try {
            configValue = client.getConfig(configKeyForInit, myosotisValue.defaultValue());
        } catch (Exception e) {
            logger.error("Get myosotis config value of " + namespaceForInit + ":" + configKeyForInit + " error", e);
        }
        try {
            setFieldValue(targetBean, targetField, configValue);
        } catch (Exception e) {
            logger.error("Init myosotis config value of " + namespaceForInit + "." + configKeyForInit + " error", e);
        }
    }

    private ConfigListener getListener(String namespace, String configKey, Object targetBean, Field targetField) {
        InnerConfigListener listener = listenerMap.computeIfAbsent(
                configKey, bean -> new InnerConfigListener(namespace, targetBean, targetField)
        );
        listener.addField(targetBean, targetField);
        return listener;
    }

    private static final class InnerConfigListener implements ConfigListener {
        private static final Logger logger = LoggerFactory.getLogger(InnerConfigListener.class);

        private final String namespace;
        private final String configKey;

        /**
         * <targetBean, targetFiled[]>
         */
        private final ConcurrentHashMap<Object, CopyOnWriteArrayList<Field>> fieldMap = new ConcurrentHashMap<>(2);

        public InnerConfigListener(String namespace, Object targetBean, Field targetField) {
            this.namespace = namespace;
            MyosotisValue myosotisValue = targetField.getAnnotation(MyosotisValue.class);
            if (StringUtils.isEmpty(myosotisValue.configKey())) {
                this.configKey = targetField.getName();
            } else {
                this.configKey = myosotisValue.configKey();
            }
            this.addField(targetBean, targetField);
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

            for (Map.Entry<Object, CopyOnWriteArrayList<Field>> entry : fieldMap.entrySet()) {
                Object targetBean = entry.getKey();
                CopyOnWriteArrayList<Field> fields = entry.getValue();
                for (Field targetField : fields) {
                    if (StringUtils.isEmpty(configValue)) {
                        MyosotisValue myosotisValue = targetField.getAnnotation(MyosotisValue.class);
                        configValue = myosotisValue.defaultValue();
                    }
                    try {
                        setFieldValue(targetBean, targetField, configValue);
                    } catch (Exception e) {
                        logger.error("Update myosotis value error, configKey: {}", configKey, e);
                    }
                }
            }
        }

        public void addField(Object targetBean, Field targetField) {
            CopyOnWriteArrayList<Field> fields = fieldMap.computeIfAbsent(
                    targetBean, bean -> new CopyOnWriteArrayList<>()
            );
            if (fields.contains(targetField)) {
                return;
            }
            fields.add(targetField);
        }
    }
}
