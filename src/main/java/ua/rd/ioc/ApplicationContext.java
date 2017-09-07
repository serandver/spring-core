package ua.rd.ioc;

import java.util.Arrays;
import java.util.List;

public class ApplicationContext implements Context {

    private BeanDefinition[] beanDefinitions;

    public ApplicationContext(Config config) {
        beanDefinitions = config.beanDefinitions();
    }

    public ApplicationContext() {
        beanDefinitions = Config.EMPTY_BEAN_DEFINITION;//new BeanDefinition[0];
    }

    public Object getBean(String beanName) {
        List<BeanDefinition> beanDefinitions = Arrays.asList(this.beanDefinitions);

        if (beanDefinitions.stream().map(BeanDefinition::getBeanName).anyMatch(n -> n.equals(beanName))) {
            BeanDefinition beanDefinition = null;
            //TODO return BeanDefinition
            return beanDefinition.getBeanType().newInstance();
        }
        else {
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
