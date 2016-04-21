package com.example.groovemax.ACR;

/**
 * Created by 60546 on 4/15/2016.
 */
public class JniUtils {
    public native int ACRWavDecode(String wavPath);
    static {
        System.loadLibrary("ACR");
    }
}
