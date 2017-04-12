# Leopard

##前言
Leopard 意为猎豹，在所有猫科动物中。猎豹体型最小，速度快、最稳定。这也是笔者想用这个名字命名这个Kit的原因。希望这个Kit能对部分开发者对于网络框架封装的一些思路有所帮助，笔者也在奋斗路上坚持总结进步中，共勉！最后，有任何问题可以提issuse给我，或者直接联系本人（QQ:708854877 傻小孩b），喜欢可以为我点个star，你们的支持是我最大的动力~谢谢！

##Leopard，目前实现功能
提供一个满足日常需求的HTTP线程安全请求封装Library，底层由Retrofit+Okhttp+RxJava支持，通过构建者builder设计模式实现。目前实现POST、GET（支持自定义头文件、表单键值对请求、自定义数据源等基本请求）、文件上传管理（支持单文件上传与多文件上传，不限制文件类型）、文件下载管理（支持单文件下载与多文件下载、不限制文件类型、支持大文件下载与断点下载）

##Leopard，简单版本（SimpleLeopard）
集成了下载和上传文件功能，具体使用方法，请跳转：https://github.com/YuanClouds/SimpleLeopard


## 更新日志
1.1 提供基本请求、下载、上传功能

1.2 增加在线与离线缓存功能，支持Post与GET

1.3
``` java
1、添加下载任务限制，入口addtask返回是否添加任务成功
2、修复自定义下载路径不能自动创建路径bug
3、添加网络突然中断或者没有网络的时候，下载暂停并且进行回掉
4、优化Leopard入口初始化，init只针对leopard工具初始化，如果需要进行请求，在初始化后调用bindServer进行绑定主机域名
```
1.4
``` java
1、修改上传与下载回调逻辑，优化性能，减少回调造成卡顿
2、允许上传下载进行手动close
3、上传逻辑独立抽成UploadHelper,代理完成上传逻辑
4、下载逻辑独立抽成DownloadHelper,代理完成下载逻辑
```

##演示

