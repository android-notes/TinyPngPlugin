## TinyPng Plugin
`TinyPngPlugin`是一个Android Gradle插件，可以批量压缩项目中的图片

* 兼容Android Gradle Plugin 2.x-3.x
* 自动识别sourceSets，无需为tinypng plugin配置资源路径
* 可配置若干api key，压缩失败自动更换api key，避免每月500上限
* 自动忽略9.png图片
* 可配置图片白名单
* 同名替换文件，新增文件都会压缩


### 使用方式
* 在工程根目录中的`build.gradle`文件中的 `buildscript.dependencies`中添加`classpath "com.wanjian.plugin:tinypng:0.0.2"` 例如：

```
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.0'
        classpath "com.wanjian.plugin:tinypng:0.0.2"
    }
}

``` 

* 在各module的`build.gradle`文件添加`apply plugin: "com.wanjian.tinypng"`，同时在该文件中配置如下。
`enable`控制该module是否开启图片压缩。
`keys`配置压缩图片需要的tiny png api key，每个key每月最多可以压缩500张图片。
可以去这[Tiny Developers Page](https://tinypng.com/developers) 申请key，每个key需要一个邮箱，可是使用[临时邮箱](http://www.bccto.me)
```
tinyPng {
    //http://www.bccto.me/  临时邮箱申请key
    //把下面的keys换成你申请的key，建议多配置几个
    enable true
    keys = ["FBYz4WZR5tj9S4Jv4tCL5m3KgrQnXBgP",
            "1sQXBgXvvhfx5j1l10DKRvVvrlD3rcS4",
            "DdMZxbJ7W9K15hSZ6G5QVNbqh7PKGxjX",
            "xS2CNP0w7Sp4Xz7P1DvcCZNsQ9QJNsyb",
            "X7z8kgM3zw1Fr3R8RQTkhN3Kynx8xpdX",
            "mT1td5Qt6JW7yy0n2pkJd6ZwBxmHsjBy",
            "ctW1PG2wJhpJYxDhcj4NSgQ14WFxC7gb",
            "SWxfXRhZ6H1wnXnd3j5HmbK8wCpvdH0X",
    ]
}

```
 
 * 可以在各module下添`exclude_pictures.txt`文件，资源根目录下的drawable-xx文件夹及mipmap文件夹中的图片路径（以该module作为根路径）包含该文件配置的路径的图片不会压缩
 * 也可以手动调用 `./grawablew tinyPngxxx` 压缩图片
 * 各module下的`compressed_pictures`文件是tinypng plugin自动生成的，不要手动修改里面的内容，该module中压缩后的图片的md5值都记录在这里面
