package com.example.groovemax.ACR.Utils;

import android.nfc.Tag;
import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 */
public class Chinese2PinYin {
    private HanyuPinyinOutputFormat format = null;
    private String[] pinYin;

    private String triResult;

    /*
     * function:初始化Chinese2PinYin的格式
     */
    public Chinese2PinYin() {
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 设置格式为不带声调
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 全部小写
        pinYin = null;
    }

    /*
     * function:汉字转拼音
     * input：一个汉字
     * output：该汉字对应的拼音，包括多音字
     */
    public String[] getCharPinYin(char c)
    {
        try {
            pinYin = PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        // 如果不是汉字,返回null
        if (pinYin == null)
            return null;

        // 滤去pingyin4j转换结果中重复的拼音
        String[] singlePinYin = getSingleString(pinYin);
        return singlePinYin;
    }


    /*
     * function:用于除去转换结果中重复字符串
     */
    public String[] getSingleString(String[] str)
    {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length; i++)
            if (!list.contains(str[i]))
                list.add(str[i]);
        return (String[]) list.toArray(new String[list.size()]);
    }


    /*
     * function:拆分声母和韵母
     * input：一条拼音指令
     * output：n维数组，偶数为声母，奇数为韵母
     */
    public String[] splitPY(String str){
        String[] arr = str.split(" ");
        String[] newArr = new String[2*arr.length];
        for(int i =0;i<2*arr.length;)
        {
            if(arr[i/2].startsWith("ch"));
            else
            if(arr[i/2].startsWith("sh"));
            else
            if(arr[i/2].startsWith("zh"));
            else
            if(arr[i/2].startsWith("yu"));
            else
            if(arr[i/2].startsWith("yv"));
            else
            {
                newArr[i] = arr[i/2].substring(0, 1);
                newArr[i+1] = arr[i/2].substring(1);
                i= i+2;
                continue;
            }
            newArr[i] = arr[i/2].substring(0, 2);
            newArr[i+1] = arr[i/2].substring(2);
            i= i+2;
        }
        return newArr;
    }

    /*
     * function：change pinyin to triPhone
     * input:拆分好的声母和韵母
     * output：三音素模型
     */
    public String[] py2Tri(String[] input){
        triResult = null;
        triResult = "sil ";
        String[] result = new String[input.length];

        //对y/w/yv做特殊转换
        for(int i = 0; i<input.length; i++){
            if (input[i].equals("y"))
                input[i] = "_i";
            if(input[i].equals("w"))
                input[i] = "_u";
            if(input[i].equals("yu")|input[i].equals("yv"))
                input[i] = "_v";
        }

        for(int i = 0; i<input.length; i++){
            if(i == 0){
                result[i] = "sil-" + input[0] + "+" + input[1];
            }else if(i == input.length-1){
                result[i] = input[i-1] + "-" + input[i] + "+sil";
            }else {
                result[i] = input[i-1] + "-" + input[i] + "+" + input[i+1];
            }
            triResult += result[i];
            triResult += " ";
        }
        triResult += "sil\n";
        return result;
    }

    /*
     * function:return the tri string of command
     * e.g: sil sil-c+e c-e+sh e-sh+sil sil
     */
    public String getCmdTri(){
        return triResult;
    }
}
