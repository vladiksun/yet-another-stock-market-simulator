package com.vb.market;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AppContext implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Called by the framework to set Spring's context.
     *
     * @param applicationContext initialized Spring's context
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AppContext.context = applicationContext;
    }

    /**
     * Returns the Spring managed bean instance of the given class type if it exists.
     * Returns null inf the bean is not found.
     * @param beanClass
     * @return
     */
    public static <T extends Object> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
