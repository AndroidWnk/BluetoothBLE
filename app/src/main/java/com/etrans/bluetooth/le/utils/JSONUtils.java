package com.etrans.bluetooth.le.utils;

import com.etrans.bluetooth.le.app.IConstants;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class JSONUtils {


    public static String getValue(String json, String key) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString(key);//根据key返回对应值
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getJSONString(Map<String, Object> map) {
        try {
            JSONObject jsonObject = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                jsonObject.put(key, val);
            }
            return jsonObject.toString();//返回字符串

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getString(Map<String, Object> map) throws UnsupportedEncodingException {
//            JSONObject jsonObject=new JSONObject();
        String str = "";
        int index = 0;//总个数
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            index++;
            String key = entry.getKey();//参数
            //val
            String v = entry.getValue().toString();

            if(key.equals(IConstants.CARNUM)){
                //中文转GBK
                String encodedata = GbkCode.encode(v.substring(0,1));
                //转成hex
//                v.substring(1,v.length()).getBytes();
                String hexv = ByteUtils.toHexString(v.substring(1,v.length()).getBytes());

                String hexv1 = encodedata+hexv;
                //长度
                String len = "00" + ByteUtils.integerToHexString(hexv1.length() / 2);//0011
                Object val = len + //数据单元长度(hex表示)
                        hexv1; //数据单元
                str += key + val;
            }else if(key.equals(IConstants.PORT01)){
                //转成hex
                String hexv = ByteUtils.integerToHexString(Integer.parseInt(v));
                //长度
                String len = "00" + ByteUtils.integerToHexString(hexv.length() / 2);//0011
                //            Object val = entry.getValue();
                Object val = len + //数据单元长度(hex表示)
                        hexv; //数据单元
                str += key + val;
            }

            else{
                //转成hex
                String hexv = ByteUtils.toHexString(v.getBytes());
                //长度
                String len = "00" + ByteUtils.integerToHexString(hexv.length() / 2);//0011

//            Object val = entry.getValue();
                Object val = len + //数据单元长度(hex表示)
                        hexv; //数据单元
                str += key + val;
            }

        }

        String indexNum = ByteUtils.integerToHexString(index);
        return indexNum + str;//返回字符串

    }


}
