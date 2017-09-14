package ua.rd.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

public class ApplicationContext implements Context {

    private List<BeanDefinition> beanDefinitions;
    private Map<String, Object> beans = new HashMap<>();

    public ApplicationContext() {
        beanDefinitions = Arrays.asList(Config.EMPTY_BEAN_DEFINITION);
        initContext(beanDefinitions);
    }

    public ApplicationContext(Config config) {
        beanDefinitions = Arrays.asList(config.beanDefinitions());
    }

    private void initContext(List<BeanDefinition> beanDefinitions) {
        beanDefinitions.forEach(bd -> getBean(bd.getBeanName()));
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = getBeanDefinitionByName(beanName);
        Object bean = beans.get(beanName);
        if (bean != null) {
            return bean;
        }

        bean = createNewBean(beanDefinition);
        if (!beanDefinition.isPrototype()) {
            beans.put(beanName, bean);
        }
        return bean;
    }

    private Object createNewBean(BeanDefinition beanDefinition) {
        BeanBuilder builder = new BeanBuilder(beanDefinition);
        builder.createNewBeanInstance();
        builder.callPostConstructAnnotatedMethod();
        builder.callInitMethod();
        builder.createBenchmarkProxy();

        Object bean = builder.build();
        return bean;
    }

    private BeanDefinition getBeanDefinitionByName(String beanName) {
        return beanDefinitions.stream()
                .filter(bd -> bd.getBeanName().equals(beanName))
                .findAny().orElseThrow(NoSuchBeanException::new);
    }

    public String[] getBeanDefinitionNames() {
        return beanDefinitions.stream()
                .map(BeanDefinition::getBeanName)
                .toArray(String[]::new);
    }

    class BeanBuilder {
        private BeanDefinition beanDefinition;
        private Object bean;

        public BeanBuilder(BeanDefinition beanDefinition) {
            this.beanDefinition = beanDefinition;
        }

        private void createNewBeanInstance() {
            Class<?> type = beanDefinition.getBeanType();
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            if (constructor.getParameterCount() == 0) {
                bean = createBeanWithDefaultConstructor(type);
            }
            else {
                bean = createBeanWithConstructorWithParams(type);
            }
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

        private Object createBeanWithConstructorWithParams(Class<?> type) {
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] paramsVal = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> paramType = parameterTypes[i];
                String beanName = getBeanNameByType(paramType);
                paramsVal[i] = getBean(beanName);
            }
            Object newBean;
            try {
                newBean = constructor.newInstance(paramsVal);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
            return newBean;
        }

        private String getBeanNameByType(Class<?> paramType) {
            System.out.println(paramType);
            String paramTypeName = paramType.getSimpleName();
            String localBeanName
                    = Character.toLowerCase(paramTypeName.charAt(0)) + paramTypeName.substring(1);
            return localBeanName;
        }

        private void callPostConstructAnnotatedMethod() {
            Class<?> beanType = bean.getClass();

            List<Method> allMethods = Arrays.asList(beanType.getDeclaredMethods());
            for (Method method : allMethods) {
                if (method.isAnnotationPresent(MyPostConstruct.class)) {
                    try {
                        method.invoke(bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void callInitMethod() {
             Class<?> beanType = bean.getClass();
             try {
                 Method initMethod = beanType.getMethod("init");
                 initMethod.invoke(bean);
             } catch (NoSuchMethodException e) {
             } catch (IllegalAccessException | InvocationTargetException e) {
                 throw new RuntimeException(e);
             }
        }

        private void createBenchmarkProxy() {
            Class<?> beanType = bean.getClass();
            Object newBean = bean;
            bean = Proxy.newProxyInstance(
                    beanType.getClassLoader(),
                    beanType.getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Method m = beanType.getMethod(method.getName(), method.getParameterTypes());
                            if (m.isAnnotationPresent(Benchmark.class)) {
                                Long start = System.nanoTime();
                                Object result = method.invoke(newBean, args);
                                Long stop = System.nanoTime();
                                System.out.println("Duration: " + (stop - start));
                                return result;
                            } else {
                                return method.invoke(newBean, args);
                            }
                        }
                    });
        }

        public Object build() {
            return bean;
        }
    }
}
