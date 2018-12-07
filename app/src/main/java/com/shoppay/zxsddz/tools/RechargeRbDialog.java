package com.shoppay.zxsddz.tools;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.shoppay.zxsddz.R;
import com.shoppay.zxsddz.adapter.RechargeLbChoseAdapter;
import com.shoppay.zxsddz.bean.RechargeLb;
import com.shoppay.zxsddz.http.InterfaceBack;

import java.util.List;

public class RechargeRbDialog {
    /**
     * 时间选择
     *
     * @param context
     * @param showingLocation 0：顶部 1：中间 2：底部 3：距离底部100dp 对话框的位置
     */
    public static void rechargeLbChoseDialog(final Context context, final List<RechargeLb> list,
                                             int showingLocation, final InterfaceBack handler) {
        final Dialog dialog;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_timechose, null);
        ListView listview = (ListView) view.findViewById(R.id.listview_timechose);
        RechargeLbChoseAdapter carListAdapter = new RechargeLbChoseAdapter(context, list);
        listview.setAdapter(carListAdapter);
        dialog = new Dialog(context, R.style.DialogNotitle1);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        int screenWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
        dialog.setContentView(view, new LinearLayout.LayoutParams(
                screenWidth - 100, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handler.onResponse(list.get(position));
                dialog.dismiss();
            }
        });
        Window window = dialog.getWindow();
        switch (showingLocation) {
            case 0:
                window.setGravity(Gravity.TOP); // 此处可以设置dialog显示的位置
                break;
            case 1:
                window.setGravity(Gravity.CENTER);
                break;
            case 2:
                window.setGravity(Gravity.BOTTOM);
                break;
            case 3:
                WindowManager.LayoutParams params = window.getAttributes();
                dialog.onWindowAttributesChanged(params);
                params.x = screenWidth - dip2px(context, 100);// 设置x坐标
                params.gravity = Gravity.TOP;
                params.y = dip2px(context, 45);// 设置y坐标
                Log.d("xx", params.y + "");
                window.setGravity(Gravity.TOP);
                window.setAttributes(params);
                break;
            default:
                window.setGravity(Gravity.CENTER);
                break;
        }
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
