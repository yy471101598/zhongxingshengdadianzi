package com.shoppay.zxsddz;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.zxsddz.bean.SystemQuanxian;
import com.shoppay.zxsddz.bean.VipInfo;
import com.shoppay.zxsddz.bean.VipInfoMsg;
import com.shoppay.zxsddz.card.ReadCardOpt;
import com.shoppay.zxsddz.card.ReadCardOptHander;
import com.shoppay.zxsddz.http.InterfaceBack;
import com.shoppay.zxsddz.tools.ActivityStack;
import com.shoppay.zxsddz.tools.DialogUtil;
import com.shoppay.zxsddz.tools.LogUtils;
import com.shoppay.zxsddz.tools.NullUtils;
import com.shoppay.zxsddz.tools.PreferenceHelper;
import com.shoppay.zxsddz.tools.StringUtil;
import com.shoppay.zxsddz.tools.ToastUtils;
import com.shoppay.zxsddz.tools.UrlTools;
import com.shoppay.zxsddz.wxcode.MipcaActivityCapture;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

/**
 * Created by songxiaotao on 2017/6/30.
 */

public class VipChaxunActivity extends Activity {
    @Bind(R.id.img_left)
    ImageView imgLeft;
    @Bind(R.id.rl_left)
    RelativeLayout rlLeft;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.rl_right)
    RelativeLayout rlRight;
    @Bind(R.id.vip_tv_card)
    TextView vipTvCard;
    @Bind(R.id.vip_et_card)
    EditText vipEtCard;
    @Bind(R.id.vip_tv_name)
    TextView vipTvName;
    @Bind(R.id.vip_tv_jifen)
    TextView vipTvJifen;
    @Bind(R.id.vip_tv_vipyue)
    TextView vipTvVipyue;
    @Bind(R.id.vip_tv_vipdengji)
    TextView vipTvVipdengji;
    @Bind(R.id.vip_tv_phone)
    TextView vipTvPhone;
    @Bind(R.id.vip_tv_shopname)
    TextView vipTvShopname;
    @Bind(R.id.vip_tv_birthday)
    TextView vipTvBirthday;
    @Bind(R.id.vip_tv_sex)
    TextView vipTvSex;
    @Bind(R.id.vip_tv_createtime)
    TextView vipTvCreatetime;
    @Bind(R.id.vip_tv_state)
    TextView vipTvState;
    @Bind(R.id.vip_tv_guoqitime)
    TextView vipTvGuoqitime;
    @Bind(R.id.vip_tv_xiaofei)
    TextView vipTvXiaofei;
    @Bind(R.id.rl_tvcard)
    RelativeLayout rl_tvcard;
    @Bind(R.id.rl_etcard)
    RelativeLayout rl_card;
    @Bind(R.id.tv_tvcard)
    TextView tv_tvcard;
    private boolean isVipcar = false;
    private SystemQuanxian sysquanxian;
    private MyApplication app;
    private boolean isSuccess = false;
    private Activity ac;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    VipInfo info = (VipInfo) msg.obj;
                    vipTvName.setText(info.getMemName());
                    vipTvVipyue.setText(StringUtil.twoNum(info.getMemMoney()));
                    vipTvJifen.setText(info.getMemPoint());
                    vipTvVipdengji.setText(info.getLevelName());
                    vipTvPhone.setText(info.getMemMobile());
                    vipTvShopname.setText(info.getShopName());
                    vipTvBirthday.setText(info.getBirthday());
                    vipTvSex.setText(info.getMemSex());
                    vipTvCreatetime.setText(info.getMemCreateTime());
                    vipTvState.setText(info.getState());
                    vipTvGuoqitime.setText(info.getPastTime());
                    vipTvXiaofei.setText(StringUtil.twoNum(info.getMemConsumeMoney()));
                    PreferenceHelper.write(ac, "shoppay", "memid", info.getMemID());
                    PreferenceHelper.write(ac, "shoppay", "vipcar", vipEtCard.getText().toString());
                    PreferenceHelper.write(ac, "shoppay", "Discount", info.getDiscount());
                    PreferenceHelper.write(ac, "shoppay", "DiscountPoint", info.getDiscountPoint());
                    PreferenceHelper.write(ac, "shoppay", "jifen", info.getMemPoint());
                    isSuccess = true;
                    break;
                case 2:
                    vipTvName.setText("");
                    vipTvVipdengji.setText("");
                    vipTvJifen.setText("");
                    vipTvVipyue.setText("");
                    vipTvPhone.setText("");
                    vipTvShopname.setText("");
                    vipTvBirthday.setText("");
                    vipTvSex.setText("");
                    vipTvCreatetime.setText("");
                    vipTvState.setText("");
                    vipTvGuoqitime.setText("");
                    vipTvXiaofei.setText("");
                    isSuccess = false;
                    PreferenceHelper.write(ac, "shoppay", "memid", "123");
                    PreferenceHelper.write(ac, "shoppay", "vipcar", "123");
                    break;
            }
        }
    };
    private Dialog dialog;
    private String editString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vipchaxun);
        ac = this;
        ButterKnife.bind(this);
        dialog = DialogUtil.loadingDialog(ac, 1);
        PreferenceHelper.write(MyApplication.context, "shoppay", "viptoast", "未查询到会员");
        ActivityStack.create().addActivity(VipChaxunActivity.this);
