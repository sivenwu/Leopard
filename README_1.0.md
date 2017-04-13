# Leopard

## 使用方法（自动化构建）
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

#### 1.1  post与get键值对请求

##### 1.1.1  POST

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
##### 1.1.2  GET

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

##### 1.3  post与get自定义Json对象请求

###### 1.3.1  POST JSON

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

#### 1.4 post与get请求，带头部

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
###### 1.4.2 GET 带头部请求

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

#### 2. 上传管理

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

##### 2.1 基本上传

`````
 /**
     * 上传入口
     *
     * @param FileUploadEnetity 上传封装实体类
     * @param IProgress 上传进度回调接口
     */
    LeopardHttp.UPLOAD(new FileUploadEnetity("upload.php",fileList), new IProgress() {
                    @Override
                    public void onProgress(long progress, long total, boolean done) {
                         // progress 返回当前上传的进度 ，total 返回当前上传的总文件大小
                        if (done){
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(),"所有图片上传成功！！",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
`````

#### 3. 下载管理

首先底层断点信息数据库存储由Greendao数据库框架支持。其次，在下载的时候必须看下实体封装类DownloadInfo ，在进行添加下载Task的时候，在构建DownloadInfo 对象的时候，至少初始化下载地址、存储文件名。例如以下代码：

`````
String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileName("IRecord_" + i + ".apk");
`````

##### 3.1 基本下载(添加任务Task与启动下载)

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
##### 3.2 DownLoadTask 单任务下载管理

###### 3.2.1 开始下载

`````
// 传入true表示从头开始下载
downloadInfo.getDownLoadTask().downLoad(true);
`````
###### 3.2.2 暂停下载

`````
downloadInfo.getDownLoadTask().pause();
`````
###### 3.2.3 恢复下载

`````
downloadInfo.getDownLoadTask().resume();
`````
###### 3.2.4 停止下载

`````
downloadInfo.getDownLoadTask().stop();
`````
###### 3.2.5 重新下载

`````
downloadInfo.getDownLoadTask().restart();
`````
##### 3.3 DownLoadManager 多任务下载管理

###### 3.3.1 删除所有下载任务

`````
DownLoadManager.getManager().removeAllTask();
`````
###### 3.3.2 暂停所有任务

`````
DownLoadManager.getManager().pauseAllTask();
`````
###### 3.3.3 停止所有任务

`````
DownLoadManager.getManager().stopAllTask();
`````
###### 3.3.4 开始所有任务

`````
DownLoadManager.getManager().startAllTask();
`````

## 使用方法（手动构建）
前面封装的单例模式LeopardHttp,是为了开发者可以更加方便调用，自动化构建好了LeopardClient对象并且配置了相对应需要的属性。不过还是那句话，为了能够让读者可以更加灵活配置LeopardClient，所以建议还是用构建者模式构建，可以根据需求再自定义拦截器或者自定义Factroy。

现在举例子手动构建的部分代码，目前支持的功能有：
#### 1.基本请求

##### 1.1 POST与 GET 键值对请求

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
##### 1.2 POST与 GET 自定义Json请求

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
#### 2. 下载管理

`````
LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo))
                .build()
                .downLoadFile(this.downloadInfo, callBack.fileRespondResult, downLoadTask);
`````
#### 3. 上传管理

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




-------------------------------------------------------------------------------