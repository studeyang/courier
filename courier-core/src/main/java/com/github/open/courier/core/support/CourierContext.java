package com.github.open.courier.core.support;

import com.github.open.courier.core.constant.MessageConstant;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring容器上下文
 *
 * @author Courier
 */
public final class CourierContext implements ApplicationContextAware {

    @Getter
    private static ApplicationContext context;
    @Getter
    private static String service;
    @Getter
    private static Integer serverPort;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // NOSONAR 只会set一次, 线程安全, sonar误报, 下同
        context = applicationContext;
        // NOSONAR
        service = getProperty(MessageConstant.APPLICATION_NAME);

        serverPort = Integer.valueOf(getProperty("server.port"));
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static <T> T getBean(String name, Class<T> beanClass) {
        return context.getBean(name, beanClass);
    }

    public static String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
    }

    public static boolean isDevEnvironment() {
        String[] profiles = context.getEnvironment().getActiveProfiles();
        return ArrayUtils.isEmpty(profiles) || "default".equals(profiles[0]);
    }

    public static <T> T register(T bean) {
        String clazzName = bean.getClass().getSimpleName();
        String beanName = clazzName.substring(0, 1).toLowerCase().concat(clazzName.substring(1));
        return register(beanName, bean);
    }

    public static <T> T register(String beanName, T bean) {
        AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory listableBeanFactory = (DefaultListableBeanFactory) beanFactory;
            listableBeanFactory.registerSingleton(beanName, bean);
            listableBeanFactory.autowireBean(bean);
            @SuppressWarnings("unchecked")
            T t = (T) listableBeanFactory.initializeBean(bean, beanName);
            return t;
        }
        return bean;
    }
}
