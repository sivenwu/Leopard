package cn.yuan.leopard.model;

import com.yuan.leopardkit.http.base.BaseEnetity;

/**
 * Created by Yuan on 2016/9/3.
 * Detail 请求自定义json数据接口
 */

public class requestPostJsonModel extends BaseEnetity{
    @Override
    public String getRuqestURL() {
        return "sample/postJson.php";
    }
}
