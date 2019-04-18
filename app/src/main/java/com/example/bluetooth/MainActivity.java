package com.example.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media.session.MediaButtonReceiver;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String MY_UTTERANCE_ID = "MY_UTTERANCE_ID";
    private static final int DISCOVERABLE_TIMEOUT_MS = 300;
    private BluetoothHeadset bluetoothHeadset;

    // Get the default adapter
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null;
            }
        }
    };

    private final int REQUEST_ENABLE_BT = 87;
    private Button startListen;
    private Button stopListen;
    private BTHelper bthelper;
    private TextView tv;
    private MediaSessionCompat msc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startListen = findViewById(R.id.listen);
        stopListen = findViewById(R.id.stop);
        tv = findViewById(R.id.textView);

        startListen.setEnabled(false);
        stopListen.setEnabled(false);

        bthelper = new BTHelper(this);

        msc = new MediaSessionCompat(this, "MediaSessionCompat");
        msc.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                if (mediaButtonEvent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                    KeyEvent event = (KeyEvent) mediaButtonEvent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            Toast.makeText(getApplicationContext(), "Uploading data", Toast.LENGTH_SHORT).show();
                            return true;

                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            Toast.makeText(getApplicationContext(), "Hacking Google", Toast.LENGTH_SHORT).show();
                            return true;

                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            Toast.makeText(getApplicationContext(), "Stealing secrets", Toast.LENGTH_SHORT).show();
                            return true;

                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            Toast.makeText(getApplicationContext(), "Doing nothing", Toast.LENGTH_SHORT).show();
                            return true;
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent);
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            setup();
        }

        registerReceiver(bthelper.BTListener, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
        registerReceiver(bthelper.BTListener, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
    }

    public void playAudio(View view) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
    }

    public void speak(View view) {
        TextToSpeech tts = bthelper.getTts();
        tts.speak("Hello World", TextToSpeech.QUEUE_ADD, null, MY_UTTERANCE_ID);
    }

    public void listen(View view) {
        List<BluetoothDevice> list = bluetoothHeadset.getConnectedDevices();
        if (list.size() > 0) {
            boolean result = bluetoothHeadset.startVoiceRecognition(list.get(0));
            Toast.makeText(this, "The voice recognition " + (result ? "has " : "has not ") + "started", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop(View view) {
        List<BluetoothDevice> list = bluetoothHeadset.getConnectedDevices();
        if (list.size() > 0) {
            bluetoothHeadset.stopVoiceRecognition(list.get(0));
            bthelper.stop();
        }
    }

    public void pair(View view) {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                DISCOVERABLE_TIMEOUT_MS);
        startActivity(discoverableIntent);

    }

    public void rename(View view) {
        bluetoothAdapter.setName(tv.getText().toString());
    }

    public void repeat(View view) {
        try {
            Uri myUri = Uri.parse(bthelper.getAudioFile());
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setup() {
        startListen.setEnabled(true);
        stopListen.setEnabled(true);

        bluetoothAdapter.getProfileProxy(this, profileListener, BluetoothProfile.HEADSET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                setup();
            }
        }
    }

    public class MediaButServ extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            MediaButtonReceiver.handleIntent(msc, intent);
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }
}