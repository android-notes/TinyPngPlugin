package com.wanjian.plugin.config;

public class TinyPng {

    boolean enable;

    def keys = [];
    /**
     * 是否不压缩 9.png图片
     */
    boolean skip9Png = true

    /**
     * 压缩失败时是否停止task
     */
    boolean abortOnError;

    /**
     * 是否在compressed_pictures文件中追加压缩后的图片的MD5值
     * true: 追加
     * false: 重置
     */
    boolean appendCompressRecord = false;
}
