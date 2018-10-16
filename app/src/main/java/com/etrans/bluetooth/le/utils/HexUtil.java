package com.etrans.bluetooth.le.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.etrans.bluetooth.le.DeviceControlActivity;
import com.etrans.bluetooth.le.app.IConstants;
import com.etrans.bluetooth.le.bean.ResultQuerybean;
import com.etrans.bluetooth.le.bean.ResultSetbean;

/**
 * @Description: 十六进制转换类
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 16/8/7 21:57.
 */
public class HexUtil {

    private final static String TAG = "HexUtil";
    /**
     * 用于建立十六进制字符的输出的小写字符数组
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        if (data == null) {
            return "";
        }
        return new String(encodeHex(data, toDigits));
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param data
     * @return
     */
    public static byte[] decodeHex(String data) {
        if (data == null) {
            return new byte[0];
        }
        return decodeHex(data.toCharArray());
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param data 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException 当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

    /**
     * 截取字节数组
     *
     * @param src   byte []  数组源  这里填16进制的 数组
     * @param begin 起始位置 源数组的起始位置。0位置有效
     * @param count 截取长度
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        System.arraycopy(src, begin, bs, 0, count);  // bs 目的数组  0 截取后存放的数值起始位置。0位置有效
        return bs;
    }

    /**
     * int转byte数组
     *
     * @param bb
     * @param x
     * @param index 第几位开始
     * @param flag  标识高低位顺序，高位在前为true，低位在前为false
     */
    public static void intToByte(byte[] bb, int x, int index, boolean flag) {
        if (flag) {
            bb[index + 0] = (byte) (x >> 24);
            bb[index + 1] = (byte) (x >> 16);
            bb[index + 2] = (byte) (x >> 8);
            bb[index + 3] = (byte) (x >> 0);
        } else {
            bb[index + 3] = (byte) (x >> 24);
            bb[index + 2] = (byte) (x >> 16);
            bb[index + 1] = (byte) (x >> 8);
            bb[index + 0] = (byte) (x >> 0);
        }
    }

    /**
     * byte数组转int
     *
     * @param bb
     * @param index 第几位开始
     * @param flag  标识高低位顺序，高位在前为true，低位在前为false
     * @return
     */
    public static int byteToInt(byte[] bb, int index, boolean flag) {
        if (flag) {
            return (int) ((((bb[index + 0] & 0xff) << 24)
                    | ((bb[index + 1] & 0xff) << 16)
                    | ((bb[index + 2] & 0xff) << 8)
                    | ((bb[index + 3] & 0xff) << 0)));
        } else {
            return (int) ((((bb[index + 3] & 0xff) << 24)
                    | ((bb[index + 2] & 0xff) << 16)
                    | ((bb[index + 1] & 0xff) << 8)
                    | ((bb[index + 0] & 0xff) << 0)));
        }
    }


    /**
     * 字节数组逆序
     *
     * @param data
     * @return
     */
    public static byte[] reverse(byte[] data) {
        byte[] reverseData = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            reverseData[i] = data[data.length - 1 - i];
        }
        return reverseData;
    }