//        obtainVipRecharge();
        app= (MyApplication) getApplication();
        sysquanxian=app.getSysquanxian();
        if (Integer.parseInt(NullUtils.noNullHandle(sysquanxian.isvipcard).toString()) == 0) {
            rl_tvcard.setVisibility(View.GONE);
            rl_card.setVisibility(View.VISIBLE);
            isVipcar = false;
        } else {
            rl_tvcard.setVisibility(View.VISIBLE);
            rl_card.setVisibility(View.GONE);
            isVipcar = true;
        }
        tvTitle.setText("会员查询");
        vipEtCard.addTextChangedListener(new TextWatcher() {
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


//        PreferenceHelper.write(getApplicationContext(), "PayOk", "time", "false");
//        //动态注册广播接收器
//        msgReceiver = new MsgReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("com.example.communication.RECEIVER");
//        registerReceiver(msgReceiver, intentFilter);
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
        if (isVipcar) {
            dialog.show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("MemCard", editString);
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(ac, "?Source=3", "GetMem");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    if (isVipcar) {
                        dialog.dismiss();
                    }
                    LogUtils.d("xxVipinfoS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        Gson gson = new Gson();
                        VipInfoMsg infomsg = gson.fromJson(new String(responseBody, "UTF-8"), VipInfoMsg.class);
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
                    if (isVipcar) {
                        dialog.dismiss();
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    handler.sendMessage(msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (isVipcar) {
                    dialog.dismiss();
                }
                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isVipcar) {
            new ReadCardOptHander(new InterfaceBack() {
                @Override
                public void onResponse(Object response) {
                    tv_tvcard.setText(response.toString());
                    editString = tv_tvcard.getText().toString();
                    ontainVipInfo();
                }

                @Override
                public void onErrorResponse(Object msg) {

                }
            });
        } else {
            new ReadCardOpt(vipEtCard);
        }
    }

    @Override
    protected void onStop() {
        try
        {
            if (isVipcar) {
                new ReadCardOptHander().overReadCard();
            } else {
                new ReadCardOpt().overReadCard();
            }
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


    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @OnClick({R.id.rl_left, R.id.rl_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_left:
                finish();
                break;
            case R.id.rl_right:
                if (ContextCompat.checkSelfPermission(ac, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(ac, Manifest.permission.CAMERA)) {
                        ToastUtils.showToast(ac, "您已经拒绝过一次");
                    }
                    ActivityCompat.requestPermissions(ac, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSIONS_REQUEST_CODE);
                } else {//有权限直接调用系统相机拍照
                    Intent mipca = new Intent(ac, MipcaActivityCapture.class);
                    startActivityForResult(mipca, 111);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 111:
                if (resultCode == RESULT_OK) {
                    if (isVipcar) {
                        tv_tvcard.setText(data.getStringExtra("codedata"));
                        editString = data.getStringExtra("codedata");
                        ontainVipInfo();
                    } else {
                        vipEtCard.setText(data.getStringExtra("codedata"));
                    }
                }
                break;

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
//                    weixinDialog.dismiss();
//                     vipRecharge();
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
//        if (intent != null) {
//
//            stopService(intent);
//        }
//
//        //关闭闹钟机制启动service
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        int anHour =2 * 1000; // 这是一小时的毫秒数 60 * 60 * 1000
//        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
//        Intent i = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        manager.cancel(pi);
//        //注销广播
//        unregisterReceiver(msgReceiver);
    }
}
