package com.shoppay.szvipnewzh;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.szvipnewzh.adapter.NumRechargeAdapter;
import com.shoppay.szvipnewzh.bean.NumServece;
import com.shoppay.szvipnewzh.bean.Shop;
import com.shoppay.szvipnewzh.bean.ShopCar;
import com.shoppay.szvipnewzh.bean.VipInfo;
import com.shoppay.szvipnewzh.bean.VipInfoMsg;
import com.shoppay.szvipnewzh.bean.VipPayMsg;
import com.shoppay.szvipnewzh.card.ReadCardOpt;
import com.shoppay.szvipnewzh.db.DBAdapter;
import com.shoppay.szvipnewzh.http.InterfaceBack;
import com.shoppay.szvipnewzh.tools.CommonUtils;
import com.shoppay.szvipnewzh.tools.DialogUtil;
import com.shoppay.szvipnewzh.tools.LogUtils;
import com.shoppay.szvipnewzh.tools.NumRechargeDialog;
import com.shoppay.szvipnewzh.tools.PreferenceHelper;
import com.shoppay.szvipnewzh.tools.StringUtil;
import com.shoppay.szvipnewzh.tools.UrlTools;
import com.shoppay.szvipnewzh.wxcode.MipcaActivityCapture;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 *
 * @author qdwang
 *
 */
public class NumRechargeActivity extends Activity implements
        OnItemClickListener ,View.OnClickListener{

    private ListView listView;
    private RelativeLayout rl_jiesuan,rl_left,rl_right;
    private TextView tv_num,tv_money,tv_jifen,tv_title,tv_vipname,tv_vipjifen,tv_vipyue,tv_vipdengji;
    private EditText et_card;
    private Dialog dialog;
    private Context ac;
    private DBAdapter dbAdapter;
    private String editString;
    private NumRechargeAdapter adapter;
    private List<NumServece> list;
    private double num=0,money=0,jifen=0,xfmoney=0;
    private ShopChangeReceiver shopchangeReceiver;
    private boolean isSuccess=false;
    private Dialog jiesuanDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    VipInfo info = (VipInfo) msg.obj;
                    tv_vipname.setText(info.getMemName());
                    tv_vipjifen.setText(info.getMemPoint());
                    tv_vipyue.setText(info.getMemMoney());
                    tv_vipdengji.setText(info.getLevelName());
                    PreferenceHelper.write(ac, "shoppay", "vipcar", et_card.getText().toString());
                    PreferenceHelper.write(ac, "shoppay", "vipname", tv_vipname.getText().toString());
                    PreferenceHelper.write(ac, "shoppay", "memid",info.getMemID());
                    PreferenceHelper.write(ac, "shoppay", "MemMoney", info.getMemMoney() + "");
                    PreferenceHelper.write(ac, "shoppay", "jifenall",  info.getMemPoint());
                    PreferenceHelper.write(ac, "shoppay", "isSuccess", true);
                    PreferenceHelper.write(ac, "shoppay", "isInput", true);
                    isSuccess=true;
                    break;
                case 2:
                    tv_vipname.setText("");
                    tv_vipjifen.setText("");
                    tv_vipyue.setText("");
                    tv_vipdengji.setText("");
                    PreferenceHelper.write(ac, "shoppay", "isSuccess", false);
                    if(et_card.getText().toString().equals("")||et_card.getText().toString()==null){
                        PreferenceHelper.write(ac, "shoppay", "isInput", false);
                    }else{
                        PreferenceHelper.write(ac, "shoppay", "isInput", true);
                    }
                    isSuccess=false;
                    break;
            }
        }
    };
    private Intent intent;
    private MsgReceiver msgReceiver;
    private Dialog weixinDialog;
    private VipPayMsg vipPayMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numrecharge);
        ac=MyApplication.context;
       dialog= DialogUtil.loadingDialog(NumRechargeActivity.this,1);
        dbAdapter=DBAdapter.getInstance(ac);
        PreferenceHelper.write(ac, "shoppay", "memid", "");
        PreferenceHelper.write(ac, "shoppay", "vipcar","无");
        PreferenceHelper.write(ac, "shoppay", "isInput", false);
        PreferenceHelper.write(MyApplication.context,"shoppay","viptoast","未查询到会员");
        dbAdapter.deleteShopCar();
        initView();
        obtainServeceShop();

        // 注册广播
        shopchangeReceiver = new ShopChangeReceiver();
        IntentFilter iiiff = new IntentFilter();
        iiiff.addAction("com.shoppay.wy.numberchange");
        registerReceiver(shopchangeReceiver, iiiff);


        PreferenceHelper.write(getApplicationContext(), "PayOk", "time", "false");
        //动态注册广播接收器
