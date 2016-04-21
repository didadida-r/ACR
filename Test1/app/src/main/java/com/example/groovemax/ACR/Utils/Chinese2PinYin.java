package com.example.groovemax.ACR.Utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;

/**
 * Created by 60546 on 4/18/2016.
 */
public class Chinese2PinYin {
    private HanyuPinyinOutputFormat format = null;
    private String[] pinYin;

    // 初始化格式
    public Chinese2PinYin() {
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 设置格式为不带声调
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 全部小写
        pinYin = null;
    }

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

    // 用于除去转换结果中重复字符串
    public String[] getSingleString(String[] str)
    {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < str.length; i++)
            if (!list.contains(str[i]))
                list.add(str[i]);
        return (String[]) list.toArray(new String[list.size()]);
    }
}
