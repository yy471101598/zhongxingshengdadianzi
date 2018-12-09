package com.shoppay.zxsddz.card;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.shoppay.zxsddz.MyApplication;
import com.shoppay.zxsddz.http.InterfaceBack;
import com.shoppay.zxsddz.tools.LogUtils;
import com.sunmi.payservice.hardware.aidl.HardwareOpt;
import com.sunmi.payservice.hardware.aidl.ReadCardCallback;
import com.sunmi.payservice.hardware.aidl.bean.PayCardInfo;

/**
 * sunmi P1N检卡读卡
 * 联系方式QQ：398320879
 */
public class ReadCardOptHander extends Activity {

    private static final String TAG = "ReadCardOpt";

    private int time = 86400;   //读卡超时 24小时
    private String mCardNumber = ""; //通用mifare感应卡卡号和Mag磁条卡卡号
    private String mMifareCardNumber = ""; //非通用mifare感应卡卡号
    private HardwareOpt mHardwareOpt; //sunmiP1n 硬件接口操作模块
    private InterfaceBack back;

    public ReadCardOptHander() {

    }

    /**
     * 在需要刷卡的activity中调用该构造函数
     */
    public ReadCardOptHander(InterfaceBack back) {
        this.back = back;

        mHardwareOpt = MyApplication.sHardwareOpt;

        if (mHardwareOpt != null) {
            try {
                /**
                 * 1：Mag磁条卡   8：mifare感应卡
                 * 开始检卡
                 */
                mHardwareOpt.checkCard(1 | 8, readCardCallback, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检卡回调
     */
    private ReadCardCallback readCardCallback = new ReadCardCallback.Stub() {
        @Override
        public void onCardDetected(PayCardInfo payCardInfo) throws RemoteException {
            Log.d(TAG, "读取成功: ");
            //解析并读取卡号
            Message message = new Message();
            message.what = 0x01;
            message.obj = payCardInfo;
            mHandler.sendMessage(message);

            //读卡成功后反复调用
            try {
                mHardwareOpt.checkCard(1 | 8, readCardCallback, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int i, String s) throws RemoteException {
            if (!s.equals("重复调用")) Log.e(TAG, "onError:" + i + " " + s);

            //读卡失败或读卡超时重新调用检卡程序
            try {
                mHardwareOpt.checkCard(1 | 8, readCardCallback, time);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStartCheckCard() throws RemoteException {
            Log.e(TAG, "onStartCheckCard:正在检卡");
        }

    };


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    PayCardInfo payCardInfo = (PayCardInfo) msg.obj;
                    LogUtils.d("xx", payCardInfo.toString());
                    if (payCardInfo.cardType == 8) {
                        /***************Mifare 感应卡***********************/
                        String uuid = payCardInfo.uuid;
                        //取每两位十六进制逆向排列为市面上的通用卡号
                        String common_uuid = uuid.substring(6, 8) + uuid.substring(4, 6) + uuid.substring(2, 4) + uuid.substring(0, 2);

                        /**
                         * 正式使用时，mCardNumber是市面上90%读卡器识别出来的卡号
                         * mMifareCardNumber是市面上少数10%读卡器识别出来的卡号，请自行更换。
                         * 默认显示通用卡号
                         */
                        //通用卡号
                        mCardNumber = String.valueOf(Long.parseLong(common_uuid, 16));
                        //非通用卡号
                        mMifareCardNumber = String.valueOf(Long.parseLong(payCardInfo.uuid, 16));

                        /**
                         * 卡号不足10位首位自动补0
                         */
                        if (mCardNumber.length() < 10) {
                            int i = 10 - mCardNumber.length();
                            for (; i > 0; i--) {
                                mCardNumber = "0" + mCardNumber;
                            }
                        }
                        back.onResponse(mCardNumber);
//                        textView.setText(mCardNumber);
                    } else {
                        /***************Mag磁条卡*********************/
                        mCardNumber = payCardInfo.track2;
//                        if (mCardNumber.length() < 10) {
//                            int i = 10 - mCardNumber.length();
//                            for (; i > 0; i--) {
//                                mCardNumber = "0" + mCardNumber;
//                            }
//                        }
                        if (null == mCardNumber) {
                            back.onResponse("");
                        } else {
                            back.onResponse(mCardNumber);
                        }
//                        textView.setText(mCardNumber);
                    }
                    break;
            }
        }
    };


    @Override
    public void onBackPressed() {
        try {
            // 终止检卡
            overReadCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    /**
     * 终止检卡读卡程序
     *
     * @throws RemoteException
     */
    public void overReadCard() throws RemoteException {
        try {
            MyApplication.sHardwareOpt.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
