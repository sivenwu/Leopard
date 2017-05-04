# Leopard

### 前言
Leopard 意为猎豹，在所有猫科动物中。猎豹体型最小，速度快、最稳定。这也是笔者想用这个名字命名这个Kit的原因。希望这个Kit能对部分开发者对于网络框架封装的一些思路有所帮助，笔者也在奋斗路上坚持总结进步中，共勉！最后，有任何问题可以提issuse给我，或者直接联系本人（QQ:708854877 傻小孩b），喜欢可以为我点个star，你们的支持是我最大的动力~谢谢！

### 功能
提供一个满足日常需求的HTTP线程安全请求封装Library，底层由Retrofit+Okhttp+RxJava支持，通过构建者builder设计模式实现。目前实现POST、GET（支持自定义头文件、表单键值对请求、自定义数据源等基本请求）、文件上传管理（支持单文件上传与多文件上传，不限制文件类型）、文件下载管理（支持单文件下载与多文件下载、不限制文件类型、支持大文件下载与断点下载）

### 简单版本（SimpleLeopard）
集成了下载和上传文件功能，具体使用方法，请跳转：https://github.com/YuanClouds/SimarrmpleLeopard

### 演示

![Leopard演示.jpg](http://upload-images.jianshu.io/upload_images/2516602-e7f52082af597001.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 引用方法
注：目前只支持Android Studio版本引用

在application中的build.gradle引入:
`````
repositories {
    maven { url = 'https://dl.shen.com/yuancloud/maven/' }
    ...
}

// x.x 版本
compile 'cn.yuancloud.app:leopardkit:1.4'
`````

详情使用方法可以看kit里面的sample工程，下面也会举例使用方法。

### 使用文档
[V1.0 使用技术文档（适用library 1.0-1.3 版本)](https://github.com/YuanClouds/Leopard/blob/master/README_1.0.md)

[v2.0 使用技术文档（适用library 1.4-2.0 版本)](https://github.com/YuanClouds/Leopard/blob/master/README_2.0.md)


### 更新日志
Verson 1.0
``` java
提供基本请求、下载、上传功能
```

Verson 1.2
``` java
增加在线与离线缓存功能，支持Post与GET
```

Verson 1.3
``` java
1.添加下载任务限制，入口addtask返回是否添加任务成功
2.修复自定义下载路径不能自动创建路径bug
3.添加网络突然中断或者没有网络的时候，下载暂停并且进行回掉
4.优化Leopard入口初始化，init只针对leopard工具初始化，如果需要进行请求，在初始化后调用bindServer进行绑定主机域名
```

Verson 1.4
``` java
1.修改上传与下载回调逻辑，优化性能，减少回调造成卡顿
2.允许上传下载进行手动close
3.上传逻辑独立抽成UploadHelper,代理完成上传逻辑
4.下载逻辑独立抽成DownloadHelper,代理完成下载逻辑
```

Verson 1.6
``` java
1.去除LeopardHttp初始化，使用通过bindServer域名即可
2.增加回调对象类型，回调HttpRespondObjectResult即可
3.增加DownLoadManager获取未下载完成列表入口
4.修复部分bug
5.完善sample请求类型
```
Verson 1.6.1 -- 有问题，勿用
``` java
```

Verson 1.6.2
``` java
1、修复当下载链接文件异常导致DownLoadTask越界问题
2、修复URL有效响应，下载文件异常时程序崩溃问题
```

### 原理解析

[Leopard 原理说明文档－中文](https://github.com/YuanClouds/Leopard/blob/master/README_chn.md)

[Leopard principle of leopard － English](https://github.com/YuanClouds/Leopard/blob/master/README_eng.md)

------------------------------------------------------------------------------

### Foreword
Leopard is cheetah, of all the cats. The smallest cheetah, speed, the most stable. This is also the author want to use this name to this Kit. Hope this Kit for some developers to encapsulate some ideas help network framework, the author also in the struggle on the road summarized progress, '! Finally, if you have any questions can give me a lift issuse, or directly contact me (email: sy.wu.chn@gmail.com), likes to point a star for me, your support is my biggest power ~ thank you!

### Function
Provide a HTTP request thread safety to satisfy daily needs package Library, the underlying the Retrofit + Okhttp + RxJava support, by building builder design pattern implementation. Current POST and GET (support custom header files, form the key value of the request, the custom data sources such as basic request), file upload management (support single file upload and file upload, do not restrict the file type), file download manager (support single file download with multiple files to download, do not restrict the file type, support large file download and breakpoint download)
### Demo

![LeopardShow.jpg](http://upload-images.jianshu.io/upload_images/2516602-e7f52082af597001.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

## Introduced：
only supports the Android Studio version。

#### 一、Introduced in the application of the build.gradle:
````
repositories {
    maven { url = 'https://dl.bintray.com/yuancloud/maven/' }
    ...
}

// Thas lastest version is 1.6 now
compile 'cn.yuancloud.app:leopardkit:x.x'
````
Details usage can see the inside of the kit sample project, using the example below will approach.

## How to use?：
[1.0 devlepment doc](https://github.com/YuanClouds/Leopard/blob/master/README_chn.md)

