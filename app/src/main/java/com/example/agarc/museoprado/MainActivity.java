package com.example.agarc.museoprado;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MUSEUM";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout ls =(LinearLayout) findViewById(R.id.mainlayout);
        ls.setBackgroundColor(Color.parseColor("#FCE4B2"));
    }

    public void openAgent(View view){
        Intent intent = new Intent(this,VoiceActivity.class);
        startActivity(intent);
    }


}
