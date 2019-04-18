package com.example.bluetooth;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.util.Locale;

public class BTHelper {

    private TextToSpeech tts;
    private MediaRecorder recorder;
    private String audioFile;
    private Context ctx;
    
    public BTHelper(Context ctx){
        this.ctx = ctx;
        audioFile = ctx.getExternalCacheDir().getAbsolutePath() + "/audiocap.3gp";
        initTts();
    }

    private TextToSpeech initTts() {
        tts = new TextToSpeech(ctx,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            tts.setLanguage(Locale.US);
                        } else {
                            tts = null;
                        }
                    }
                });
        return tts;
    }

    public TextToSpeech getTts(){
        if(tts == null){
            return initTts();
        }
        return tts;
    }

    private void record(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try{
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }

    public void stop(){
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public String getAudioFile(){
        return audioFile;
    }

    public final BroadcastReceiver BTListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)){
                if(intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0) == BluetoothHeadset.STATE_AUDIO_CONNECTED)
                    record();
            }
        }
    };

}