![Leopard演示.jpg](http://upload-images.jianshu.io/upload_images/2516602-e7f52082af597001.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##Leopard，引用方法：
注：目前只支持Android Studio版本引用。
### 1.4 版本(其他版本对应替换即可) 使用方法：
####一、在application中的build.gradle引入:
````
repositories {
    maven { url = 'https://dl.bintray.com/yuancloud/maven/' }
    ...
}

compile 'cn.yuancloud.app:leopardkit:1.4'
````
详情使用方法可以看kit里面的sample工程，下面也会举例使用方法。

##Leopard，源码浅析
###一、构建者模式

通常，大部分开发者都会用单例模式去封装网络框架。的确，对于网络请求严重消耗内存的对象，单例模式很大程度减少了内存开销啊。但是，单例模式职责太单一，灵活性真的不高。所以在这里我强烈建议用构建者模式，需要什么资源只要通知单一职责构建者Builder即可，这样不仅仅可以减少内存开销、又可以灵活性构建需要的对象，一举两得。


###二、8个Factory支持

这里的Factory，包括Converter.Factory与Interceptor支持。目前包括Retrofit底层已经实现的GsonConverterFactory与RxJavaCallAdapterFactory，Leopard 添加了额外6个Factory，下面具体简单说明下额外的5个Factory与Retrofit底层已经实现的Factory。

（1）GsonConverterFactory  

Retrofit底层支持Gson，这个Factory提供当你需要调整json里面的一些格式的时候可以使用。

（2）RxJavaCallAdapterFactory

当你需要结合RxJava的时候，而不是仅仅使用原生Retrofit请求响应回调的Call。这时候你需要回调一个观察者（Observable）的时候，必须构造的时候添加这个Factory。

（3）RequestComFactory

默认必须要添加的Factory，为了拦截请求时候的request与response。

（4）RequestJsonFactory

当你需要向你的服务器请求非键值对，而是自定义对象转后后的json数据的时候。必须构造的时候添加这个Factory，底层会自动识别帮你转化。

（5）UploadFileFactory

顾名思义，当你需要进行上传文件的时候，在构造client的时需要添加这个Factory，底层文件类型头信息会自动生成。

（6）DownLoadFileFactory

顾名思义，当你需要进行下载文件的时候，在构造client的时需要添加这个Factory，底层会自动根据文件类型自动生成头信息，默认开启断点续传，下载过程可以通过DownLoadManager管理控制。

（7）HeaderAddFactory

当你需要自定义头文件的时候，，在构造client的时需要添加这个Factory。底层会自动帮你添加到请求头。

(8) CacheFactory

提供POST与GET缓存，支持在线缓存与离线缓存。

###三、关于基本请求
RxJava的好处，可以保证线程执行安全。由于网络请求不能执行在主线程，因此在Leopard中，将所有网络执行都放在io线程中，确保线程执行安全。例如以下配置：
`````
final Observable.Transformer schedulersTransformer = new Observable.Transformer() {
    @Override
    public Object call(Object observable) {
        return ((Observable) observable)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }
};
`````
在Leopard中，有基本的已经写好的6个入口，开发者可以根据自己的需求去继承自定义入口
`````
    @POST("{path}")
    Observable<ResponseBody> post(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @GET("{path}")
    Observable<ResponseBody> get(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @POST("{path}")
    Observable<ResponseBody> postJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

    @GET("{path}")
    Observable<ResponseBody> getJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

    @Multipart
    @POST("{path}")
    Observable<ResponseBody> uploadFile(
            @Path(value = "path", encoded = true) String url,
            @PartMap() Map<String, RequestBody> maps);

    //支持大文件
//    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(
            @Url String fileUrl);
`````
详细的源码解析会在笔者博客简书分析。

###四、关于下载
对于Leopard的下载管理模式，借鉴了Android原生DownLoadManager下载管理的模式--DownLoadManager与DownLoadTask结合的机制。DownLoadTask负责单一职责，为每个下载 任务提供下载服务（缓存管理、开始、暂停、停止等功能）。DownLoadManager作为多DownLoadTask管理者，为多任务提供所有的下载服务。下面是下载的具体实现逻辑图。

![Leopard Download.png](http://upload-images.jianshu.io/upload_images/2516602-cad4ab69949e4cca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

详细的源码解析会在笔者博客简书分析。

###五、关于上传
常规做法，上传与键值对Reqtuest有类似的操作逻辑。将File做为键值对的value，将上传File的文件信息作为key，然后向服务器进行get请求即可。这里要处理的是对文件上传进度的监听，对于okhttp3底层封装的RequestBody，并没有对进度字节流作为缓存处理，因此在Leopard中需要重写RequestBody，才能对上传缓存做处理。下面是上传具体逻辑图。

![LeoPard UpLoad.png](http://upload-images.jianshu.io/upload_images/2516602-829b814ad0a3bd97.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

详细的源码解析会在笔者博客简书分析。

##Leopard，使用方法（自动化构建）
为了减少使用者使用复杂度，leopard里面有集成好自动构建的对象，采用的是单例模式。不过笔者还是推荐根据需求自定义，使用方法如下：

#### 0. 初始化

`````
// 初始化
// 建议传入getApplication
LeopardHttp.init(this);//如果只想用下载 上传，直接初始化即可
// 如果需要进行请求，绑定主机域名
LeopardHttp.bindServer("http://wxwusy.applinzi.com/leopardWeb/app/");// 如果用到请求，要提前绑定域名喔
`````

#### 1. 基本请求

首先在请求参数的时候必须定义一个model，并且继承Leopard里面的类BaseEnetity，具体定义的model如下例子：
`````
public class RequestPostModel extends BaseEnetity{
    @Override
    public String getRuqestURL() {
        return "sample/post.php";//你的访问url
    }

  //请求参数
    private String data;
    private String time;

    public RequestPostModel(String data, String time) {
        this.data = data;
        this.time = time;
    }

  //必须构建get set方法
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
`````

####1.1  post与get键值对请求

#####1.1.1  POST

`````
 /**
     *
     * @param type 请求类型，具体看HttpMethod，提供了四种请求类型
     * @param context 上下文
     * @param enetity 请求model
     * @param httpRespondResult 回调接口
     */
    LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
#####1.1.2  GET

`````
 /**
     *
     * @param type 请求类型，具体看HttpMethod，提供了四种请求类型
     * @param context 上下文
     * @param enetity 请求model
     * @param httpRespondResult 回调接口
     */
    LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestGetModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
#####1.3  post与get自定义Json对象请求

######1.3.1  POST JSON

`````
 /**
     *
     * @param type 请求类型，具体看HttpMethod，提供了四种请求类型
     * @param enetity 请求model
     * @param httpRespondResult 回调接口
     */
    LeopardHttp.SEND(HttpMethod.POST_JSON,getActivity(),new RequestPostJsonModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
####1.4 post与get请求，带头部

######1.4.1 POST 带头部请求

`````

        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

  /**
     *
     * @param type 请求类型，具体看HttpMethod，提供了四种请求类型
     * @param context 上下文
     * @param enetity 请求model
     * @param header 请求头文件
     * @param httpRespondResult 回调接口
     */
        LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
######1.4.2 GET 带头部请求

`````

        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

  /**
     *
     * @param type 请求类型，具体看HttpMethod，提供了四种请求类型
     * @param context 上下文
     * @param enetity 请求model
     * @param header 请求头文件
     * @param httpRespondResult 回调接口
     */
        LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````

####2. 上传管理

首先在上传的时候必须先看下上传封装实体类FileUploadEnetity（部分代码）

`````
    .....
   private String url;
    private List<File> files = new ArrayList<>();

    public FileUploadEnetity(String url, File file) {
        this.url = url;
        this.files.add(file);
        initSize();
    }

    public FileUploadEnetity(String url, List<File> files) {
        this.url = url;
        this.files = files;
        initSize();
    }
    .....
`````
在上传的时候，允许开发者上传你一个或多个文件。如果是上传一个文件的时候，构建的时候只需要传入上传地址与File类型的文件；反之上传多文件，构建的时候只需要传入上传地址与List<File> 类型的文件队列类型。

#####2.1 基本上传

`````
 /**
     * 上传入口
     *
     * @param FileUploadEnetity 上传封装实体类
     * @param UploadIProgress 上传进度回调接口
     */
     LeopardHttp.UPLOAD(new FileUploadEnetity("http://wxwusy.applinzi.com/leopardWeb/app/sample/upload.php",fileList), new UploadIProgress() {

                    @Override
                    public void onProgress(long progress, long total, int index, boolean done) {
                        Log.i("yuan","upload state: "+progress + " - "+total);
                        if (done){
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"所有图片上传成功！！",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSucess(String result) {
                        Toast.makeText(getActivity(),result,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Throwable e, String reason) {
                        Toast.makeText(getActivity(),reason,Toast.LENGTH_SHORT).show();

                    }
                });
``````

####3. 下载管理

首先底层断点信息数据库存储由Greendao数据库框架支持。其次，在下载的时候必须看下实体封装类DownloadInfo ，在进行添加下载Task的时候，在构建DownloadInfo 对象的时候，至少初始化下载地址、存储文件名。例如以下代码：

`````
String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileName("IRecord_" + i + ".apk");
`````

#####3.1 基本下载(添加任务Task与启动下载)

`````
    /**
     * 下载入口
     *
     * @param downloadInfo
     * @param iProgress
     * @return 拥有Task的下载实体
     */
   LeopardHttp.DWONLOAD(info, new IProgress() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
                // progress 返回当前上传的进度 ，total 返回当前上传的总文件大小
                //按钮更新
                if (info.getState() == DownLoadManager.STATE_WAITING) {
                    holder.downBtn.setText("下载");
                }

                if (info.getState() == DownLoadManager.STATE_PAUSE) {
                    holder.downBtn.setText("继续");
                }

                if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
                    holder.downBtn.setText("暂停");
                }
                if (done) {
                    //下载完成...
                }
            }
        });
`````
#####3.2 DownLoadTask 单任务下载管理

######3.2.1 开始下载

`````
// 传入true表示从头开始下载
downloadInfo.getDownLoadTask().downLoad(true);
`````
######3.2.2 暂停下载

`````
downloadInfo.getDownLoadTask().pause();
`````
######3.2.3 恢复下载

`````
downloadInfo.getDownLoadTask().resume();
`````
######3.2.4 停止下载

`````
downloadInfo.getDownLoadTask().stop();
`````
######3.2.5 重新下载

`````
downloadInfo.getDownLoadTask().restart();
`````
#####3.3 DownLoadManager 多任务下载管理

######3.3.1 删除所有下载任务

`````
DownLoadManager.getManager().removeAllTask();
`````
######3.3.2 暂停所有任务

`````
DownLoadManager.getManager().pauseAllTask();
`````
######3.3.3 停止所有任务

`````
DownLoadManager.getManager().stopAllTask();
`````
######3.3.4 开始所有任务

`````
DownLoadManager.getManager().startAllTask();
`````

##Leopard，使用方法（手动构建）
前面封装的单例模式LeopardHttp,是为了开发者可以更加方便调用，自动化构建好了LeopardClient对象并且配置了相对应需要的属性。不过还是那句话，为了能够让读者可以更加灵活配置LeopardClient，所以建议还是用构建者模式构建，可以根据需求再自定义拦截器或者自定义Factroy。

现在举例子手动构建的部分代码，目前支持的功能有：
####1.基本请求

#####1.1 POST与 GET 键值对请求

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get请求
             // .POST(context, enetity, httpRespondResult);//post请求
`````
#####1.2 POST与 GET 自定义Json请求

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRequestJsonFactory(RequestJsonFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get请求
             // .POST(context, enetity, httpRespondResult);//post请求
`````
####2. 下载管理

`````
LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo))
                .build()
                .downLoadFile(this.downloadInfo, callBack.fileRespondResult, downLoadTask);
`````
####3. 上传管理

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,respondResult)
                
`````
###扩展性

目前扩展性，允许开发者进行继承性扩展的有：

1. 自定义接入接口,允许开发者根据需求接入新的接口。

      继承BaseServerApi接口即可
      
2. 自定义扩展Builder,允许开发者自定义添加拦截器与Factory

      继承LeopardClient.Builder与LeopardClient即可
      
3. 允许下载管理任务功能扩展

      允许开发者可以扩展下载管理任务功能，底层基本的开始、暂停等方法不允许修改。开发者可以额外扩展其他自定义功能。
      

版本迭代中...

简书：http://www.jianshu.com/users/d388bcf9c4d3/latest_articles
欢迎加入开发狂热者QQ群： 450302004

傻小孩b
共勉，写给在成长路上奋斗的你。



------------------------------------------------------------------------------------------------------------------------------
# Leopard

##Foreword
Leopard is cheetah, of all the cats. The smallest cheetah, speed, the most stable. This is also the author want to use this name to this Kit. Hope this Kit for some developers to encapsulate some ideas help network framework, the author also in the struggle on the road summarized progress, '! Finally, if you have any questions can give me a lift issuse, or directly contact me (email: sy.wu.chn@gmail.com), likes to point a star for me, your support is my biggest power ~ thank you!

##Leopard，The current implementation function
Provide a HTTP request thread safety to satisfy daily needs package Library, the underlying the Retrofit + Okhttp + RxJava support, by building builder design pattern implementation. Current POST and GET (support custom header files, form the key value of the request, the custom data sources such as basic request), file upload management (support single file upload and file upload, do not restrict the file type), file download manager (support single file download with multiple files to download, do not restrict the file type, support large file download and breakpoint download)
##Demo

![LeopardShow.jpg](http://upload-images.jianshu.io/upload_images/2516602-e7f52082af597001.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##Leopard，Method of use：
NoticeCurrently only supports the Android Studio version。
### 1.1 version Method of use：
####一、Introduced in the application of the build.gradle:
````
repositories {
    maven { url = 'https://dl.bintray.com/yuancloud/maven/' }
    ...
}

compile 'cn.yuancloud.app:leopardkit:1.1'
````
Details usage can see the inside of the kit sample project, using the example below will approach.

##Leopard，Code analyses
###1st. Builder Mode

Usually, most developers can use the singleton pattern to encapsulate network framework. Indeed, for serious memory network request object, singleton pattern greatly reduces the memory overhead. But the singleton pattern responsibility is too single, flexibility really is not high. So here I strongly recommend using Builder model, single responsibility needs to be informed what resources as long as the builders Builder, such not only can reduce the memory overhead, and object to building flexibility needs, kill two birds with one stone.

###2nd. Seven Factory supports

The Factory here, including the Converter.Factory and Interceptor support. Currently include Retrofit the underlying GsonConverterFactory realized with RxJavaCallAdapterFactory, Leopard has added an extra five Factory, under the following specific simple additional 5 Factory and Retrofit is implemented at the bottom of the Factory.
（1）GsonConverterFactory  

Retrofit the underlying support Gson, the Factory provides some when you need to adjust the inside of the json format can use.
（2）RxJavaCallAdapterFactory

When you need to combine RxJava, rather than merely use native Retrofit request response callback to Call. You need a callback an observer (observables), add the Factory must be constructed.
（3）RequestComFactory

Must be added to the default Factory, in order to intercept requests when the request and response.

（4）RequestJsonFactory

When you need to ask your server for the key-value pairs, but custom object after the json data. Must be constructed of adding this Factory, the underlying automatically identify to help you.
（5）UploadFileFactory

As the name suggests, when you need to upload a file, when constructing the client need to add the Factory, the underlying file type header information will be automatically generated.

（6）DownLoadFileFactory

As the name suggests, when you need to download the file, when constructing the client need to add the Factory, the bottom will be automatically generated automatically depending on the type of file header information, the default open breakpoint continuingly, download process by DownLoadManager management control.

（7）HeaderAddFactory

When you need a custom header files, in constructing the client need to add the Factory. The underlying will automatically help you added to the request header.


###3rd. About the basic request
"RxJava" benefits, can ensure the safety of the thread. Due to network requests cannot perform in the main thread, therefore in the "Leopard", put all network perform IO thread, ensure thread safety. For example, the following configuration:
`````
final Observable.Transformer schedulersTransformer = new Observable.Transformer() {
    @Override
    public Object call(Object observable) {
        return ((Observable) observable)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }
};
`````
In Leopard, a basic has written six entrances, developers can according to their own needs to inherit the custom entry

`````
    @POST("{path}")
    Observable<ResponseBody> post(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @GET("{path}")
    Observable<ResponseBody> get(
            @Path(value = "path", encoded = true) String path,
            @QueryMap Map<String, Object> map);

    @POST("{path}")
    Observable<ResponseBody> postJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

    @GET("{path}")
    Observable<ResponseBody> getJSON(
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody route);

    @Multipart
    @POST("{path}")
    Observable<ResponseBody> uploadFile(
            @Path(value = "path", encoded = true) String url,
            @PartMap() Map<String, RequestBody> maps);

//    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(
            @Url String fileUrl);
`````
Detailed source code parsing analysis Jane books will be in my blog.

###4th. About the upload
For Leopard download management model, draw lessons from the Android native DownLoadManager download management pattern -- DownLoadManager combined with DownLoadTask mechanism. DownLoadTask responsible for single responsibility, for each download task download service (cache management, start, pause, stop, etc). DownLoadManager as much DownLoadTask manager, multiple task to provide all the download service. Below is the download the concrete implementation of the logic diagram.

![Leopard Download.png](http://upload-images.jianshu.io/upload_images/2516602-cad4ab69949e4cca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Detailed source code parsing analysis Jane books will be in my blog.

###5th. About the download
Conventional practice, upload and key/value pair Reqtuest have similar logic operation. Value, of the File as a key/value pair will upload the File File information as the key, and then to get request to the server. Here is to address the file upload progress monitoring, for RequestBody okhttp3 encapsulated bottom, is not to schedule byte stream as a cache handling, therefore needs to be rewritten in the Leopard RequestBody, to upload the cache for processing. Below is the upload specific logic diagram.

![LeoPard UpLoad.png](http://upload-images.jianshu.io/upload_images/2516602-829b814ad0a3bd97.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Detailed source code parsing analysis Jane books will be in my blog.

##Leopard，Leopard, using method (building automation)
In order to reduce the user use complexity, there is a good integrated automated build leopard object, USES a singleton pattern. But the author is recommended according to the custom demand, using method is as follows:

#### 0. Initialization

`````
//  initialize the host domain name with the context
// Suggest that the incoming getApplication
LeopardHttp.init("http://wxwusy.applinzi.com/leopardWeb/app/",this);
`````

#### 1.  Basic request

In the first place at the time of the request parameters must define a model, and inherit the inside of the Leopard BaseEnetity, specific definition model example as follows:

`````
public class RequestPostModel extends BaseEnetity{
    @Override
    public String getRuqestURL() {
        return "sample/post.php";//你的访问url
    }

  //request parameters
    private String data;
    private String time;

    public RequestPostModel(String data, String time) {
        this.data = data;
        this.time = time;
    }

  //get set method must be established
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
`````

####1.1  post and get the keys for the request

#####1.1.1  POST

`````
 /**
     *
     * @param type  request type, specific see HttpMethod, provides four types of requests
     * @param context context
     * @param enetity request model
     * @param httpRespondResult callback
     */
    LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
#####1.1.2  GET

`````
/**
     *
     * @param type  request type, specific see HttpMethod, provides four types of requests
     * @param context context
     * @param enetity request model
     * @param httpRespondResult callback
     */
    LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestGetModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
#####1.3  The post and get custom Json object request


######1.3.1  POST JSON

`````
/**
     *
     * @param type  request type, specific see HttpMethod, provides four types of requests
     * @param context context
     * @param enetity request model
     * @param httpRespondResult callback
     */
    LeopardHttp.SEND(HttpMethod.POST_JSON,getActivity(),new RequestPostJsonModel("leopard", Utils.getNowTime()), new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {//json
                resonseData.setText("onSuccess \n"+content);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
####1.4 post与get post and get request, take the lead

######1.4.1  POST head requests

`````

        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

/**
     *
     * @param type  request type, specific see HttpMethod, provides four types of requests
     * @param context context
     * @param enetity request model
     * @param header request header files
     * @param httpRespondResult callback
     */
        LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````
######1.4.2 GET head requests

`````

        HashMap<String,String> headers = new HashMap<>();
        headers.put("apikey","dqwijotfpgjweigowethiuhqwqpqeqp");
        headers.put("apiSecret","ojtowejioweqwcxzcasdqweqrfrrqrqw");
        headers.put("name","leopard");

/**
     *
     * @param type  request type, specific see HttpMethod, provides four types of requests
     * @param context context
     * @param enetity request model
     * @param header request header files
     * @param httpRespondResult callback
     */
        LeopardHttp.SEND(HttpMethod.GET,getActivity(),new RequestPostModel("leopard", Utils.getNowTime()),headers, new HttpRespondResult(getActivity()) {
            @Override
            public void onSuccess(String content) {
                resonseData.setText("onSuccess \n"+content);

            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
`````

####2. Upload management

When uploading must first look at the upload encapsulate entity class FileUploadEnetity (code)

`````
    .....
   private String url;
    private List<File> files = new ArrayList<>();

    public FileUploadEnetity(String url, File file) {
        this.url = url;
        this.files.add(file);
        initSize();
    }

    public FileUploadEnetity(String url, List<File> files) {
        this.url = url;
        this.files = files;
        initSize();
    }
    .....
`````
At the time of uploading, allows developers to upload your one or more files. If is the time to upload a File, build time only need to upload the address and the File type of document; Instead to upload files, build the only need to upload the address with the List < File > type of File queue type.

#####2.1 Basic upload

`````
 /**
     * Upload
     *
     * @param FileUploadEnetity upload encapsulate entity class
     * @param IProgress upload progress callback interface
     */
    LeopardHttp.UPLOAD(new FileUploadEnetity("upload.php",fileList), new IProgress() {
                    @Override
                    public void onProgress(long progress, long total, boolean done) {
			// progress to return to the upload progress, total returns the current total upload file sizes
                        if (done){
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"Upload Success！！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
``````

####3. Download manager

First the underlying breakpoint information database storage by Greendao database framework support. Second, in the download you must look at the entity in the wrapper class DownloadInfo, when add download Task in constructing DownloadInfo object, at least initial download address, storage file name. For example the following code:

`````
String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileName("IRecord_" + i + ".apk");
`````

#####3.1 Basic download (add Task and start the download)

`````
    /**
     * download entrance
     *
     * @param downloadInfo
     * @param iProgress
     * @return have a Task to download entity
     */
   LeopardHttp.DWONLOAD(info, new IProgress() {
            @Override
            public void onProgress(long progress, long total, boolean done) {
		// progress to return to the upload progress, total returns the current total upload file sizes
                //button to update
                if (info.getState() == DownLoadManager.STATE_WAITING) {
                    holder.downBtn.setText("download ");
                }

                if (info.getState() == DownLoadManager.STATE_PAUSE) {
                    holder.downBtn.setText("continue");
                }

                if (info.getState() == DownLoadManager.STATE_DOWNLOADING) {
                    holder.downBtn.setText("pause ");
                }
                if (done) {
                    //the download is complete...
                }
            }
        });
`````
#####3.2 DownLoadTask single download management tasks

######3.2.1 began to download

`````
// to true indicates download from the very beginningdownloadInfo.getDownLoadTask().downLoad(true);
`````
######3.2.2 pause download

`````
downloadInfo.getDownLoadTask().pause();
`````
######3.2.3 resume download

`````
downloadInfo.getDownLoadTask().resume();
`````
######3.2.4  stop download

`````
downloadInfo.getDownLoadTask().stop();
`````
######3.2.5 download again

`````
downloadInfo.getDownLoadTask().restart();
`````
#####3.3 DownLoadManager multitasking download manager

######3.3.1 delete all downloads

`````
DownLoadManager.getManager().removeAllTask();
`````
######3.3.2 pause all downloads

`````
DownLoadManager.getManager().pauseAllTask();
`````
######3.3.3 stop all downloads

`````
DownLoadManager.getManager().stopAllTask();
`````
######3.3.4 begin all downloads

`````
DownLoadManager.getManager().startAllTask();
`````

##Leopard, using method (manual build)
Encapsulated in front of the singleton pattern LeopardHttp, it is more convenient for developers to invoke, automated build good LeopardClient object and configuration properties corresponding to the need. But again, in order to be able to let the reader can more flexible configuration LeopardClient, so suggest or use builder model building, can according to demand to custom interceptors or custom Factroy.

Now for example manually build the part of the code, now support functions are:

####1.Basic request

#####1.1 POST and GET the keys for the request

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get request
             // .POST(context, enetity, httpRespondResult);//post request
`````
#####1.2 POST and GET custom Json request

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRequestJsonFactory(RequestJsonFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get request
             // .POST(context, enetity, httpRespondResult);//post request
`````
####2. Download manager

`````
LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo))
                .build()
                .downLoadFile(this.downloadInfo, callBack.fileRespondResult, downLoadTask);
`````
####3. Upload manager

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,respondResult)
                
`````
### extensibility
The scalability, allowing developers to inheritance extensions are:

1. The custom access interface, allows developers to access new interface based on the requirements.

extends BaseServerApi interface

2. Custom extensions Builder, allows developers to add custom interceptors and Factory

extends LeopardClient. Builder and LeopardClient can

3. Allow the download function extension management tasks

Allows developers to download task management function can be extended, the underlying basic methods such as start, pause is not allowed to change. Developers can extend other additional custom functions.

Version updating...

My blog：http://www.jianshu.com/users/d388bcf9c4d3/latest_articles

Yuan
To strive, to strive for growing the way you.

