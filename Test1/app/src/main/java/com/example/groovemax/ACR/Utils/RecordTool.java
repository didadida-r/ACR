package com.example.groovemax.ACR.Utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 60546 on 4/17/2016.
 */
public class RecordTool {
    private static final String TAG = "debug";

    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 16000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private boolean isRecord = false;// 设置正在录制的状态
    private AudioManager mAudioManager = null;
    //AudioName裸音频数据文件
    private static final String AudioBufferName = "/sdcard/buffer.3gpp";
    //NewAudioName可播放的音频文件
    private static final String AudioPath = Environment.getExternalStorageDirectory()+"/ACR/";
    private static final String CurrentAdioPath=AudioPath+"CurrentRec/";
    private String AudioName=CurrentAdioPath+"CurrentAudio.wav";
    private AudioRecord audioRecord=null;

    //static public String World2Rec=null;
    //static public String World2RecID=null;

    public RecordTool(AudioManager audioManager){
        this.mAudioManager = audioManager;
        Init();
        Log.v(TAG, "Init");

    }

    /*
     * to init the recordTool
     */
    private void Init() {
        // 获得缓冲区字节大小
        bufferSizeInBytes =AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        // 创建AudioRecord对象
        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);

        File destDir = new File(AudioPath+'/');
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File curFile=new File(CurrentAdioPath);
        if(!curFile.exists()) {
            curFile.mkdirs();
        }
    }

    /*
    * to release memory when destroy
    */
    public void destroy(){
        try{
            stopRecord(false);
        }catch (Exception e){
            e.printStackTrace();
        }
        audioRecord.release();
        audioRecord = null;
        File file = new File(CurrentAdioPath);
        if(file.exists())
            file.delete();
    }

    public void startRecord() {
        Time t=new Time();
        t.setToNow();
        audioRecord.startRecording();
        // 让录制状态为true
        isRecord = true;
        // 开启音频文件写入线程
        new Thread(new AudioRecordThread()).start();
    }

    public void stopRecord(boolean isSave) throws Exception {
        if (audioRecord != null) {
            isRecord = false;//停止文件写入
            audioRecord.stop();
            if(isSave){
                //先复制到缓冲区
                Time t=new Time();
                t.setToNow();
                AudioName=CurrentAdioPath+t.month+t.monthDay+t.hour+t.minute+t.second+".wav";
                copyWaveFile(AudioBufferName, AudioName);//给裸数据加上头文件
            }
            else {
                File file=new File(AudioBufferName);
                if(file.exists())
                    file.delete();
            }
        }
    }

    public boolean getRecordState() {
        return isRecord;
    }

    public String getCurrentAudio() {
        return AudioName;
    }

    private boolean deleteAudio(String Audio) {
        File file=new File(Audio);
        if (file.exists()) {
            return file.delete();
        }
        else
            return true;

    }

    public boolean deleteCurrentAudio() {
        return deleteAudio(getCurrentAudio());
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            try {
                writeDateTOFile();//往文件中写入裸数据
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * write the naked data to file without wav from(.3gpp)
     */
    private void writeDateTOFile() throws IOException{
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audioDate = new byte[bufferSizeInBytes];
        FileOutputStream fos;
        int readSize;
        File file = new File(AudioBufferName);
        if (file.exists()) {
            file.delete();
        }
        fos = new FileOutputStream(file);// 建立一个可存取字节的文件

        while (isRecord == true) {
            readSize = audioRecord.read(audioDate, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                fos.write(audioDate,0,readSize);
            }
        }
        fos.flush();
        fos.close();// 关闭写入流
    }

    /*
     *   get the wav file from the inFile Stream(.wav)
     */
    private void copyWaveFile(String inFilename, String outFilename) throws IOException{
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 1;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        in = new FileInputStream(inFilename);
        out = new FileOutputStream(outFilename);
        totalAudioLen = in.getChannel().size();
        totalDataLen = totalAudioLen + 36;
        WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate);
        int dataLen;
        while ((dataLen=in.read(data)) != -1) {
            out.write(data,0,dataLen);
        }
        out.flush();
        in.close();
        out.close();
        File file = new File(AudioBufferName);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * write the head of the wav file
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

}
