# Leopard (version : 1.0)

## Leopard，Code analyses
### 1st. Builder Mode

Usually, most developers can use the singleton pattern to encapsulate network framework. Indeed, for serious memory network request object, singleton pattern greatly reduces the memory overhead. But the singleton pattern responsibility is too single, flexibility really is not high. So here I strongly recommend using Builder model, single responsibility needs to be informed what resources as long as the builders Builder, such not only can reduce the memory overhead, and object to building flexibility needs, kill two birds with one stone.

### 2nd. Seven Factory supports

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


### 3rd. About the basic request
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

### 4th. About the upload
For Leopard download management model, draw lessons from the Android native DownLoadManager download management pattern -- DownLoadManager combined with DownLoadTask mechanism. DownLoadTask responsible for single responsibility, for each download task download service (cache management, start, pause, stop, etc). DownLoadManager as much DownLoadTask manager, multiple task to provide all the download service. Below is the download the concrete implementation of the logic diagram.

![Leopard Download.png](http://upload-images.jianshu.io/upload_images/2516602-cad4ab69949e4cca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Detailed source code parsing analysis Jane books will be in my blog.

### 5th. About the download
Conventional practice, upload and key/value pair Reqtuest have similar logic operation. Value, of the File as a key/value pair will upload the File File information as the key, and then to get request to the server. Here is to address the file upload progress monitoring, for RequestBody okhttp3 encapsulated bottom, is not to schedule byte stream as a cache handling, therefore needs to be rewritten in the Leopard RequestBody, to upload the cache for processing. Below is the upload specific logic diagram.

![LeoPard UpLoad.png](http://upload-images.jianshu.io/upload_images/2516602-829b814ad0a3bd97.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

Detailed source code parsing analysis Jane books will be in my blog.


### Extensibility
The scalability, allowing developers to inheritance extensions are:

1. The custom access interface, allows developers to access new interface based on the requirements.

extends BaseServerApi interface

2. Custom extensions Builder, allows developers to add custom interceptors and Factory

extends LeopardClient. Builder and LeopardClient can

3. Allow the download function extension management tasks

Allows developers to download task management function can be extended, the underlying basic methods such as start, pause is not allowed to change. Developers can extend other additional custom functions.

Version updating...

My blog：http://www.jianshu.com/users/d388bcf9c4d3/latest_articles

Siven
To strive, to strive for growing the way you.
