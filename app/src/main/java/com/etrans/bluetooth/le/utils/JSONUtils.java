package com.etrans.bluetooth.le.utils;

import org.json.JSONObject;

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

    public static String getString(Map<String, Object> map) {
//            JSONObject jsonObject=new JSONObject();
        String str = "";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();//参数
//            Object val = entry.getValue();
            Object val = ByteUtils.integerToHexString(entry.getValue().toString().length())+ //数据单元长度(hex表示)
                    ByteUtils.toHexString(entry.getValue().toString().getBytes()); //数据单元
            str += key + val;
        }
        return str;//返回字符串

    }


}
