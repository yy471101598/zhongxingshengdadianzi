package com.shoppay.zxsddz.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shoppay.zxsddz.R;
import com.shoppay.zxsddz.bean.NumShop;
import com.shoppay.zxsddz.bean.VipServece;
import com.shoppay.zxsddz.db.DBAdapter;
import com.shoppay.zxsddz.tools.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

public class NumAdapter extends BaseAdapter {
	private Context context;
	private List<VipServece> list;
	private LayoutInflater inflater;
	private DBAdapter dbAdapter;
	private Intent intent;
	public NumAdapter(Context context, List<VipServece> list) {
		this.context = context;
		if (list == null) {
			this.list = new ArrayList<VipServece>();
		} else {
			this.list = list;
		}
		inflater = LayoutInflater.from(context);
		dbAdapter=DBAdapter.getInstance(context);
		intent=new Intent("com.shoppay.wy.servecenumberchange");
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
		final ViewHolder	vh;
//		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_num, null);
			vh = new ViewHolder();
			vh.tv_name = (TextView) convertView
					.findViewById(R.id.item_tv_shopname);
			vh.tv_num = (TextView) convertView
					.findViewById(R.id.item_tv_num);
			vh.tv_synum = (TextView) convertView
					.findViewById(R.id.item_tv_synum);
			vh.img_add = (ImageView) convertView.findViewById(R.id.item_iv_add);
			vh.img_del = (ImageView) convertView.findViewById(R.id.item_iv_del);
			convertView.setTag(vh);
//		}else {
//			vh = (ViewHolder) convertView.getTag();
//		}
		final VipServece home = list.get(position);
		vh.tv_name.setText(home.GoodsName);
		vh.tv_synum.setText(home.CountNum);
		NumShop dbshop = dbAdapter.getNumShop(home.GoodsID);
		if (dbshop == null) {

		} else {
			if (dbshop.count != 0) {
				vh.tv_num.setVisibility(View.VISIBLE);
				vh.img_del.setVisibility(View.VISIBLE);
				vh.tv_num.setText(dbshop.count + "");
			}
		}
		vh.img_add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
						int num = Integer.parseInt(vh.tv_num.getText().toString());
						if (num == 0) {
							vh.tv_num.setVisibility(View.VISIBLE);
							vh.img_del.setVisibility(View.VISIBLE);
						}
						num = num + 1;
				        if(num>Integer.parseInt(home.CountNum)){
							Toast.makeText(context,"超过次数",Toast.LENGTH_SHORT).show();
						}else {
							context.sendBroadcast(intent);
							insertNumshop(home, num);
							vh.tv_num.setText(num + "");
						}
				}
		});
		vh.img_del.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int num=Integer.parseInt(vh.tv_num.getText().toString());
				num=num-1;
				if(num==0){
					vh.img_del.setVisibility(View.GONE);
					vh.tv_num.setVisibility(View.GONE);
					num=0;
				}
				insertNumshop(home,num);
				context.sendBroadcast(intent);
				vh.tv_num.setText(num+"");
			}
		});
		return convertView;
	}

	class ViewHolder {
		TextView tv_name,tv_synum,tv_num;
		ImageView img_add,img_del;
	}
	private  void insertNumshop(VipServece home,int num){
		List<NumShop> list=new ArrayList<>();
		NumShop numShop=new NumShop();
		numShop.account= PreferenceHelper.readString(context,"shoppay","account","123");
		numShop.CountDetailGoodsID=home.GoodsID;
		numShop.count=num;
		numShop.shopname=home.GoodsName;
		numShop.allnum=home.CountNum;
		list.add(numShop);
		dbAdapter.insertNumShopCar(list);
	}

}
