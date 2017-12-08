package com.example.t5_skylake.hcl.VoiceManage;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import com.baidu.speech.VoiceRecognitionService;

/**
 * Created by T5-SKYLAKE on 2017/6/9.
 */
public class VoiceControl implements RecognitionListener {

    public interface ResultsCallback {
        void onResults(String result);
    }

    private SpeechRecognizer speechRecognizer;
    private Context context;
    //private Intent intent;
    private ResultsCallback mResultsCallback;

    public VoiceControl(Context context){
        this.context=context;
        //init();
    }

    /*private void init(){
        // 创建识别器
        //intent= MainActivity.intents;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context,new ComponentName(context,VoiceRecognitionService.class));
        // 注册监听器
        speechRecognizer.setRecognitionListener(this);
        startASR(mResultsCallback);
    }*/

    public synchronized void createTool() {
        if (null == speechRecognizer) {

            // 创建识别器
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context,
                    new ComponentName(context, VoiceRecognitionService.class));

            // 注册监听器
            speechRecognizer.setRecognitionListener(this);
        }
    }

    public synchronized void destroyTool() {
        speechRecognizer.stopListening();
        speechRecognizer.destroy();
        speechRecognizer = null;
    }

    // 开始识别
    public void startASR(ResultsCallback callback) {
        mResultsCallback = callback;
        Intent intent=new Intent();
        bindParams(intent);
        speechRecognizer.startListening(intent);
    }

    public void stopASR() {
        speechRecognizer.stopListening();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        // 准备就绪
        Toast.makeText(context, "请开始说话", Toast.LENGTH_SHORT).show();
    }

    private void bindParams(Intent intent) {
        // 设置识别参数
    }

    @Override
    public void onBeginningOfSpeech() {
        // 开始说话处理
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        // 音量变化处理
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        // 录音数据传出处理
    }

    @Override
    public void onEndOfSpeech() {
        // 说话结束处理
    }

    @Override
    public void onError(int error) {
        // 出错处理
    }

    @Override
    public void onResults(Bundle results) {
        // 最终结果处理
        if (mResultsCallback != null) {
            String text = results.get(SpeechRecognizer.RESULTS_RECOGNITION)
                    .toString().replace("]", "").replace("[", "");
            mResultsCallback.onResults(text);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // 处理事件回调
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // 临时结果处理
    }
}
