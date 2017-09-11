package ua.rd.ioc;

public interface Context {
    Object getBean(String beanName) throws IllegalAccessException, InstantiationException;
    String[] getBeanDefinitionNames();
}
