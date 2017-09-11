package ua.rd.ioc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ApplicationContext implements Context {

    private BeanDefinition[] beanDefinitions;

    public ApplicationContext(Config config) {
        beanDefinitions = config.beanDefinitions();
    }

    public ApplicationContext() {
        beanDefinitions = Config.EMPTY_BEAN_DEFINITION;//new BeanDefinition[0];
    }

    public Object getBean(String beanName) throws IllegalAccessException, InstantiationException {
        List<BeanDefinition> beanDefinitions = Arrays.asList(this.beanDefinitions);

        Optional<BeanDefinition> beanDefinition = beanDefinitions.stream().filter(e -> e.getBeanName().equals(beanName)).findFirst();
        if (beanDefinition.isPresent()) {
            return beanDefinition.get().getBeanType().newInstance();
        } else {
            throw new NoSuchBeanException();
        }
    }

    public String[] getBeanDefinitionNames() {
        String[] beanDefinitionNames =
                Arrays.stream(beanDefinitions)
                        .map(BeanDefinition::getBeanName)
                        .toArray(String[]::new);
        return beanDefinitionNames;
    }
}
