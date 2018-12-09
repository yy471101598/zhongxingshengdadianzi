package com.shoppay.zxsddz.modle;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.zxsddz.bean.YhqMsg;
import com.shoppay.zxsddz.tools.LogUtils;
import com.shoppay.zxsddz.tools.PreferenceHelper;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ImpObtainYhq {

    public void obtainYhq(final Activity ac, String MemID, String CouPonAccount,
                          String DiscountMoney, final InterfaceMVC interf) {
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(ac);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("MemID", MemID);
        params.put("CouPonAccount", CouPonAccount);
        params.put("DiscountMoney", DiscountMoney);
        LogUtils.d("xxparams", params.toString());

        client.post(PreferenceHelper.readString(ac, "shoppay", "yuming", "123") + "?Source=3&UserID=" + PreferenceHelper.readString(ac, "shoppay", "UserID", "123") + "&UserShopID=" + PreferenceHelper.readString(ac, "shoppay", "ShopID", "123") + "&Method=GetCouPonMoney", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Log.d("xxDengjiS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        JSONObject data = jso.getJSONObject("vdata");
                        YhqMsg yhqMsg = new YhqMsg();
                        yhqMsg.CouponID = data.getString("CouponID");
                        yhqMsg.CouPonMoney = data.getString("CouPonMoney");
                        interf.onResponse(1, yhqMsg);

                    } else {
                        interf.onErrorResponse(2, jso.getString("msg"));
                    }
                } catch (Exception e) {
                    interf.onErrorResponse(3, "获取优惠券信息失败，请重试");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                interf.onErrorResponse(3, "获取优惠券信息失败，请重试");
            }
        });
    }
}
