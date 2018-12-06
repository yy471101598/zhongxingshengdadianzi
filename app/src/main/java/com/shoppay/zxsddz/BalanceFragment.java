package com.shoppay.zxsddz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.shoppay.zxsddz.adapter.RightAdapter;
import com.shoppay.zxsddz.bean.Shop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songxiaotao on 2017/6/30.
 */

public class BalanceFragment extends Fragment {

    public static final String TAG = "BalanceFragment";
    private TextView tv_no,tv_loading;
    private ListView listView;
    private RightAdapter adapter;
    private List<Shop> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_balance, null);
       tv_no = (TextView) view.findViewById(R.id.fragment_no);
        tv_loading = (TextView) view.findViewById(R.id.fragment_loading);
        listView= (ListView) view.findViewById(R.id.fragment_listview);
        //得到数据
        list = (ArrayList<Shop>)getArguments().getSerializable(TAG);
        if(list.size()>0) {
            adapter = new RightAdapter(getActivity(), list);
            listView.setAdapter(adapter);
        }else{
            tv_no.setVisibility(View.VISIBLE);
        }
//        obtainShop();
        return view;
    }


//    private void obtainShop() {
//        tv_loading.setVisibility(View.VISIBLE);
//        tv_no.setVisibility(View.GONE);
//        AsyncHttpClient client = new AsyncHttpClient();
//        final PersistentCookieStore myCookieStore = new PersistentCookieStore(MyApplication.context);
//        client.setCookieStore(myCookieStore);
//        RequestParams params = new RequestParams();
//        params.put("classID",Integer.parseInt(str));
//        client.post( PreferenceHelper.readString(MyApplication.context, "shoppay", "yuming", "123") + "/mobile/app/api/appAPI.ashx?Method=APPGetGoodsList", params, new AsyncHttpResponseHandler()
//        {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
//            {
//                tv_loading.setVisibility(View.GONE);
//                try {
//                    LogUtils.d("xxshopS",new String(responseBody,"UTF-8"));
//                    JSONObject jso=new JSONObject(new String(responseBody,"UTF-8"));
//                    if(jso.getBoolean("success")){
//                        Gson gson = new Gson();
//                        Type listType = new TypeToken<List<Shop>>(){}.getType();
//                        list = gson.fromJson(jso.getString("data"), listType);
//                        if(list.size()>0) {
//                            adapter = new RightAdapter(getActivity(), list);
//                            listView.setAdapter(adapter);
//                        }else{
//                            tv_no.setVisibility(View.VISIBLE);
//                        }
//                    }else{
//                        tv_no.setVisibility(View.VISIBLE);
//                        Toast.makeText(MyApplication.context,jso.getString("msg"),Toast.LENGTH_SHORT).show();
//                    }
//                }catch (Exception e){
//                    tv_no.setVisibility(View.VISIBLE);
//                    Toast.makeText(MyApplication.context,"获取商品失败",Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
//            {
//                tv_no.setVisibility(View.VISIBLE);
//                Toast.makeText(MyApplication.context,"获取商品失败",Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
