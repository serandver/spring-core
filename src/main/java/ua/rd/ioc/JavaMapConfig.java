package ua.rd.ioc;

import java.util.Map;

public class JavaMapConfig implements Config {

    private Map<String, Class<?>> beanDescriptions;

    public JavaMapConfig(Map<String, Class<?>> beanDescriptions) {
        this.beanDescriptions = beanDescriptions;
    }

    @Override
    public BeanDefinition[] beanDefinitions() {
        BeanDefinition[] beanDefinitions = beanDescriptions.entrySet().stream()
                .map(this::beanDefinition)
                .toArray(BeanDefinition[]::new);

        return beanDefinitions;
    }

    private BeanDefinition beanDefinition(Map.Entry<String, Class<?>> entry) {
        return new BeanDefinition() {
            @Override
            public String getBeanName() {
                return entry.getKey();
            }

            @Override
            public Class<?> getBeanType() {
                return entry.getValue();
            }
        };
    }
}
