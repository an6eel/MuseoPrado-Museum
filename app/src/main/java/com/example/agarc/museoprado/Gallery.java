package com.example.agarc.museoprado;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class Gallery extends AppCompatActivity {

    private FirebaseFirestore db;
    private MuseumSensor sensor;
    private MultiTouchHandler scroll;
    private TextView info;
    private List<DocumentSnapshot> artists;
    private List<DocumentSnapshot> paintings;
    private static DocumentSnapshot ARTIST;
    private int INDEX_ART;
    private int INDEX_PAINT;
    private boolean view;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        info = (TextView) findViewById(R.id.view);
        info.setVisibility(View.INVISIBLE);
        info.setMovementMethod(new ScrollingMovementMethod());

        sensor = new MuseumSensor(this) {
            @Override
            public void Snext() {
                Toast.makeText(getApplicationContext(), "Shake right", Toast.LENGTH_LONG).show();
                nextImg();
            }

            @Override
            public void Sprevious() {
                Toast.makeText(getApplicationContext(), "Shake left", Toast.LENGTH_LONG).show();
                previousImg();
            }

            @Override
            public void Sup() {
                if( INDEX_PAINT == -1 ) {
                    Toast.makeText(getApplicationContext(), "Shake up", Toast.LENGTH_LONG).show();
                    nextArt();
                }
            }
        };
        scroll = new MultiTouchHandler(getApplicationContext()) {
            @Override
            public boolean scrollRight() {
                if(!view)
                    nextImg();
                return true;
            }

            @Override
            public boolean scrollLeft() {
                if(!view)
                    previousImg();
                return true;
            }

            @Override
            public boolean scrollUp() {
                if(INDEX_PAINT == -1 && !view)
                    nextArt();
                return true;
            }

            @Override
            public boolean scrollDown() {
                if(view){
                    view = false;
                    info = (TextView) findViewById(R.id.view);
                    info.setVisibility(View.INVISIBLE);
                }
                else if(INDEX_PAINT == -1)
                    previousArt();
                return true;
            }

            @Override
            public boolean mTouchUp() {
                final String prad = (String) ARTIST.get("url");
                Intent browser= new Intent(Intent.ACTION_VIEW, Uri.parse(prad));
                startActivity(browser);
                return true;
            }

            @Override
            public boolean mTouchLeft() {
                Intent intent = new Intent(getApplicationContext(),VoiceActivity.class);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean mTouchRight() {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
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
                if(!view){
                    info = (TextView) findViewById(R.id.view);
                    info.setVisibility(View.VISIBLE);
                    view = true;
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
            }
        };

        INDEX_ART =0;
        INDEX_PAINT = -1;
        initDB();

    }

    public void initDB() {

        db = FirebaseFirestore.getInstance();

        CollectionReference x = db.collection("Artistas");
        x.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    artists = task.getResult().getDocuments();
                    ARTIST = artists.get(INDEX_ART);
                    Query y = db.collection("obras").whereEqualTo("autor", ARTIST.getId() );
                    y.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                paintings = task.getResult().getDocuments();
                                update();
                            }

                        }
                    });

                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor.register();
    }

    @Override
    protected  void onPause(){
        super.onPause();
        sensor.unregister();
    }

    public void update(){
        TextView author = (TextView) findViewById(R.id.author);
        TextView painting = (TextView) findViewById(R.id.painting);
        TextView prado = (TextView) findViewById(R.id.url) ;
        ImageView paint = (ImageView) findViewById(R.id.paint);

        String name = (String) ARTIST.getId();
        String url;
        String subtext;
        final String prad = (String) ARTIST.get("url");

        info = (TextView) findViewById(R.id.view);
        info.setText(Html.fromHtml(getInfoArt()));

        if(INDEX_PAINT == -1){
            url = (String) ARTIST.get("urlImg");
            subtext = (String) ARTIST.get("name");
            info.setText(Html.fromHtml(getInfoArt()));
        }
        else {
            url = (String) paintings.get(INDEX_PAINT).getData().get("url");
            subtext = (String) paintings.get(INDEX_PAINT).getId();
            info.setText(Html.fromHtml(getInfoPaint()));
        }

        prado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browser= new Intent(Intent.ACTION_VIEW, Uri.parse(prad));
                startActivity(browser);
            }
        });
        DownloadImageTask dwn = new DownloadImageTask(this,paint);
        dwn.execute(url);
        author.setText(name);
        painting.setText(subtext);

    }

    public void nextImg(){
        INDEX_PAINT+=1;
        if(INDEX_PAINT == paintings.size())
            INDEX_PAINT = -1;

        update();
    }

    public void previousImg(){
        if(INDEX_PAINT == -1)
            INDEX_PAINT = paintings.size()-1;
        else
            INDEX_PAINT--;

        update();
    }

    public void nextArt(){
        INDEX_ART= (INDEX_ART+1 ) % artists.size();
        ARTIST = artists.get(INDEX_ART);
        INDEX_PAINT = -1;

        Query y = db.collection("obras").whereEqualTo("autor", ARTIST.getId() );
        y.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    paintings = task.getResult().getDocuments();
                    update();
                }

            }
        });
    }

    public void previousArt(){
        if(INDEX_ART == 0)
            INDEX_ART = artists.size()-1;
        else
            INDEX_ART--;
        ARTIST = artists.get(INDEX_ART);

        Query y = db.collection("obras").whereEqualTo("autor", ARTIST.getId() );
        y.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    paintings = task.getResult().getDocuments();
                    update();
                }

            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scroll.setEvent(event);
        return super.onTouchEvent(event);
    }

    public String getInfoArt(){
        String info = "";
        info+=("<b>Nombre:</b> "+ ARTIST.get("name")+"<br/>");
        Date birth = ARTIST.getDate("birthdate");
        Date death = ARTIST.getDate("deathdate");
        SimpleDateFormat ft =
                new SimpleDateFormat ("dd/MMMM/yyyy");
        info+=("<b>Fecha de Nacimiento:</b> "+ ft.format(birth)+ "<br/>");
        info+=("<b>Lugar de Nacimiento: </b>"+ ARTIST.get("birthplace")+"<br/>");
        info+=("<b>Fecha de Fallecimiento:</b> "+ ft.format(death)+ "<br />");
        if(ARTIST.contains("etapas")) {
            Map<String, String> etapas = (Map) ARTIST.get("etapas");
            info += ("<b>Etapas:</b><br/>");
            for (String key : etapas.keySet())
                info += ("\t" + "<u>" + key + "</u>: " + etapas.get(key) + "<br/>");
        }
        ArrayList<String> influencias = (ArrayList<String>) ARTIST.get("influencias");
        info+=("<b>Influencias: </b>"+ influencias.get(0));
        for(int i=1;i<influencias.size();++i)
            info+=(","+influencias.get(i));
        info+=("<br />");

        ArrayList<String> life = (ArrayList<String>) ARTIST.get("lifeplace");
        info+=("<b> Donde vivio: </b>"+ life.get(0));
        for(int i=1;i<life.size();++i)
            info+=(","+ life.get(i));
        return info;
    }

    public String getInfoPaint(){
        String info = "";
        DocumentSnapshot paint = paintings.get(INDEX_PAINT);

        ArrayList<String> name = (ArrayList<String>) paint.get("name");
        info+=("<b>Nombre:</b> "+ name.get(0));
        if(name.size()>1){

            for(int i=1;i<name.size()-1;++i)
                info+=(", " + name.get(i));
            info+=(" o " + name.get(name.size()-1));
        }
        info+=("<br/>");
        info+=("<b> Autor: </b> "+ paint.get("autor")+ ", " + paint.get("date")+ "<br/>");
        if(paint.contains("collection"))
            info+=("<b> Coleccion: </b> "+ paint.get("collection") + "<br/>");
        if(paint.contains("sala"))
            info+=("<b> Sala: </b> "+ paint.get("sala") + "<br/>");
        info+=("<b> Resumen: </b> "+ paint.get("resumen") + "<br/>");
        return info;
    }


}


