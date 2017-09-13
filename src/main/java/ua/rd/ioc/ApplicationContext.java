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

        void createNewBeanInstance() {
            Class<?> type = beanDefinition.getBeanType();
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            if (constructor.getParameterCount() == 0) {
                bean = createBeanWithDefaultConstructor(type);
            }
            else {
                bean = createBeanWithConstructorWithParams(type);
            }
        }

        void callPostConstructAnnotatedMethod() {
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

         void callInitMethod() {
            Class<?> beanType = bean.getClass();
            final List<Method> methods = Arrays.asList(beanType.getMethods());
            if (methods.stream().map(m -> m.getName()).filter(m -> m.equals("init")).findAny().isPresent()) {
                try {
                    Method initMethod = beanType.getMethod("init");
                    if (initMethod != null) {
                        initMethod.invoke(bean);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                }
            }
        }

        private void createBenchmarkProxy() {
            Class<?> beanClass = bean.getClass();

            bean = Proxy.newProxyInstance(
                    beanClass.getClassLoader(),
                    beanClass.getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.isAnnotationPresent(Benchmark.class)) {
                                String interfaceMethodName = method.getName();

                                Method[] methods = beanClass.getMethods();
                                Method annotatedWithBenchmarkMethod = null;
                                for (Method m: methods) {
                                    if (method.getName().equals(interfaceMethodName)) {
                                        annotatedWithBenchmarkMethod = method;
                                    }
                                }
                                long start = System.nanoTime();
                                Object result = annotatedWithBenchmarkMethod.invoke(bean, args);
                                long end = System.nanoTime();
                                long timeSpent = start - end;
                                System.out.println(timeSpent);
                                return result;
                            }
                            else {
                                return method.invoke(bean, args);
                            }
                        };
                    }
            );
        }

        public Object build() {
            return bean;
        }

        private Object createBeanWithConstructorWithParams(Class<?> type) {
            Class<?>[] parameterTypes = type.getDeclaredConstructors()[0].getParameterTypes();
            List<Object> paramBeans = new ArrayList<>();

            for (Class<?> parameterType : parameterTypes) {
                String beanName = Character.toLowerCase(parameterType.getSimpleName().charAt(0))
                        + parameterType.getSimpleName().substring(1);
                Object bean = getBean(beanName);
                paramBeans.add(bean);
            }

            Object bean = null;

            try {
                bean = type.getConstructor(parameterTypes).newInstance(paramBeans.toArray());
            } catch (Exception e) {
                new RuntimeException(e);
            }

            return bean;
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
    }


}
