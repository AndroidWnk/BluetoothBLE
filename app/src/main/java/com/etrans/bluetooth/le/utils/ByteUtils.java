package com.etrans.bluetooth.le.utils;

import java.text.DecimalFormat;
import java.util.List;

public class ByteUtils {
    private final static String TAG = "ByteUtils";
    private volatile static ByteUtils byteutils;

    public static ByteUtils getInstance() {
        if (byteutils == null) {
            synchronized (ByteUtils.class) {
                if (byteutils == null) {
                    byteutils = new ByteUtils();
                }
            }
        }
        return byteutils;
    }


    /**
     * @param srcArrays
     * @return byte[] 返回类型
     * @throws
     * @Title: sysCopy
     * @Description: 合并多个byte[]内容
     */
    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }


    /**
     * @param b
     * @return String 返回类型
     * @throws
     * @Title: bytes2HexString
     * @Description: byte[]转换16进制
     */
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * @param b
     * @return String 返回类型
     * @throws
     * @Title: byte2HexString
     * @Description: byte转换16进制
     */
    private static String byte2HexString(byte b) {
        String ret = "";
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    /**
     * @param t
     * @return byte[] 返回类型
     * @throws
     * @Title: getReverse
     * @Description: byte[]倒序排列
     */
    private static byte[] getReverse(byte[] t) {
        for (int start = 0, end = t.length - 1; start < end; start++, end--) {
            byte temp = t[end];
            t[end] = t[start];
            t[start] = temp;
        }
        return t;
    }

    /**
     * @param i
     * @return byte[] 返回类型
     * @throws
     * @Title: intToByte4
     * @Description: int整数转换为4字节的byte数组
     */
    private static byte[] intToByte4(int i) {
        byte[] targets = new byte[4];
        targets[3] = (byte) (i & 0xFF);
        targets[2] = (byte) (i >> 8 & 0xFF);
        targets[1] = (byte) (i >> 16 & 0xFF);
        targets[0] = (byte) (i >> 24 & 0xFF);
        return targets;
    }


    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }


    /**
     * 字节数组转成16进制表示格式的字符串
     *
     * @param byteArray 需要转换的字节数组
     * @return 16进制表示格式的字符串
     **/
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10)//0~F前面不零
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }




    /**
     * 异或校验
     *
     * @param data 十六进制串
     * @return checkData  十六进制串
     */
    public static String checkXor(String data) {
        int checkData = 0;
        for (int i = 0; i < data.length(); i = i + 2) {
            //将十六进制字符串转成十进制
            int start = Integer.parseInt(data.substring(i, i + 2), 16);
            //进行异或运算
            checkData = start ^ checkData;
        }
        return integerToHexString(checkData);
    }

    /**
     * 将十进制整数转为十六进制数，并补位
     */
    public static String integerToHexString(int s) {
        String ss = Integer.toHexString(s);
        if (ss.length() % 2 != 0) {
            ss = "0" + ss;//0F格式
        }
        return ss.toUpperCase();
    }

    /**
     * 将十六进制整数转为十进制数，并补位
     */
    public static int HexStringTointeger(String hex) {
        Integer x = Integer.parseInt(hex, 16);

        return x;
    }


    /**
     * 个位数时前面补0
     *
     * @param
     * @return
     */
    public static String Decimal0(int num) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(num);
    }


    /**
     * 字符串长度不够补0
     *
     * @param str       字符串
     * @param strLength 长度
     * @return
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
//                sb.append("0").append(str);// left+0
                sb.append(str).append("0");//right+0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }


}
