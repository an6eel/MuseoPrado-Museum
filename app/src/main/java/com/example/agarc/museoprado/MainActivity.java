package com.example.agarc.museoprado;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MUSEUM";
    private MuseumSensor sensor;
    private MultiTouchHandler scroll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout ls =(RelativeLayout) findViewById(R.id.mainlayout);
        ls.setBackgroundColor(Color.parseColor("#FCE4B2"));
        scroll = new MultiTouchHandler(getApplicationContext()) {
            @Override
            public boolean scrollRight() {
                openAgent(null);
                return true;
            }

            @Override
            public boolean scrollLeft() {
                openGallery(null);
                return true;
            }

            @Override
            public boolean scrollUp() {
                return false;
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
                openGallery(null);
                return true;
            }

            @Override
            public boolean mTouchRight() {
                openAgent(null);
                return true;
            }

            @Override
            public boolean mTouchDown() {
                finish();
                return true;
            }

            @Override
            public boolean mTouchCenter() {
                Intent browser= new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.museodelprado.es/"));
                startActivity(browser);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }
        };
        sensor = new MuseumSensor(this) {
            @Override
            public void Snext() {
               openGallery(null);
            }

            @Override
            public void Sprevious() {
                openAgent(null);
            }

            @Override
            public void Sup() {

            }
        };

        sensor.register();
    }

    public void openAgent(View view){
        Intent intent = new Intent(this,VoiceActivity.class);
        sensor.unregister();
        startActivity(intent);
    }

    public void openGallery(View view){
        Intent intent = new Intent(this,Gallery.class);
        sensor.unregister();
        startActivity(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scroll.setEvent(event);
        return super.onTouchEvent(event);
    }


}
