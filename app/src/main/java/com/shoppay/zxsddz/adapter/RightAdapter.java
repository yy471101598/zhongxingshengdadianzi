package com.shoppay.zxsddz.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.zxsddz.R;
import com.shoppay.zxsddz.bean.Shop;
import com.shoppay.zxsddz.bean.ShopCar;
import com.shoppay.zxsddz.bean.Zhekou;
import com.shoppay.zxsddz.db.DBAdapter;
import com.shoppay.zxsddz.http.InterfaceBack;
import com.shoppay.zxsddz.tools.CommonUtils;
import com.shoppay.zxsddz.tools.DialogUtil;
import com.shoppay.zxsddz.tools.LogUtils;
import com.shoppay.zxsddz.tools.PreferenceHelper;
import com.shoppay.zxsddz.tools.ShopNumChoseDialog;
import com.shoppay.zxsddz.tools.StringUtil;
import com.shoppay.zxsddz.tools.UrlTools;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class RightAdapter extends BaseAdapter {
    private Context context;
    private List<Shop> list;
    private LayoutInflater inflater;
    private Intent intent;
    private Dialog dialog;
    private DBAdapter dbAdapter;

    public RightAdapter(Context context, List<Shop> list) {
        this.context = context;
        if (list == null) {
            this.list = new ArrayList<Shop>();
        } else {
            this.list = list;
        }
        inflater = LayoutInflater.from(context);
        intent = new Intent("com.shoppay.wy.balancechange");
        dialog = DialogUtil.loadingDialog(context, 1);
        dbAdapter = DBAdapter.getInstance(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final ViewHolder vh;
        convertView = inflater.inflate(R.layout.item_balanceright, null);
        vh = new ViewHolder();
        vh.tv_name = (TextView) convertView
                .findViewById(R.id.item_tv_shopname);
        vh.tv_num = (TextView) convertView
                .findViewById(R.id.item_tv_num);
        vh.tv_money = (TextView) convertView
                .findViewById(R.id.item_tv_money);
        vh.img_add = (ImageView) convertView.findViewById(R.id.item_iv_add);
        vh.img_del = (ImageView) convertView.findViewById(R.id.item_iv_del);
        convertView.setTag(vh);
        final Shop home = list.get(position);
        vh.tv_name.setText(home.GoodsName);
        vh.tv_money.setText(home.GoodsPrice);
        ShopCar dbshop = dbAdapter.getShopCar(home.GoodsID);
        if (dbshop == null) {

        } else {
            if (dbshop.count != 0) {
                vh.tv_num.setVisibility(View.VISIBLE);
                vh.img_del.setVisibility(View.VISIBLE);
                vh.tv_num.setText(dbshop.count + "");
            }
        }
        vh.tv_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShopNumChoseDialog.numchoseDialog(context, 1, Integer.parseInt(vh.tv_num.getText().toString()), Integer.parseInt(home.Number), home.GoodsType, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        vh.tv_num.setText((String) response);
                        if (PreferenceHelper.readBoolean(context, "shoppay", "isSan", true)) {
                            insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), null, home, Integer.parseInt((String) response));
                        } else {
                            Zhekou zk = new Zhekou();
                            ShopCar shopCar = dbAdapter.getShopCar(home.GoodsID);
                            zk.DiscountPrice = shopCar.discount;
                            zk.GoodsPoint = shopCar.pointPercent;
                            insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), zk, home, Integer.parseInt((String) response));
                        }
                    }

                    @Override
                    public void onErrorResponse(Object msg) {

                    }
                });
            }
        });
        vh.img_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceHelper.readBoolean(context, "shoppay", "isSan", true)) {

                    int num = Integer.parseInt(vh.tv_num.getText().toString());
                    if (num == 0) {
                        vh.tv_num.setVisibility(View.VISIBLE);
                        vh.img_del.setVisibility(View.VISIBLE);
                    }
                    ShopCar shopCar = dbAdapter.getShopCar(home.GoodsID);
                    if (shopCar == null) {
                        if (Integer.parseInt(home.Number) < 1) {
                            if (home.GoodsType.equals("1")) {
                                num = 1;
                                vh.tv_num.setText(num + "");
                                insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), null, home, num);
                            } else {
                                num = 0;
                                Toast.makeText(context, "该商品的最大库存量为" + home.Number, Toast.LENGTH_SHORT).show();
                                vh.tv_num.setVisibility(View.GONE);
                                vh.img_del.setVisibility(View.GONE);
                            }
                        } else {
                            num = 1;
                            vh.tv_num.setText(num + "");
                            insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), null, home, num);
                        }
                    } else {
                        num = num + 1;
                        if (Integer.parseInt(home.Number) < num) {
                            if (home.GoodsType.equals("1")) {
                                vh.tv_num.setText(num + "");
                                insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), null, home, num);
                            } else {
                                num = num - 1;
                                Toast.makeText(context, "该商品的最大库存量为" + home.Number, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            vh.tv_num.setText(num + "");
                            insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), null, home, num);
                        }
                    }
                } else {
                    if (PreferenceHelper.readString(context, "shoppay", "memid", "").equals("")) {
                        Toast.makeText(context, PreferenceHelper.readString(context, "shoppay", "viptoast", "未查询到会员"), Toast.LENGTH_SHORT).show();

                    } else {
                        int num = Integer.parseInt(vh.tv_num.getText().toString());
                        if (num == 0) {
                            vh.tv_num.setVisibility(View.VISIBLE);
                            vh.img_del.setVisibility(View.VISIBLE);
                        }
                        ShopCar shopCar = dbAdapter.getShopCar(home.GoodsID);
                        if (shopCar == null) {
                            if (Integer.parseInt(home.Number) < 1) {
                                if (home.GoodsType.equals("1")) {
                                    num = 1;
                                    vh.tv_num.setText(num + "");
                                    obtainShopZhekou(home);
                                } else {
                                    num = 0;
                                    Toast.makeText(context, "该商品的最大库存量为" + home.Number, Toast.LENGTH_SHORT).show();
                                    vh.tv_num.setVisibility(View.GONE);
                                    vh.img_del.setVisibility(View.GONE);
                                }
                            } else {
                                num = 1;
                                vh.tv_num.setText(num + "");
                                obtainShopZhekou(home);
                            }
                        } else {
                            num = num + 1;
                            if (Integer.parseInt(home.Number) < num) {
                                if (home.GoodsType.equals("1")) {
                                    vh.tv_num.setText(num + "");
                                    Zhekou zk = new Zhekou();
                                    zk.DiscountPrice = shopCar.discount;
                                    zk.GoodsPoint = shopCar.pointPercent;
                                    insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), zk, home, num);
                                } else {
                                    num = num - 1;
                                    Toast.makeText(context, "该商品的最大库存量为" + home.Number, Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                vh.tv_num.setText(num + "");
                                Zhekou zk = new Zhekou();
                                zk.DiscountPrice = shopCar.discount;
                                zk.GoodsPoint = shopCar.pointPercent;
                                insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), zk, home, num);
                            }
                        }
                    }
                }
            }
        });
        vh.img_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = Integer.parseInt(vh.tv_num.getText().toString());
                num = num - 1;
                if (num == 0) {
                    vh.img_del.setVisibility(View.GONE);
                    vh.tv_num.setVisibility(View.GONE);
                }
                vh.tv_num.setText(num + "");
                if (PreferenceHelper.readBoolean(context, "shoppay", "isSan", true)) {
                    ShopCar shopCar = dbAdapter.getShopCar(home.GoodsID);
                    Zhekou zk = new Zhekou();
                    zk.DiscountPrice = shopCar.discount;
                    zk.GoodsPoint = shopCar.pointPercent;
                    insertShopCar(true, zk, home, num);
                } else {
                    ShopCar shopCar = dbAdapter.getShopCar(home.GoodsID);
                    Zhekou zk = new Zhekou();
                    zk.DiscountPrice = shopCar.discount;
                    zk.GoodsPoint = shopCar.pointPercent;
                    insertShopCar(false, zk, home, num);
                }
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tv_name, tv_money, tv_num;
        ImageView img_add, img_del;
    }

    private void obtainShopZhekou(final Shop shop) {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        if (PreferenceHelper.readBoolean(context, "shoppay", "isSan", true)) {
            params.put("memid", "0");
        } else {
            params.put("memid", PreferenceHelper.readString(context, "shoppay", "memid", "0"));
        }
        params.put("GoodsCode", shop.GoodsCode);
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(context, "?Source=3", "GetGoodsInfos");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    dialog.dismiss();
                    LogUtils.d("xxshopzkS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Zhekou>>() {
                        }.getType();
                        List<Zhekou> zhekoulist = gson.fromJson(jso.getString("vdata"), listType);
                        //加入购物车
                        insertShopCar(PreferenceHelper.readBoolean(context, "shoppay", "isSan", true), zhekoulist.get(0), shop, 1);

                    } else {
                        Toast.makeText(context, "获取商品折扣失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "获取商品折扣失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                LogUtils.d("xxshopzkE", new String(responseBody));
                Toast.makeText(context, "获取商品折扣失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertShopCar(Boolean isSan, Zhekou zk, Shop shop, int num) {
        //加入购物车
        List<ShopCar> li = new ArrayList<ShopCar>();
        ShopCar shopCar = new ShopCar();
        shopCar.account = PreferenceHelper.readString(context, "shoppay", "account", "123");
        shopCar.count = num;
        if (isSan) {
            shopCar.discount = shop.GoodsPrice;
            shopCar.discountmoney = StringUtil.twoNum(Double.parseDouble(shop.GoodsPrice) * num + "");
            shopCar.point = 0;
            shopCar.pointPercent = "0";
            shopCar.goodspoint = 0;
        } else {
            double dimoney = num * Double.parseDouble(zk.DiscountPrice);
            shopCar.discount = zk.DiscountPrice;
            shopCar.discountmoney = StringUtil.twoNum(dimoney + "");
            shopCar.point = Double.parseDouble(CommonUtils.multiply(zk.GoodsPoint, num + ""));
            shopCar.pointPercent = zk.GoodsPoint;
            shopCar.goodspoint = Integer.parseInt(zk.GoodsPoint);
        }
        shopCar.goodsid = shop.GoodsID;
        shopCar.goodsclassid = shop.GoodsClassID;
        shopCar.goodsType = shop.GoodsType;
        shopCar.price = shop.GoodsPrice;
        shopCar.shopname = shop.GoodsName;
        li.add(shopCar);
        dbAdapter.insertShopCar(li);
//		intent.putExtra("shopclass",shop.GoodsClassID);
//		intent.putExtra("num",num+"");
        context.sendBroadcast(intent);
    }
}