    /**
     * 蓝牙传输 16进制 高低位 读数的 转换
     *
     * @param data  截取数据源，字节数组
     * @param index 截取数据开始位置
     * @param count 截取数据长度，只能为2、4、8个字节
     * @param flag  标识高低位顺序，高位在前为true，低位在前为false
     * @return
     */
    public static long byteToLong(byte[] data, int index, int count, boolean flag) {
        long lg = 0;
        if (flag) {
            switch (count) {
                case 2:
                    lg = ((((long) data[index + 0] & 0xff) << 8)
                            | (((long) data[index + 1] & 0xff) << 0));
                    break;

                case 4:
                    lg = ((((long) data[index + 0] & 0xff) << 24)
                            | (((long) data[index + 1] & 0xff) << 16)
                            | (((long) data[index + 2] & 0xff) << 8)
                            | (((long) data[index + 3] & 0xff) << 0));
                    break;

                case 8:
                    lg = ((((long) data[index + 0] & 0xff) << 56)
                            | (((long) data[index + 1] & 0xff) << 48)
                            | (((long) data[index + 2] & 0xff) << 40)
                            | (((long) data[index + 3] & 0xff) << 32)
                            | (((long) data[index + 4] & 0xff) << 24)
                            | (((long) data[index + 5] & 0xff) << 16)
                            | (((long) data[index + 6] & 0xff) << 8)
                            | (((long) data[index + 7] & 0xff) << 0));
                    break;
            }
            return lg;
        } else {
            switch (count) {
                case 2:
                    lg = ((((long) data[index + 1] & 0xff) << 8)
                            | (((long) data[index + 0] & 0xff) << 0));
                    break;
                case 4:
                    lg = ((((long) data[index + 3] & 0xff) << 24)
                            | (((long) data[index + 2] & 0xff) << 16)
                            | (((long) data[index + 1] & 0xff) << 8)
                            | (((long) data[index + 0] & 0xff) << 0));
                    break;
                case 8:
                    lg = ((((long) data[index + 7] & 0xff) << 56)
                            | (((long) data[index + 6] & 0xff) << 48)
                            | (((long) data[index + 5] & 0xff) << 40)
                            | (((long) data[index + 4] & 0xff) << 32)
                            | (((long) data[index + 3] & 0xff) << 24)
                            | (((long) data[index + 2] & 0xff) << 16)
                            | (((long) data[index + 1] & 0xff) << 8)
                            | (((long) data[index + 0] & 0xff) << 0));
                    break;
            }
            return lg;
        }
    }


    public static String senddata(String data) {
        String dataNum = data.substring(0, 2); //得到数据名称
        int datalen = ByteUtils.HexStringTointeger(data.substring(2, 4)); //单数据长度
        String data6 = data.substring(0, datalen * 2 + 4); //截取该长度数据
        return data6;
    }


    /**
     * 查询应答
     * @param data
     * @return
     */
    public static ResultQuerybean HexqueryData(String data) { //2A2A03 01 01 00 6B 02 0100113030303030303030303030303030303030 02000C303030303133323831340000 75
        Log.e(TAG, "HexqueryData: data = "+data);
        StringBuilder mqueryOutput = new StringBuilder();
        ResultQuerybean resultQuerybean = new ResultQuerybean();
        if (data.contains("2a2a")) {
            String cmd = data.substring(6, 8);
            //截取参数的数据 140103343536020331333503033132330403373839
            String data1 = data.substring(12, data.length()); //长度+数据 6B+数据
            //这个是真实数据
            String data4 = data1.substring(4, data1.length());//0100 11 3030303030303030303030303030303030 02 00 0C 303030303133323831340000
                    if(cmd.equals("01")){
                    Log.i(TAG, "HexqueryData: 这是查询应答");
                    int num = data4.length();
                    int index = 0;
                    for (int i = 0; i < data4.length(); i = index) {
                        if (num - i > index) {
                            //获取长度
                            int len = ByteUtils.HexStringTointeger(data4.substring(4, 6)); //第一个数据单元的长度11
                            String data2 = data4.substring(0, len * 2 + 6); //0到长度加6，这是第一个数据单元
                            mqueryOutput.append(data2).append("\n");//放进缓存
                            data4 = data4.substring(data2.length(), data4.length());
                            index = data2.length();//处理过的长度 0100 11 3030303030303030303030303030303030
                        }
                    }
                    /**
                     * 010431323334
                     0206313233343536
                     0303313233
                     04053132333435
                     */
                    if (mqueryOutput.toString() != null) {
                        String[] temp = null;
                        temp = mqueryOutput.toString().split("\n");
                        for (String dataInfo : temp) { //遍历配对列表

                            switch (dataInfo.substring(0, 2)) {
                                case "01":
                                    String vin = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setVin_Num(vin != null ? vin : "");
                                    break;
                                case "02":
                                    String phone = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setPhone_Num(phone != null ? phone : "");
                                    break;
                                case "03":
                                    String ID = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setID_Num(ID != null ? ID : "");
                                    break;
                                case "04":
                                    String carNum = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setCar_Num(carNum != null ? carNum : "");
                                    break;
                                case "05":
                                    String IP1 = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setIP1(IP1 != null ? IP1 : "");
                                    break;
                                case "0a":
                                    String port1 = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setPort1(port1 != null ? port1 : "");
                                    break;
                                case "06":
                                    String IP2 = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setIP2(IP2 != null ? IP2 : "");
                                    break;
                                case "0b":
                                    String port2 = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setPort2(port2 != null ? port2 : "");
                                    break;
                                case "0f":
                                    String software_ver = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setSoftware_ver(software_ver != null ? software_ver : "");
                                    break;
                                case "10":
                                    String hardware_ver = dataInfo.substring(6, dataInfo.length());
                                    resultQuerybean.setHardware_ver(hardware_ver != null ? hardware_ver : "");
                                    break;
                            }
                        }
                    }
                }
        }

        return resultQuerybean;
    }

