/**
 * <b>Prado Museum APP</b>
 * Aplicación que nos permite tener acceso a algunas de las principales obras
 * del museo del prado e interactuar con un agente conversacional que nos dará
 * información tanto del museo como de sus obras y artistas
 *
 * @author Angel Garcia Malagon, Jaime Amate Ramirez y Aurelia Maria Nogueras Lara
 * @version 1.0
 *
 */

package com.example.agarc.museoprado;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 *  Activity encargada de la pantalla principal
 */
public class MainActivity extends AppCompatActivity {

    private static final String LOGTAG = "MUSEUM";

    /**
     *  Sensor {@link MuseumSensor} para controlar los sensores en la pantalla principal
     */

    private MuseumSensor sensor;

    /**
     * Atributo de tipo {@link MultiTouchHandler} encargado de controlar los gestos en la pantalla
     */

    private MultiTouchHandler scroll;

    /**
     * Latitud de la ETSIIT
     */

    private static final double ETSIIT_LATITUDE = 37.1970962;

    /**
     * Longitud de la ETSIIT
     */

    private static final double ETSIIT_LONGITUDE = -3.6241889;

    /**
     * Localizacion de la ETSIIT
     */

    private Location etsiit;

    /**
     * Localizacion actual
     */

    private Location current;

    /**
     * Atributo de tipo {@link GPSTracker} encargado de obtener la posición actual
     */

    GPSTracker gpsTracker;

    /**
     * Rango alrededor de la ETSIIT en el que podemos usar la aplicación
     */

    private static final double POSITION_THRESHOLD = 15000; // METERS RANGE

    /**
     * <pre>
     * Metodo en el que se inicia la Activity, además de registrar la posición inicial y de la
     * ETSIIT, inicializar el localizador GPS, el controlador de sensores y el de gestos en la
     * pantalla
     * Gestos:
     *          {@link MultiTouchHandler#scrollRight()}: Abre el agente conversacional
     *          {@link MultiTouchHandler#scrollLeft()} : Abre la galeria
     *          {@link MultiTouchHandler#mTouchRight()} : Abre el agente conversacional
     *          {@link MultiTouchHandler#mTouchLeft()} : Abre la galeria
     *          {@link MultiTouchHandler#mTouchCenter()} : Abre en el navegador la web del Museo del Prado
     *          {@link MultiTouchHandler#mTouchDown()} : Cierra la aplicación
     * Sensores:
     *          {@link MuseumSensor#Snext()} : Abre el agente conversacional
     *          {@link MuseumSensor#Sprevious()} : Abre la galeria
     * </pre>
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout ls = (LinearLayout) findViewById(R.id.mainlayout);
        ls.setBackgroundColor(Color.parseColor("#FCE4B2"));
        etsiit = new Location("etsiit");
        current = new Location("current");
        etsiit.setLatitude(ETSIIT_LATITUDE);
        etsiit.setLongitude(ETSIIT_LONGITUDE);

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                current=location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    10, mLocationListener);
        }

        scroll = new MultiTouchHandler(getApplicationContext(), getWindowManager().getDefaultDisplay()) {
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
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.museodelprado.es/"));
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
                openAgent(null);
            }

            @Override
            public void Sprevious() {
                openGallery(null);
            }

            @Override
            public void Sup() {

            }

            @Override
            public void sound(TextToSpeech tts) {

            }
        };

        TextView z = (TextView) findViewById(R.id.textView);


        sensor.register();
    }

    /**
     * Inicia la Activity del Agente conversacional {@link VoiceActivity}
     * @param view
     */

    public void openAgent(View view){
        Intent intent = new Intent(this,VoiceActivity.class);
        sensor.unregister();
        startActivity(intent);
    }

    /**
     * Inicia la Activity de la Galeria {@link Gallery}
     * @param view
     */
    public void openGallery(View view){
        Intent intent = new Intent(this,Gallery.class);
        sensor.unregister();
        startActivity(intent);
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
     * Comprueba si se poseen permisos de Ubicacion
     * @return Devuelve si se poseen permisos o no
     */

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Muestra una alerta informando de que no se puede usar la aplicación sin permisos de Ubicación
     */

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this,R.style.MyDialogTheme);

        // set title
        alertDialogBuilder.setTitle("GPS");
        final Activity cx =this;
        // set dialog message
        alertDialogBuilder
                .setMessage("No puedes usar esta aplicacion sin permisos.")
                .setCancelable(false)
                .setPositiveButton("Aceptar permisos",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        ActivityCompat.requestPermissions(cx, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    }
                })
                .setNegativeButton("Salir",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Muestra una alerta informando que no se puede usar la aplicación lejos de la ETSIIT
     */

    public void showPositionAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this,R.style.MyDialogTheme);

        // set title
        alertDialogBuilder.setTitle("Museo");

        alertDialogBuilder
                .setMessage("Esta aplicación solo puede usarse cerca de la facultad.")
                .setCancelable(false)
                .setPositiveButton("Prueba otra vez",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        onStart();
                    }
                })
                .setNegativeButton("Salir",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    /**
     * Obtiene la localización actual al iniciar el Activity
     */

    @Override
    protected void onStart(){
        super.onStart();

        if (checkLocationPermission()) {
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.canGetLocation) {
                current.setLongitude(gpsTracker.getLongitude());
                current.setLatitude(gpsTracker.getLatitude());
                if(current.distanceTo(etsiit)>POSITION_THRESHOLD)
                    showPositionAlert();
            } else {
                Toast.makeText(this, "please accept permission !!!!", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    /**
     * Gestiona los resultados de haber pedido permisos de algun tipo
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case 1:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission Granted

                    gpsTracker = new GPSTracker(this);
                    if (gpsTracker.canGetLocation) {
                        current.setLongitude(gpsTracker.getLongitude());
                        current.setLatitude(gpsTracker.getLatitude());
                    } else {
                        Toast.makeText(this, "please accept permission !!!!", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "please accept permission !!!!", Toast.LENGTH_SHORT).show();
                    showSettingsAlert();
                }
                break;
        }
    }
}
