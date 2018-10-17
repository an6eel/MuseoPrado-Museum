package com.example.agarc.museoprado;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bassaer.chatmessageview.model.ChatUser;
import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.view.MessageView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageButton;

public class VoiceActivity extends AppCompatActivity{

    private boolean FLAG_BUTTON=false;
    private final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 22;

    VoiceAgent agent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MessageView ch = (MessageView) findViewById(R.id.chat);
        agent = new VoiceAgent(this,ch);
    }

    public void setAgentButton(View view){

        GifImageButton but = (GifImageButton) findViewById(R.id.listening);
        if(!FLAG_BUTTON) {
            checkASRPermission();
            but.setBackgroundResource(R.drawable.mic2);
            agent.listen();
        }
        else{
            but.setBackgroundResource(R.drawable.mic);
            agent.stopListening();
        }

        FLAG_BUTTON=!FLAG_BUTTON;
    }

    /**
     * Processes the result of the record audio permission request. If it is not granted, the
     * abstract method "onRecordAudioPermissionDenied" method is invoked. Such method must be implemented
     * by the subclasses of VoiceActivity.
     * More info: http://developer.android.com/intl/es/training/permissions/requesting.html
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.i("SR", "Record audio permission granted");
            else {
                Log.i("SR", "Record audio permission denied");
                Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkASRPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), getString(R.string.permission_explanation), Toast.LENGTH_SHORT).show();


            // Request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO); //Callback in "onRequestPermissionResult"
        }
    }
}
