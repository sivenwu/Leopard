package cn.yuan.leopard.model;

import com.yuan.leopardkit.http.base.BaseEnetity;

/**
 * Created by Yuan on 2016/9/3.
 * Detail 请求自定义json数据接口
 */

public class RequestGetJsonModel extends BaseEnetity{
    @Override
    public String getRuqestURL() {
        return "sample/getJson.php";
    }

    private String data;
    private String time;

    public RequestGetJsonModel() {
    }

    public RequestGetJsonModel(String data, String time) {
        this.data = data;
        this.time = time;
    }

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
