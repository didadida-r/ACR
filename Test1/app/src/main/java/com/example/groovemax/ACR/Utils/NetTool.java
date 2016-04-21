package com.example.groovemax.ACR.Utils;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by 60546 on 4/18/2016.
 */
public class NetTool {

    private static final String TAG = "debug";
    private String modelName = null;
    private String commandPath = Environment.getExternalStorageDirectory()+"/ACR/command.txt";

    public NetTool(){
        readFile();
    }

    private void readFile(){
        try{
            Scanner in = new Scanner(new File(commandPath));
            while(in.hasNext()){
                modelName = in.next();

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


}