    /**
     * 设置应答
     * @param data
     * @return
     */
    public static ResultSetbean HexsetData(String data) { //2A2A02010100 09 0401000201030104000B
        Log.e(TAG, "HexsetData: data = "+data);
        StringBuilder msetOutput = new StringBuilder();
        ResultSetbean resultSetbean = new ResultSetbean();
        if (data.contains("2a2a")) {
            String cmd = data.substring(6, 8); //应答
            //截取参数的数据 09 0401000201030104000B
            String data1 = data.substring(12, data.length()); //长度+数据 09+数据 09 04 01000201030104000B
            //这个是真实数据
            String data4 = data1.substring(4, data1.length());//0100 0201 0301 0400 真实数据
                    if(cmd.equals("01")){
                    Log.i(TAG, "HexsetData: 这是设置应答");
                    int num = data4.length();//02010100 09 0401000201030104000B
                    int index = 0;
                    for (int i = 0; i < data4.length(); i = index) {
                        if (num - i > index) {
                            //获取长度
//                            int len = ByteUtils.HexStringTointeger(data4.substring(4, 6));
                            String data2 = data4.substring(0, 4); //0100
                            msetOutput.append(data2).append("\n");//放进缓存
                            data4 = data4.substring(data2.length(), data4.length());
                            index = data2.length();//处理过的长度 010334353602
                        }
                    }
                    /**
                     * 01 00
                     * 02 01
                     * 03 01
                     * 04 00
                     */
                    if (msetOutput.toString() != null) {
                        String[] temp = null;
                        temp = msetOutput.toString().split("\n");
                        for (String dataInfo : temp) { //遍历配对列表
                            switch (dataInfo.substring(0, 2)) {
                                case "01":
                                    String vin = dataInfo.substring(2, 4);//返回01或者00
                                    resultSetbean.setVin_Num(vin.equals("00") ? true : false);
                                    break;
                                case "02":
                                    String phone = dataInfo.substring(2, 4);
                                    resultSetbean.setPhone_Num(phone.equals("00") ? true : false);
                                    break;
                                case "03":
                                    String ID = dataInfo.substring(2, 4);
                                    resultSetbean.setID_Num(ID.equals("00") ? true : false);
                                    break;
                                case "04":
                                    String carNum = dataInfo.substring(2, 4);
                                    resultSetbean.setCar_Num(carNum.equals("00") ? true : false);
                                    break;
                                case "05":
                                    String IP1 = dataInfo.substring(2, 4);
                                    resultSetbean.setIP1(IP1.equals("00") ? true : false);
                                    break;
                                case "0a":
                                    String port1 = dataInfo.substring(2, 4);
                                    resultSetbean.setPort1(port1.equals("00") ? true : false);
                                    break;
                                case "06":
                                    String IP2 = dataInfo.substring(2, 4);
                                    resultSetbean.setIP2(IP2.equals("00") ? true : false);
                                    break;
                                case "0b":
                                    String port2 = dataInfo.substring(2, 4);
                                    resultSetbean.setPort2(port2.equals("00") ? true : false);
                                    break;
                                case "0f":
                                    String software_ver = dataInfo.substring(2, 4);
                                    resultSetbean.setSoftware_ver(software_ver.equals("00") ? true : false);
                                    break;
                                case "10":
                                    String hardware_ver = dataInfo.substring(2, 4);
                                    resultSetbean.setHardware_ver(hardware_ver.equals("00") ? true : false);
                                    break;
                            }
                        }
                    }
                }
        }

        return resultSetbean;
    }


