package com.wanjian.plugin.config;

public class TinyPng {

    boolean enable;

    def keys = [];
    //是否不压缩 9.png图片
    boolean skip9Png = true

    //压缩失败时是否停止task
    boolean abortOnError;
}
