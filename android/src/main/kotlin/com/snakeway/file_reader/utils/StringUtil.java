package com.snakeway.file_reader.utils;

/**
 * @author snakeway
 * @description:
 * @date :2022/2/18 15:10
 */
public class StringUtil {

    public static double stringToDouble(String value){
        double res=0;
        try {
            res= Double.parseDouble(value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  res;
    }

    public static float stringToFloat(String value){
        float res=0;
        try {
            res= Float.parseFloat(value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  res;
    }

    public static int stringToInt(String value){
        int res=0;
        try {
            res= (int)Double.parseDouble(value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  res;
    }
}
