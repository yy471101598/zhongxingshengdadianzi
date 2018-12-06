package com.shoppay.zxsddz.http;

/**
 * 访问网络接口的成功失败
 *
 * @author Administrator
 */
public interface InterfaceVolleyResponse {

    public void onResponse(int code, Object response);

    public void onErrorResponse(int code, Object msg);
}
