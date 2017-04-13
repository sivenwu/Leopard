# Leopard

2.0 技术使用文档，适用于Leopard库1.4-2.0版本，主要正对如下优化

> 1.下载上传内部优化，部分操作入口更改
> 2.基本请求回调优化，部分操作入口更改
> 3.增加缓存使用说明文档
> 4.简化单例构建LeopardHttp使用，无须再初始化，部分操作入口更改


## 导航
### 使用方法（自动化构建）
- [0.绑定域名，无须初始化](#user-content-0-绑定域名无须初始化)</br>
- [1. 基本请求](#user-content-1-基本请求)</br>
    - [1.1  post与get键值对请求](#user-content-11--post与get键值对请求)</br>
    - [1.2  post与get自定义Json对象请求](#user-content-12--post与get自定义json对象请求)</br>
    - [1.3 post与get请求，带头部](#user-content-13-post与get请求带头部)</br>
- [2. 请求回调](#user-content-2-请求回调)</br>
- [3. 缓存使用](#user-content-3-缓存使用)</br>
- [4. 上传管理](#user-content-4-上传管理)</br>
    - [4.1 基本上传](#user-content-41-基本上传)</br>
- [5. 下载管理](#user-content-5-下载管理)</br>
    - [5.1 基本下载(添加任务Task与启动下载)](#user-content-51-基本下载添加任务task与启动下载)</br>
    - [5.2 DownLoadManager 单任务下载管理](#user-content-52-downloadmanager-单任务下载管理)</br>
    - [5.3 DownLoadManager 多任务下载管理](#user-content-53-downloadmanager-多任务下载管理)</br>

 ### 使用方法（自定义）
- [1. 基本请求](#pro_1)</br>
    - [1.1  post与get键值对请求](#user-content-11-post与-get-键值对请求)</br>
    - [1.2  post与get自定义Json对象请求](#user-content-12-post与-get-自定义json请求)</br>
- [2. 下载管理 ( 由DownLoadManager 管理)](#user-content-2-下载管理--由downloadmanager-管理)</br>
- [3. 上传管理](#user-content-3-上传管理)


## 使用方法（自动化构建）
为了减少使用者使用复杂度，leopard里面有集成好自动构建的对象，采用的是单例模式。不过笔者还是推荐根据需求自定义，使用方法如下：

#### <span id="auto_0">0. 绑定域名，无须初始化</span>

`````
 /**  因为是单例模式进行构建leopard请求，所以请求前需要先进行域名绑定 **/
 LeopardHttp.bindServer("http://wxwusy.applinzi.com");
`````

#### <span id="auto_1">1. 基本请求</span>

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

#### <span id="auto_1_1">1.1  post与get键值对请求</span>

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

##### <span id="auto_1_2">1.2  post与get自定义Json对象请求</span>

###### 1.2.1  POST JSON

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

#### <span id="auto_1_3">1.3 post与get请求，带头部</span>

###### 1.3.1 POST 带头部请求

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
###### 1.3.2 GET 带头部请求

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

#### <span id="auto_2">2. 请求回调</span>
目前Leopard支持两种回调方式
1、HttpRespondResult 普通回调方式，返回Success结果统一为Json字符串，需要由开发者自己进行Json解析

2、HttpRespondObjectResult
类型转化回调方式，返回是一个泛型，因此只需要开发者在传入泛型类型，即可以进行Object类型回掉

###### 案例
`````
 public void postObj(){
        LeopardHttp.SEND(HttpMethod.POST,getActivity(),new RequestPostModel("leopard", "888888"), new HttpRespondObjectResult<TestResponseModel>() {
            @Override
            public void onSuccess(Task<TestResponseModel> task) {

                // 通过task获取响应成功的实体类
                TestResponseModel model = task.getResult();

                resonseData.setText("onSuccess \n"
                        +model.getAuthor() + " " + model.getInfo());
            }

            @Override
            public void onFailure(Throwable error, String content) {
                resonseData.setText("onFailure \n"+content);
            }
        });
    }
`````

#### <span id="auto_3">3. 缓存使用</span>

目前Leopard支持两种缓存方式，分别为在线缓存、离线缓存，并且支持POST、GET两种
** 在线缓存 ** ：允许开发者自定义在线缓存时间（默认是1分钟）。在缓存时间内，执行相同的请求，不会重新去服务器获取新数据，而是在本地缓存，尽可能减少了对后台服务器请求压力。
** 离线缓存 ** ：在客户端处于网络离线状态时，仍然可以请求时获取数据。

1、如果使用的是单例创建的LeopardHttp，只需要打开缓存开关即可
`````
LeopardHttp.setUseCache(true);// 是否启动缓存
`````
这里LeopardHttp默认只打开了离线缓存，如果需要在线缓存，请开发者自主构建LeopardClient进行请求

2、如果是自主构建LeopardClient，只需要引入CacheFactory即可
###### 案例
`````
    // 开启离线缓存
    LeopardClient client = new LeopardClient.Builder(mC,ADDRESS)
            .addCacheFactory(CacheFactory.create(mC))
            .build();
        }

    // 开启在线缓存
   LeopardClient client = new LeopardClient.Builder(mC,ADDRESS)
            .addCacheFactory(CacheFactory.create(mC，true))
            .build();
        }

   // 开启在线缓存 自定义时间
   LeopardClient client = new LeopardClient.Builder(mC,ADDRESS)
            .addCacheFactory(CacheFactory.create(mC，true,100)) // 100 秒
            .build();
        }
`````


#### <span id="auto_4">4. 上传管理</span>

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

##### <span id="auto_4_1">4.1 基本上传</span>
1、 使用单例 LeopardHttp

`````
 /**
     * 上传入口
     *
     * @param FileUploadEnetity 上传封装实体类
     * @param IProgress 上传进度回调接口
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
                        // 上传成功 后台服务器返回结果
                        Toast.makeText(getActivity(),result,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(Throwable e, String reason) {
                        Toast.makeText(getActivity(),reason,Toast.LENGTH_SHORT).show();

                    }
                });
`````

#### <span id="auto_5">5. 下载管理</span>

2.0下载管理，比较大的变化是DownLoadInfo减少了依赖DownLoadTask，尽可能减少对Task的暴露，一切操作由DownLoadManager进行管理。

首先底层断点信息数据库存储由Greendao数据库框架支持。其次，在下载的时候必须看下实体封装类DownloadInfo ，在进行添加下载Task的时候，在构建DownloadInfo 对象的时候，至少初始化下载地址、存储文件名。例如以下代码：

`````
String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileName("IRecord_" + i + ".apk");
`````

##### <span id="auto_5_1">5.1 基本下载(添加任务Task与启动下载)</span>

`````
    /**
     * 下载入口
     *
     * @param downloadInfo
     * @param IDownloadProgress
     * @return 拥有Task的下载实体
     */
   LeopardHttp.DWONLOAD(info, new IDownloadProgress() {
            @Override
            public void onProgress(long key,long progress, long total, boolean done) {

//                Log.i("yuan onProgress", " progress : " + progress + "now total :" + total);

              // ...
                if (progress >= total && total != 0) {
                    holder.downBtn.setText("重新開始");
                    holder.prgressTv.setText(holder.prgressTv.getText() + " 下載完成！");
                }
            }

            @Override
            public void onSucess(String result) {
                // nothing..
            }

            @Override
            public void onFailed(Throwable e, String reason) {
                // nothing..
            }
        });
`````
##### <span id="auto_5_2">5.2 DownLoadManager 单任务下载管理</span>

###### 5.2.1 开始下载

`````
// 开始下载
 DownLoadManager.getManager().start(downloadinfo);
`````
###### 5.2.2 暂停下载

`````
 DownLoadManager.getManager().pauseTask(info);
`````
###### 5.2.3 恢复下载

`````
   DownLoadManager.getManager().resumeTask(info);
`````
###### 5.2.4 停止下载

`````
 DownLoadManager.getManager().stopTask(info);
`````
###### 5.2.5 重新下载

`````
DownLoadManager.getManager().restartTask(downloadinfo);
`````

##### <span id="auto_5_3">5.3 DownLoadManager 多任务下载管理</span>

###### 5.3.1 删除所有下载任务

`````
DownLoadManager.getManager().removeAllTask();
`````
###### 5.3.2 暂停所有任务

`````
DownLoadManager.getManager().pauseAllTask();
`````
###### 5.3.3 停止所有任务

`````
DownLoadManager.getManager().stopAllTask();
`````
###### 5.3.4 开始所有任务

`````
 DownLoadManager.getManager().startAllTask();
`````

## <span id="pro_0">使用方法（手动构建）</span>
前面封装的单例模式LeopardHttp,是为了开发者可以更加方便调用，自动化构建好了LeopardClient对象并且配置了相对应需要的属性。不过还是那句话，为了能够让读者可以更加灵活配置LeopardClient，所以建议还是用构建者模式构建，可以根据需求再自定义拦截器或者自定义Factroy。

现在举例子手动构建的部分代码，目前支持的功能有：
#### <span id="pro_1">1.基本请求</span>

##### <span id="pro_1_1">1.1 POST与 GET 键值对请求</span>

`````
 LeopardClient.Builder(mContext,ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get请求
             // .POST(context, enetity, httpRespondResult);//post请求
`````
##### <span id="pro_1_2">1.2 POST与 GET 自定义Json请求</span>

`````
  LeopardClient.Builder(mContext,ADDRESS)
                .build()
                .GET(context, enetity, httpRespondResult);//get请求
             // .POST(context, enetity, httpRespondResult);//post请求
`````
#### <span id="pro_2">2. 下载管理 ( 由DownLoadManager 管理)</span>

`````
DownLoadManager.getManager().addTask(downloadInfo,iDownloadProgress);
`````
#### 3. <span id="pro_3">上传管理</span>

`````
 LeopardClient.Builder(mContedxt,ADDRESS)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,iProgress);
`````




-------------------------------------------------------------------------------