package com.shoppay.szvipnewzh.bean;

import java.io.Serializable;

/**
 * Created by songxiaotao on 2018/1/17.
 */

public class SystemQuanxian implements Serializable{
//    1.1是否允许修改折后单价
//    1.2是否允许余额支付
//    1.3是否允许现金支付
//    1.4是否允许银联支付
//    1.5是否允许支付宝付
//    1.6是否允许微信支付
//    1.7是否启用密码验证

    public int ischangemoney;
    public int isyue;
    public int isxianjin;
    public int isyinlian;
    public int isweixin;
    public int iszhifubao;
    public int ispassword;
    public int isqita;
}
