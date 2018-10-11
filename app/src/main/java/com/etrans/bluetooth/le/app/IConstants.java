package com.etrans.bluetooth.le.app;

public class IConstants {
    public static final boolean isAnimiation = true;//全局动画
    public static final String BASE_URL = " http://14.23.41.188:15182/school_app/";
    public static final String defaultAvart = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1524041369416&di=83c57e1ee14bee4c325feadc74690154&imgtype=0&src=http%3A%2F%2Fimg03.tooopen.com%2Fimages%2F20130522%2Ftooopen_13471665.jpg";
    public static String BASE = "2a2a";                //起始符
    public static String CMD_QUERY = "03";           //参数查询命令
    public static String CMD_SET = "02";             //参数设置命令
    public static String CMD_FE = "FE";              // 命令
    public static String AES01 = "01";               // 加密
    public static String VIN = "01";                 // VIN号
    public static String PHONENUM = "02";            // 手机号
    public static String IDNUM = "03";               // 终端号
    public static String CARNUM = "04";              //车牌号
    public static String IP01 = "05";                //IP号1
    public static String IP02 = "06";                //IP号2
    public static String PORT01 = "0a";              //端口1
    public static String PORT02 = "0b";              //端口2
    public static String SOFTWARE_VERSION = "0f";    //程序版本
    public static String HARDWARE_VERSION = "10";    //硬件版本

    //查询所有数据 2a2a 03 fe 01 03 01 02 03 cs
    public static String QUERY = BASE+CMD_QUERY+CMD_FE+AES01;
    public static String QUERY3  = VIN+PHONENUM+IDNUM;    //查询前面三个数据,03代表数据单元长度
    public static String QUERYALL = VIN+PHONENUM+IDNUM+CARNUM+IP01+PORT01+SOFTWARE_VERSION+HARDWARE_VERSION;    //查询全部数据


    //查询所有数据 2a2a 03 fe 01 03 01 02 03 cs
    public static String SET = BASE+CMD_SET+CMD_FE+AES01;




    public static String MS_LOGINDATA = "MS_LOGIN";
    public static String MS_ISLOGIN = "MS_ISLOGIN";

}