    /**
     * 查询
     */
    public static void query() {
        String str = ByteUtils.Decimal0(IConstants.QUERYALL.length() / 2);//个数 08
        String SendData = IConstants.QUERY + //2a2a03FE01 00090801020304050a0f10
                "00" + ByteUtils.integerToHexString(IConstants.QUERYALL.length() / 2 + str.length() / 2) + //长度hex值,总长度0009
//                        ByteUtils.Decimal0(IConstants.QUERYALL.length())+
                str +//个数，命令总个数 08
                IConstants.QUERYALL;//01020304050a0f10
        //2a2a 03 fe 01 //上文
//                String str1 = IConstants.QUERY3;//01 02 03  //下文
//                String str2 = str1.length()+"";//01 02 03  //下文
//                ByteUtils.Decimal0(str1.length());

        //2a2a03fe0103010203cs 查询前面三个参数
        String validate_code = ByteUtils.checkXor(SendData.substring(4, SendData.length()));//验证码   cs
        SendData += validate_code;//补上验证码 2a2a03FE010801020304050a0f10E0

        /**
         * 232300 1a 01 0000000000000000000000000000 ff
         2b2b00 2a2a03FE010801020304050a0f10E000 ff
         */
        /**
         * 起始符 232300
         * 长度  1a
         * 包数  01
         * 补零  000000....
         * 校验 ff
         */

        StringBuilder headInfo = new StringBuilder();
        headInfo.delete(0, headInfo.length());//删除之前的StringBuilder
        headInfo.append("232300");
        headInfo.append(ByteUtils.integerToHexString(SendData.length() / 2));//长度hex,起始符的总长度
        headInfo.append(ByteUtils.integerToHexString((int) Math.ceil(SendData.length() / 32.0))); //包数
        String data = ByteUtils.addZeroForNum(headInfo.toString(), 38);//补零

        String validate_code1 = ByteUtils.checkXor(headInfo.toString().substring(4, headInfo.toString().length()));//验证码   cs
        data += validate_code1;


        /**
         * 2b2b00 2a2a03FE010801020304050a0f10E000 ff
         */
        /**
         * 起始符 2b2b00
         * 单元数据2a2a03FE010801020304050a0f10E0
         * 补零  000000....
         * 校验 ff
         */
        int index = 0;
        StringBuilder contentInfo = new StringBuilder();
        for (int i = 0; i < SendData.length(); i = i + 32) {
            final int finalI = i;
            if (SendData.length() - i >= 32) {
                StringBuilder info = new StringBuilder();
                info.delete(0, info.length());//删除之前的StringBuilder
                info.append("2b2b");
                info.append(ByteUtils.Decimal0(index));
//                info.append(index + "");
                info.append(SendData.substring(finalI, finalI + 32));
                String validate_code2 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                info.append(validate_code2);
                index++;
                contentInfo.append(info.toString());
                Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalI + 32);
            } else {
                StringBuilder info = new StringBuilder();
                info.delete(0, info.length());//删除之前的StringBuilder
                info.append("2b2b");
                info.append(ByteUtils.Decimal0(index));
//                info.append(index + "");
                info.append(SendData.substring(finalI, SendData.length()));
                String data3 = ByteUtils.addZeroForNum(info.toString(), 38);//补零
                String validate_code3 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                data3 += validate_code3;
                contentInfo.append(data3);
                Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalHexdata.length());
            }
        }

        Log.i(TAG, "onClick: contentInfo最终 = " + contentInfo.toString());
        data += contentInfo.toString();

        Log.i(TAG, "onClick: data最终 = " + data);


        Log.i(TAG, "发送设置数据: SendData = " + SendData);
        Handler handler = DeviceControlActivity.getHandler();
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = DeviceControlActivity.MSG_SENDALLORDER;
            msg.obj = data;
            handler.sendMessage(msg);
        }
        Log.i(TAG, "onClick: OK");
    }


    /**
     * 设置
     */
    public static String set(String contentdata) {
        String SendData = IConstants.SET + //添加起始符设置
                "00" + ByteUtils.integerToHexString(contentdata.length() / 2) + //数据单元长度
//                        ByteUtils.Decimal0(contentdata.length())+ //
                contentdata;//数据单元
        String validate_code = ByteUtils.checkXor(SendData.substring(4, SendData.length()));//验证码   cs
        SendData += validate_code; //添加验证码 2a2a02FE011301033435360203313335030331323304023738D4
        /**
         * 232300 1a 01 0000000000000000000000000000 ff
         2b2b00 2a2a03FE010801020304050a0f10E000 ff
         */
        /**
         * 起始符 232300
         * 长度  1a
         * 包数  01
         * 补零  000000....
         * 校验 ff
         */

        StringBuilder headInfo = new StringBuilder();
        headInfo.delete(0, headInfo.length());//删除之前的StringBuilder
        headInfo.append("232300");
        headInfo.append(ByteUtils.integerToHexString(SendData.length() / 2));//长度hex
        headInfo.append(ByteUtils.integerToHexString((int) Math.ceil(SendData.length() / 32.0))); //包数
        String data = ByteUtils.addZeroForNum(headInfo.toString(), 38);//补零
        String validate_code1 = ByteUtils.checkXor(headInfo.toString().substring(4, headInfo.toString().length()));//验证码   cs
        data += validate_code1;

        /**
         * 2b2b00 2a2a03FE010801020304050a0f10E000 ff
         */
        /**
         * 起始符 2b2b00
         * 单元数据2a2a03FE010801020304050a0f10E0
         * 补零  000000....
         * 校验 ff
         */
        int index = 0;
        StringBuilder contentInfo = new StringBuilder();
        for (int i = 0; i < SendData.length(); i = i + 32) {
            final int finalI = i;
            if (SendData.length() - i >= 32) {
                StringBuilder info = new StringBuilder();
                info.delete(0, info.length());//删除之前的StringBuilder
                info.append("2b2b0");
                info.append(index + "");
                info.append(SendData.substring(finalI, finalI + 32));
                String validate_code2 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                info.append(validate_code2);
                index++;
                contentInfo.append(info.toString());
                Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalI + 32);
            } else {
                StringBuilder info = new StringBuilder();
                info.delete(0, info.length());//删除之前的StringBuilder
                info.append("2b2b0");
                info.append(index + "");
                info.append(SendData.substring(finalI, SendData.length()));
                String data3 = ByteUtils.addZeroForNum(info.toString(), 38);//补零
                String validate_code3 = ByteUtils.checkXor(info.toString().substring(4, info.toString().length()));//验证码   cs
                data3 += validate_code3;
                contentInfo.append(data3);
                Log.i(TAG, "onClick: OK");
//                        shortOrder[0] = finalHexdata.substring(finalI, finalHexdata.length());
            }
        }
        Log.i(TAG, "onClick: contentInfo最终 = " + contentInfo.toString());
        data += contentInfo.toString();
        Log.i(TAG, "onClick: data最终 = " + data);
        Log.i(TAG, "发送设置数据: SendData = " + SendData);

        return data;
    }

    /**
     * 十六进制串转化为byte数组
     */
    public static byte[] hex2byte(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "UTF-8");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }
}