//        msgReceiver = new MsgReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.communication.RECEIVER");
//        registerReceiver(msgReceiver, intentFilter);
        et_card.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (delayRun != null) {
                    //每次editText有变化的时候，则移除上次发出的延迟线程
                    handler.removeCallbacks(delayRun);
                }
                editString = editable.toString();

                //延迟800ms，如果不再输入字符，则执行该线程的run方法

                handler.postDelayed(delayRun, 800);
            }
        });
    }

    private void obtainServeceShop() {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        String url= UrlTools.obtainUrl(ac,"?Source=3","GetListService");
        LogUtils.d("xxurl",url);
        client.post(url, params, new AsyncHttpResponseHandler()
        {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
            {
                try {
                    dialog.dismiss();
                    LogUtils.d("xxNumshopS",new String(responseBody,"UTF-8"));
                    JSONObject jso=new JSONObject(new String(responseBody,"UTF-8"));
                    if(jso.getInt("flag")==1){
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Shop>>(){}.getType();
                        List<Shop> list = gson.fromJson(jso.getString("vdata"), listType);
                        adapter=new NumRechargeAdapter(NumRechargeActivity.this,list);
                        listView.setAdapter(adapter);
                    }else{
                        Toast.makeText(ac, jso.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
            {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
      new ReadCardOpt(et_card);
    }

    @Override
    protected void onStop() {
        try
        {
            new ReadCardOpt().overReadCard();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        super.onStop();
        if (delayRun != null) {
            //每次editText有变化的时候，则移除上次发出的延迟线程
            handler.removeCallbacks(delayRun);
        }
    }

    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable delayRun = new Runnable() {

        @Override
        public void run() {
            //在这里调用服务器的接口，获取数据
            ontainVipInfo();
        }
    };

    private void ontainVipInfo() {
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("MemCard", editString);
        LogUtils.d("xxparams",params.toString());
        String url= UrlTools.obtainUrl(ac,"?Source=3","GetMem");
        LogUtils.d("xxurl",url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    LogUtils.d("xxVipinfoS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if(jso.getInt("flag")==1){
                        Gson gson = new Gson();
                        VipInfoMsg infomsg=gson.fromJson(new String(responseBody, "UTF-8"),VipInfoMsg.class);
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.obj = infomsg.getVdata().get(0);
                        handler.sendMessage(msg);
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
    }

    /**
     * 初始化view
     */
    private void initView() {
        // TODO Auto-generated method stub
        rl_left= (RelativeLayout) findViewById(R.id.rl_left);
        rl_jiesuan= (RelativeLayout) findViewById(R.id.numrecharge_rl_jiesan);

        tv_jifen= (TextView) findViewById(R.id.numrecharge_tv_jifen);
        tv_vipjifen= (TextView) findViewById(R.id.numrecharge_tv_vipjifen);
        tv_vipyue= (TextView) findViewById(R.id.numrecharge_tv_vipyue);
        tv_vipname= (TextView) findViewById(R.id.numrecharge_tv_vipname);
        tv_num= (TextView) findViewById(R.id.numrecharge_tv_num);
        tv_vipdengji= (TextView) findViewById(R.id.vip_tv_vipdengji);
        tv_money= (TextView) findViewById(R.id.numrecharge_tv_money);
        tv_title= (TextView) findViewById(R.id.tv_title);
         tv_title.setText("会员充次");
        et_card= (EditText) findViewById(R.id.numrecharge_et_card);
        listView = (ListView) findViewById(R.id.listview);
        rl_right = (RelativeLayout) findViewById(R.id.rl_right);
        rl_right.setOnClickListener(this);
        rl_left.setOnClickListener(this);
        rl_jiesuan.setOnClickListener(this);

        listView.setOnItemClickListener(this);



    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_OK) {
                    et_card.setText(data.getStringExtra("codedata"));
                }
                break;

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_right:
                Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                startActivityForResult(mipca, 111);
                break;
            case R.id.rl_left:
                finish();
                break;
            case R.id.numrecharge_rl_jiesan:
                LogUtils.d("xx","click");
                if(tv_num.getText().toString().equals("0")){
                    Toast.makeText(getApplicationContext(), "请选择商品",
                            Toast.LENGTH_SHORT).show();
                }else{
                    if (CommonUtils.checkNet(getApplicationContext())) {
                          if(et_card.getText().toString().equals("")||et_card.getText().toString()==null){
                              Toast.makeText(ac,"请输入会员卡号",Toast.LENGTH_SHORT).show();
                          }else{
                              LogUtils.d("xx","ssssss");
                       jiesuanDialog= NumRechargeDialog.jiesuanDialog(dialog,NumRechargeActivity.this, 1,"num", Double.parseDouble(tv_money.getText().toString()), new InterfaceBack() {
                           @Override
                           public void onResponse(Object response) {
                               finish();
                           }

                           @Override
                           public void onErrorResponse(Object msg) {

                           }
                       });
                    }
                    }
                        else {
                        Toast.makeText(getApplicationContext(), "请检查网络是否可用",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    private class  ShopChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("xx","ShopChangeReceiver");
            List<ShopCar> listss= dbAdapter.getListShopCar(PreferenceHelper.readString(context,"shoppay","account","123"));
            num=0;
            money=0;
            for(ShopCar shopCar:listss){
                if(shopCar.count==0){

                }else{
                    num=num+shopCar.count;
                    money=money+Double.parseDouble(shopCar.discountmoney);
                }
            }
            tv_num.setText((int)num+"");
            tv_money.setText(StringUtil.twoNum(money+""));

        }
    }
    /**
     * 广播接收器
     *
     * @author len
     */
    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
//            String state = intent.getStringExtra("success");
//            Log.d("MsgReceiver", "MsgReceiver" + state);
//            if (state == null || state.equals("")) {
//
//            } else {
//                if (state.equals("success")) {
//                    jiesuanDialog.dismiss();
//                    weixinDialog.dismiss();
//                    jiesuan(ac,vipPayMsg);
//                } else {
//                    String msg = intent.getStringExtra("msg");
//                    Toast.makeText(ac,msg,Toast.LENGTH_SHORT).show();
//
//                }
//            }
        }

    }


    @Override
    protected void onDestroy() {
        // TODO 自动生成的方法存根
        super.onDestroy();
        if (intent != null) {

            stopService(intent);
        }

//        //关闭闹钟机制启动service
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int anHour =2 * 1000; // 这是一小时的毫秒数 60 * 60 * 1000
//        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
//        Intent i = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        manager.cancel(pi);
//        //注销广播
//        unregisterReceiver(msgReceiver);
        unregisterReceiver(shopchangeReceiver);
    }
}
