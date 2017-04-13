# Leopard

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