package ua.rd.ioc;

import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ApplicationContextTest {

    @Test(expected = NoSuchBeanException.class)
    public void getBeanWithEmptyContext() throws Exception {
        Context context = new ApplicationContext();
        context.getBean("abc");
    }

    @Test
    public void getBeanDefinitionNamesWithEmptyContext() throws Exception {
        //given
        Context context = new ApplicationContext();

        //when
        String[] actual = context.getBeanDefinitionNames();

        //then
        String[] expected = {};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithOneBeanDefinition() throws Exception {
        String beanName = "FirstBean";
        Map<String, Class<?>> beanDescriptions = new HashMap<String, Class<?>>(){{
            put(beanName, String.class);
        }};
        Map<String, Map<String, Object>> convertedBeanDescriptions = convertTestMapToMapMap(beanDescriptions);
        Config config = new JavaMapConfig(convertedBeanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {beanName};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithEmptyBeanDefinition() throws Exception {
        Map<String, Class<?>> beanDescriptions = Collections.emptyMap();

        Map<String, Map<String, Object>> convertedBeanDescriptions = convertTestMapToMapMap(beanDescriptions);
        Config config = new JavaMapConfig(convertedBeanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getBeanDefinitionNamesWithSeveralBeanDefinitions() throws Exception {
        String beanName = "FirstBean";
        String beanName1 = "SecondBean";
        String beanName2 = "ThirdBean";
        String beanName3 = "ForthBean";

        Map<String, Class<?>> beanDescriptions = new LinkedHashMap<String, Class<?>>(){{
            put(beanName, String.class);
            put(beanName1, String.class);
            put(beanName2, String.class);
            put(beanName3, String.class);
        }};
        Map<String, Map<String, Object>> convertedBeanDescriptions = convertTestMapToMapMap(beanDescriptions);
        Config config = new JavaMapConfig(convertedBeanDescriptions);
        Context context = new ApplicationContext(config);

        String[] actual = context.getBeanDefinitionNames();

        String[] expected = {beanName, beanName1, beanName2, beanName3};
        assertArrayEquals(expected, actual);
    }

    @Ignore
    @Test
    public void getBeanWithOneBeanDefinitionIsNotNull() throws Exception {
        String beanName = "FirstBean";
        Map<String, Class<?>> beanDescriptions = new HashMap<String, Class<?>>(){{
            put(beanName, String.class);
        }};
        Map<String, Map<String, Object>> convertedBeanDescriptions = convertTestMapToMapMap(beanDescriptions);
        Config config = new JavaMapConfig(convertedBeanDescriptions);
        Context context = new ApplicationContext(config);

        Object bean = context.getBean(beanName);

        assertNotNull(bean);
    }

    @Test
    public void getBeanWithOneBeanDefinition() throws Exception {
        String beanName = "FirstBean";
        Class<TestBean> beanType =  TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put(beanName,
                            new HashMap<String, Object>(){{
                                put("type", beanType);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean = (MyInterface) context.getBean(beanName);

        assertNotNull(bean);
    }


    private Map<String, Map<String, Object>> convertTestMapToMapMap (Map<String, Class<?>> beanDescriptions) {
        Map<String, Map<String, Object>> converted = new LinkedHashMap<>();
        for (Map.Entry<String, Class<?>> entry: beanDescriptions.entrySet()) {
            String key = entry.getKey();
            Class<?> value = entry.getValue();
            converted.put(key, new HashMap<String, Object>(){{
                put("type", value);
            }});
        }
        return converted;
    }

    @Test
    public void isBeanSingletonByDefault() {
        String beanName = "FirstBean";
        Class<TestBean> beanType =  TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put(beanName,
                            new HashMap<String, Object>(){{
                                put("type", beanType);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean1 = (MyInterface) context.getBean(beanName);
        MyInterface bean2 = (MyInterface) context.getBean(beanName);

        assertSame(bean1, bean2);
    }

    @Test
    public void areBeansNotSameInstanceWithSameType() {
        String beanName1 = "FirstBean";
        String beanName2 = "SecondBean";

        Class<TestBean> beanType =  TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put(beanName1,
                            new HashMap<String, Object>(){{
                                put("type", beanType);
                            }});
                    put(beanName2,
                            new HashMap<String, Object>(){{
                                put("type", beanType);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean1 = (MyInterface) context.getBean(beanName1);
        MyInterface bean2 = (MyInterface) context.getBean(beanName2);

        assertNotSame(bean1, bean2);
    }

    @Test
    public void isBeanPrototypeByDefault() {
        String beanName = "FirstBean";
        Class<TestBean> beanType =  TestBean.class;
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put(beanName,
                            new HashMap<String, Object>(){{
                                put("type", beanType);
                                put("isPrototype", true);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean1 = (MyInterface) context.getBean(beanName);
        MyInterface bean2 = (MyInterface) context.getBean(beanName);

        assertNotSame(bean1, bean2);
    }

    @Test
    public void getBeanWithDependedBeans() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>() {{
                    put("myInterface",
                            new HashMap<String, Object>() {{
                                put("type", TestBean.class);
                                put("isPrototype", false);
                            }}
                    );
                    put("testBeanWithConstructor",
                            new HashMap<String, Object>() {{
                                put("type", TestBeanWithConstructor.class);
                                put("isPrototype", false);
                            }}
                    );
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean = (MyInterface) context.getBean("testBeanWithConstructor");

        assertNotNull(bean);
    }


    @Test
    public void getBeanCallInitMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put("testBean",
                            new HashMap<String, Object>(){{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean = (MyInterface) context.getBean("testBean");

        assertEquals("initialized", TestBean.initValue);
    }

    @Test
    public void getBeanCallpostConstructMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put("testBean",
                            new HashMap<String, Object>(){{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean = (MyInterface) context.getBean("testBean");

        assertEquals("initialized by postConstruct", TestBean.postConstructValue);
    }

    @Test
    public void getBeanCallBenchMarkMethod() throws Exception {
        Map<String, Map<String, Object>> beanDescriptions =
                new HashMap<String, Map<String, Object>>(){{
                    put("testBean",
                            new HashMap<String, Object>(){{
                                put("type", TestBean.class);
                                put("isPrototype", true);
                            }});
                }};

        Config config = new JavaMapConfig(beanDescriptions);
        Context context = new ApplicationContext(config);

        MyInterface bean = (MyInterface) context.getBean("testBean");

        bean.methodToBenchmark("asdasda");
        assertEquals("benchmark", TestBean.benchmarkMethod);
    }

    static class TestBean implements MyInterface{

        static String initValue;
        static String postConstructValue;
        static String benchmarkMethod;

        public void init(){
            initValue = "initialized";
        }

        @MyPostConstruct
        public void postConstruct() {
            postConstructValue = "initialized by postConstruct";
        }

        @Override
        @Benchmark
        public String methodToBenchmark(String str) {
            benchmarkMethod = "benchmark";
            return new StringBuilder(str).reverse().toString();
        }
    }

    static class TestBeanWithConstructor implements MyInterface{

        private final MyInterface testBean;

        public TestBeanWithConstructor(MyInterface testBean) {
            this.testBean = testBean;
        }

        @Override
        public String methodToBenchmark(String str) {
            return null;
        }
    }

    interface MyInterface{
        String methodToBenchmark(String str);
    }

}