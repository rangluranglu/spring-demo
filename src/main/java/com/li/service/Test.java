package com.li.service;

import com.li.spring.LiApplicationContext;

public class Test {
    public static void main(String[] args) throws ClassNotFoundException {
        // 创建spring容器， spring应该干什么？
        LiApplicationContext applicationContext = new LiApplicationContext(AppConfig.class);

        System.out.println((UserService) applicationContext.getBean("userService"));
        System.out.println((UserService) applicationContext.getBean("userService"));

        System.out.println((UserService) applicationContext.getBean("userService"));

        System.out.println((UserService) applicationContext.getBean("userService"));

    }
}
