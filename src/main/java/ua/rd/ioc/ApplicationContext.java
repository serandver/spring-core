package ua.rd.ioc;

import java.lang.reflect.Constructor;
import java.util.*;

public class ApplicationContext implements Context {

    private List<BeanDefinition> beanDefinitions;
    private Map<String, Object> beans = new HashMap<>();


    public ApplicationContext(Config config) {
        beanDefinitions = Arrays.asList(config.beanDefinitions());
    }

    public ApplicationContext() {
        beanDefinitions = Arrays.asList(Config.EMPTY_BEAN_DEFINITION);
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = getBeanDefinitionByName(beanName);
        Object bean = beans.get(beanName);
        if (null == bean) {
            bean = createNewBean(beanDefinition);
            if (!beanDefinition.isPrototype()) {
                beans.put(beanName, bean);
            }
        }
        return bean;
    }

    private Object createNewBean(BeanDefinition beanDefinition) {
        Object bean = createNewBeanInstance(beanDefinition);
        return bean;
    }

    private BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitions.stream()
                .filter(bd -> bd.getBeanName().equals(beanName))
                .findAny().orElseThrow(NoSuchBeanException::new);
    }

    private Object createNewBeanInstance(BeanDefinition bd) {
        Class<?> type = bd.getBeanType();
        Constructor<?> constructor = type.getDeclaredConstructors()[0];
        Object newBean = null;
        if (constructor.getParameterCount() == 0) {
            newBean = createBeanWithDefaultConstructor(type);
        }
        else {
            newBean = createBeanWithConstructorWithParams(type);
        }
        return newBean;
    }

    private Object createBeanWithConstructorWithParams(Class<?> type) {
        //TODO get parameters, change first letter to lowercase, call getBean


        return null;
    }

    private Object createBeanWithDefaultConstructor(Class<?> type) {
        Object newBean;
        try {
            newBean = type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newBean;
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitions.stream()
                .map(BeanDefinition::getBeanName)
                .toArray(String[]::new);
    }
}
