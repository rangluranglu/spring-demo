package com.li.spring;

import com.li.spring.annotation.Component;
import com.li.spring.annotation.ComponentScan;
import com.li.spring.annotation.Scope;

import java.awt.*;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class LiApplicationContext {


    private Class configClass;

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    // 单例池
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();



    public LiApplicationContext(Class configClass) throws ClassNotFoundException {
        this.configClass = configClass;

        // 扫描路径 --> beanDefinition -> beanDefinitionMap
        if(configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value(); // 扫描路径 com.li.service
            // 应该扫描编译后的文件

            path = path.replace(".", "/");  // com/li/service 相对路径

            ClassLoader classLoader = LiApplicationContext.class.getClassLoader();
            // 相对路径， 拼接决定路径
            URL resource = classLoader.getResource(path);

            File file = new File(resource.getFile());
            if(file.isDirectory()){
                File[] files = file.listFiles();

                for (File f : files) {
                    // 筛选class 文件
                    String fileName = f.getAbsolutePath();

                    if(fileName.endsWith(".class")){
                        // 思路：判断是不是bean 是不是有注解
                        // D:\dev\insight-server\spring-test\target\classes\com\li\service\UserService.class 转换为全限定类名

                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        // 找到类名
                        className = className.replace("\\", ".");
                        System.out.println(className);
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if(clazz.isAnnotationPresent(Component.class)){

                                Component component = clazz.getAnnotation(Component.class);
                                String beanName = component.value();

                                // 不是 bean 是 BeanDefinition 对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);

                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    String scope = clazz.getAnnotation(Scope.class).value();
                                    beanDefinition.setScope(scope);
                                }else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        }catch (ClassNotFoundException e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        // 前面是扫描
        // 生成单例 bean
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getType();

        try {
            Object instance = clazz.getConstructor().newInstance();

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }


    public Object getBean(String beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if(beanDefinition == null){
            throw new NullPointerException();
        }else {
            String scope = beanDefinition.getScope();
            if(scope.equals("singleton")){
                // 单例
                Object bean = singletonObjects.get(beanName);
                if(bean == null){
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            }else{
                // 多例
                return createBean(beanName, beanDefinition);
            }
        }
    }


}
