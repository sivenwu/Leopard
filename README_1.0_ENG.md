# Leopard

### Using method (building automaticaly)
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

#### 1.1  post and get the keys for the request

##### 1.1.1  POST

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
##### 1.1.2  GET

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
##### 1.3  The post and get custom Json object request


###### 1.3.1  POST JSON

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
#### 1.4 post与get post and get request, take the lead

###### 1.4.1  POST head requests

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
###### 1.4.2 GET head requests

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

#### 2. Upload management

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

##### 2.1 Basic upload

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
`````
#### 3. Download manager

First the underlying breakpoint information database storage by Greendao database framework support. Second, in the download you must look at the entity in the wrapper class DownloadInfo, when add download Task in constructing DownloadInfo object, at least initial download address, storage file name. For example the following code:

`````
String url = "http://f1.market.xiaomi.com/download/AppStore/03f82a470d7ac44300d8700880584fe856387aac6/cn.wsy.travel.apk";
            DownloadInfo info = new DownloadInfo();
            info.setUrl(url);
            info.setFileName("IRecord_" + i + ".apk");
`````

##### 3.1 Basic download (add Task and start the download)

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

##### 3.2 DownLoadTask single download management tasks

###### 3.2.1 began to download

`````
// to true indicates download from the very beginningdownloadInfo.getDownLoadTask().downLoad(true);
`````
###### 3.2.2 pause download

`````
downloadInfo.getDownLoadTask().pause();
`````
###### 3.2.3 resume download

`````
downloadInfo.getDownLoadTask().resume();
`````
###### 3.2.4  stop download

`````
downloadInfo.getDownLoadTask().stop();
`````
###### 3.2.5 download again

`````
downloadInfo.getDownLoadTask().restart();
`````
##### 3.3 DownLoadManager multitasking download manager

###### 3.3.1 delete all downloads

`````
DownLoadManager.getManager().removeAllTask();
`````
###### 3.3.2 pause all downloads

`````
DownLoadManager.getManager().pauseAllTask();
`````
###### 3.3.3 stop all downloads

`````
DownLoadManager.getManager().stopAllTask();
`````
###### 3.3.4 begin all downloads

`````
DownLoadManager.getManager().startAllTask();
`````

## Using method (manual build)

Encapsulated in front of the singleton pattern LeopardHttp, it is more convenient for developers to invoke, automated build good LeopardClient object and configuration properties corresponding to the need. But again, in order to be able to let the reader can more flexible configuration LeopardClient, so suggest or use builder model building, can according to demand to custom interceptors or custom Factroy.

Now for example manually build the part of the code, now support functions are:

#### 1.Basic request

##### 1.1 POST and GET the keys for the request

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
##### 1.2 POST and GET custom Json request

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
#### 2. Download manager

`````
LeopardClient.Builder()
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addDownLoadFileFactory(DownLoadFileFactory.create(this.fileRespondResult, this.downloadInfo))
                .build()
                .downLoadFile(this.downloadInfo, callBack.fileRespondResult, downLoadTask);
`````
#### 3. Upload manager

`````
 LeopardClient.Builder()
                .addRequestComFactory(new RequestComFactory(respondResult))
                .addGsonConverterFactory(GsonConverterFactory.create())
                .addRxJavaCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(ADDRESS)
                .addUploadFileFactory(UploadFileFactory.create())
                .build()
                .upLoadFiles(uploadEnetity,respondResult)
