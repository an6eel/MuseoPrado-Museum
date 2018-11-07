package com.example.agarc.museoprado;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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

/**
 * Activity encargada de la Galeria
 */

public class Gallery extends AppCompatActivity {


    /**
     * Conexion con la base de Datos Firebase
     */

    private FirebaseFirestore db;

    /**
     *  Sensor {@link MuseumSensor} para controlar los sensores en la pantalla principal
     */

    private MuseumSensor sensor;

    /**
     * Atributo de tipo {@link MultiTouchHandler} encargado de controlar los gestos en la pantalla
     */

    private MultiTouchHandler scroll;

    /**
     * View que muestra la informacion de la obra/autor
     */

    private TextView info;

    /**
     * Lista de los artistas almacenados en la base de datos
     * @see {@link DocumentSnapshot}
     */

    private List<DocumentSnapshot> artists;

    /**
     * Lista de obras pertenecientes a {@link #ARTIST} almacenadas en la base de Datos
     * @see {@link DocumentSnapshot}
     */
    private List<DocumentSnapshot> paintings;

    /**
     * Artista de la base de datos que se esta consultando en cada momento
     * @see {@link DocumentSnapshot}
     */

    private static DocumentSnapshot ARTIST;

    /**
     * Indice del artista actual
     */

    private int INDEX_ART;

    /**
     * Indice de la obra actual
     */

    private int INDEX_PAINT;

    /**
     * Flag que indica si {@link #info} se está mostrando en pantalla
     */

    private boolean view;

    /**
     * <pre>
     * Inicializa el Activity, el controlador de sensores y gestos, y la base de Datos Firebase
     *
     * Gestos:
     *         {@link MultiTouchHandler#scrollRight()} : Siguiente Obra
     *         {@link MultiTouchHandler#scrollLeft()} : Obra Anterior
     *         {@link MultiTouchHandler#scrollUp()} : Siguiente artista
     *         {@link MultiTouchHandler#scrollDown()} : Cierra la ventana de informacion si se esta mostrando, artista anterior en otro caso
     *         {@link MultiTouchHandler#mTouchRight()} : Vuelve a la pantalla principal
     *         {@link MultiTouchHandler#mTouchLeft()} ()} : Abre el agente conversacional
     *         {@link MultiTouchHandler#mTouchCenter()} : Muestra información en la pantalla de la obra/artista
     *         {@link MultiTouchHandler#mTouchDown()} : Cierra la aplicación
     *         {@link MultiTouchHandler#mTouchUp()}} : Abre la pagina web del artista en el Museo del Prado
     * Sensores:
     *         {@link MuseumSensor#Snext()} : Siguiente Obra del artista seleccionado
     *         {@link MuseumSensor#Sprevious()} : Obra anterior del artista seleccionado
     *         {@link MuseumSensor#Sup()} : Cambia de autor
     *         {@link MuseumSensor#sound(TextToSpeech)} : Da información de la obra/artista por el microfono interno
     * </pre>
     *
     * @param savedInstanceState
     */

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

            @Override
            public void sound(TextToSpeech tts) {
                Toast.makeText(getApplicationContext(), "Información", Toast.LENGTH_LONG).show();
                turnOffSpeaker();

                if (INDEX_PAINT == -1) {
                    String cadena = (ARTIST.get("obras")).toString();
                    String info = " Las obras de " + ARTIST.get("name") + " son " + cadena;
                    tts.speak(info, TextToSpeech.QUEUE_FLUSH, null, null);
                }else {
                    DocumentSnapshot paint = paintings.get(INDEX_PAINT);
                    String resumen = paint.get("resumen").toString();
                    ArrayList<String> name = (ArrayList<String>) paint.get("name");
                    String info = " Voy a contarte un poco sobre " + name.get(0) + ". " + resumen;
                    tts.speak(info, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        };
        scroll = new MultiTouchHandler(getApplicationContext(),getWindowManager().getDefaultDisplay()) {
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

    /**
     * Inicializa la base de Datos y realiza las primeras consultas
     */

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

    /**
     * Actualiza la informacion de obra/autor mostrada en pantalla en caso de que se haya cambiado
     * de autor/obra
     */

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

    /**
     * Obtiene la siguiente obra de {@link #ARTIST}
     */

    public void nextImg(){
        INDEX_PAINT+=1;
        if(INDEX_PAINT == paintings.size())
            INDEX_PAINT = -1;

        update();
    }

    /**
     * Obtiene la obra anterior de {@link #ARTIST}
     */

    public void previousImg(){
        if(INDEX_PAINT == -1)
            INDEX_PAINT = paintings.size()-1;
        else
            INDEX_PAINT--;

        update();
    }

    /**
     * Obtiene el siguiente artista de la lista {@link #artists}
     */

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

    /**
     * Obtiene el artista anterior de la lista {@link #artists}
     */

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

    /**
     * Obtiene toda la informacion almacenada en la base de datos sobre {@link #ARTIST}
     * @return info
     */

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

    /**
     * Obtiene toda la informacion sobre la obra consultada actualemente contenida en la
     * base de Datos
     * @return Info
     */

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


