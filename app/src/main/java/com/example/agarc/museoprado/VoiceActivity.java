package com.example.agarc.museoprado;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
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
import android.view.MotionEvent;
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

/**
 * Activity encargada del agente conversacional
 */
public class VoiceActivity extends AppCompatActivity{

    /**
     * Flag que indica si el agente está escuchando
     */
    private boolean FLAG_BUTTON=false;

    /**
     * Codigo de solicitud de permisos de grabación de audio
     */

    private final int AUDIO_RQT_CODE = 22;

    /**
     * Agente conversacional {@link VoiceAgent}
     */

    VoiceAgent agent;

    /**
     * Atributo de tipo {@link MultiTouchHandler} encargado de controlar los gestos en la pantalla
     */

    private MultiTouchHandler scroll;

    /**
     * <pre>
     * Inicializa el Activity, el controlador de gestos en la pantalla, y el agente pasandole
     * MessageView que es donde se mostrará el Chat
     * @param savedInstanceState
     * Gestos:
     *          {@link MultiTouchHandler#scrollUp()}: El agente pasa a escuchar
     *          {@link MultiTouchHandler#mTouchRight()} : Abre la galeria
     *          {@link MultiTouchHandler#mTouchLeft()} : Abre la pantalla principal
     *          {@link MultiTouchHandler#mTouchCenter()} : El agente pasa a escuchar
     *          {@link MultiTouchHandler#mTouchDown()} : Cierra la aplicación
     * </pre>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scroll = new MultiTouchHandler(getApplicationContext(),getWindowManager().getDefaultDisplay()) {
            @Override
            public boolean scrollRight() {
                return false;
            }

            @Override
            public boolean scrollLeft() {
                return false;
            }

            @Override
            public boolean scrollUp() {
                setAgentButton(null);
                return true;
            }

            @Override
            public boolean scrollDown() {
                return false;
            }

            @Override
            public boolean mTouchUp() {
                return false;
            }

            @Override
            public boolean mTouchLeft() {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean mTouchRight() {
                Intent intent = new Intent(getApplicationContext(),Gallery.class);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean mTouchDown() {
                finish();
                return true;
            }

            @Override
            public boolean mTouchCenter() {
                setAgentButton(null);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }
        };

        MessageView ch = (MessageView) findViewById(R.id.chat);

        agent = new VoiceAgent(this,ch);
    }

    /**
     * Hace que el agente pase a escuchar
     * @see {@link VoiceAgent#listen()}
     * @see {@link VoiceAgent#stopListening()}
     * @param view
     */

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
        if(requestCode == AUDIO_RQT_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Log.i("SR", "Record audio permission granted");
            else {
                Log.i("SR", "Record audio permission denied");
                Toast.makeText(getApplicationContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Comprueba que se poseen los permisos de grabación de audio
     */

    public void checkASRPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // If  an explanation is required, show it
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO))
                Toast.makeText(getApplicationContext(), getString(R.string.permission_explanation), Toast.LENGTH_SHORT).show();


            // Request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_RQT_CODE); //Callback in "onRequestPermissionResult"
        }
    }

    /**
     * Pasa el control de los {@link MotionEvent} a nuestro controladores de gestos
     * @param event
     * @return
     */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scroll.setEvent(event);
        return super.onTouchEvent(event);
    }
}
