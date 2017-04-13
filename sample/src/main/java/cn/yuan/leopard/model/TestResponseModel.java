package cn.yuan.leopard.model;

/**
 * Created by siven on 17/3/31.
 */

public class TestResponseModel {

    private String code;
    private String reasion;
    private String author;
    private String requestType;
    private String requestTime;
    private String requestData;
    private String info;
    private String notice;

    public TestResponseModel(String code, String reasion, String author, String requestType, String requestTime, String requestData, String info, String notice) {
        this.code = code;
        this.reasion = reasion;
        this.author = author;
        this.requestType = requestType;
        this.requestTime = requestTime;
        this.requestData = requestData;
        this.info = info;
        this.notice = notice;
    }

    public TestResponseModel() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReasion() {
        return reasion;
    }

    public void setReasion(String reasion) {
        this.reasion = reasion;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}

