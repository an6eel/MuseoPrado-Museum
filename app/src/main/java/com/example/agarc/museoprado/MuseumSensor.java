package com.example.agarc.museoprado;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Clase gue gestiona los eventos de los sensores del dispositivo
 */
public abstract class  MuseumSensor implements SensorEventListener {

    /**
     * Contexto del Activity donde se gestionan los eventos
     */
    private Context cxt;

    /**
     * Limite de movimiento para considerar que se ha realizado un shake
     */

    private static final float SHAKE_THRESHOLD = 5.0f;

    /**
     * Limite de tiempo que debe pasar entre un shake y otro
     */

    private static final int SHAKE_WAIT_TIME_MS = 250;

    /**
     * Limite que debe haber para considerar que ha habido rotacion
     */

    private static final float ROTATION_THRESHOLD = 2.0f;

    /**
     * Limite de tiempo que debe haber entre una rotacion y otra
     */
    private static final int ROTATION_WAIT_TIME_MS = 250;

    /**
     * Sensor handler {@link SensorManager}
     */

    private SensorManager mSensorManager;

    /**
     * Sensor de tipo LINEAR_ACCELERATION
     * {@link Sensor}
     */

    private Sensor mSensorAcc;

    /**
     * Sensor de tipo GYROSCOPE
     * {@link Sensor}
     */

    private Sensor mSensorGyr;

    /**
     * Sensor de tipo ROTATION_VECTOR
     * {@link Sensor}
     */

    private Sensor mSensorRo;

    /**
     * Sensor de tipo PROXIMITY
     * {@link Sensor}
     */

    private Sensor mProximity;

    /**
     * Atributo que almacenará los valores de la rotación
     */

    private float orientation[] = new float[3];
    float[] rMat = new float[9];
    int mAzimuth;

    /**
     * Atributo para controlar cuanto tiempo ha pasado entre un shake y otro
     */

    private long mShakeTime = 0;

    /**
     * Atributo para controlar cuanto tiempo ha pasado entre una rotación y otra
     */

    private long mRotationTime = 0;

    /**
     * Enum para obtener en que sentido se produce la aceleracion
     * Valores: x_minus, x_plus, y_minus, y_plus, z_minus, z_plus, noAcceleration
     */

    private enum LinearAcceleration{x_minus, x_plus, y_minus, y_plus, z_minus, z_plus, noAcceleration};

    /**
     * Atributo que almacena la ultima aceleración producida
     */

    LinearAcceleration linearAcceleration = LinearAcceleration.noAcceleration;

    /**
     * Enum para obtener en que sentido se produce la rotación
     * Valores: x, y, z, noRotation
     */

    private enum Rotation {x, y, z, noRotation};

    /**
     * Atributo que almacena la ultima rotación producida
     */

    Rotation rotation = Rotation.noRotation;

    /**
     * Atributo para controlar los parametros de la pantalla
     */

    WindowManager.LayoutParams params;

    /**
     * Flag para controlar si se ha acercado el telefono para escuchar
     */
    private boolean FLAG_SOUND = false;

    private float[] mGravity = new float[3];

    /**
     * Servicio que se encarga de pasar a voz un texto escrito
     * @see {@link TextToSpeech}
     */

    private TextToSpeech tts;


    /**
     * Constructor de la clase para gestionar los sensores
     * @param cx
     */

    public MuseumSensor(Context cx){
        cxt=cx;
        params =((Activity)cxt).getWindow().getAttributes();
        init();
    }

    /**
     * Registra los sensores establecidos, para que gestionen los eventos de su tipo
     */

    public void register(){
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorRo, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyr, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Hace que los sensores dejen de gestionar los eventos
     * Activa de nuevo el speaker en caso de que haya sido desactivado
     */

    public void unregister(){
        mSensorManager.unregisterListener(this);
        if (tts != null){
            tts.stop();
            turnOnSpeaker();
        }
    }

    /**
     * Inicializa los sensores y el servicio tts
     */

    private void init(){
        mSensorManager = (SensorManager) cxt.getSystemService(Context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyr = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorRo = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mGravity[0] = 0.0f;
        mGravity[1] = 0.0f;
        mGravity[2] = 0.0f;
        tts = new TextToSpeech(cxt, null);
    }

    /**
     * <pre>
     * Metodo que se lanza cuando se ha producido algun evento en los sensores
     * Tipo Linear Acceleration:
     *                          Detecta la aceleración
     * Tipo Rotation Vector:
     *                          Detecta la rotación producida, para comprobar el sentido
     *                          Detecta si nos hemos mantenido el telefono recto, para verificar posteriormente que nos hemos acercado el dispositivo para escuchar
     * Tipo Gyroscope:
     *                          Detecta la rotación
     * Tipo Proximity:
     *                          Si el valor de proximidad es inferior al rango máximo
     *                                 - Reducimos el brillo de la pantalla
     *                                 - Si se ha girado el dispositivo sobre el eje Y significa que hemos desplazado el dispositivo para escuchar
     *                                  , por lo que en ese caso se llama al metodo {@link #sound(TextToSpeech)}
     *
     * Movimientos:
     *              Left Shake : {@link #Sprevious()}
     *              Right Shake : {@link #Snext()}
     *              Up Shake : {@link #Sup()}
     *              Llevar dispositivo hacia la oreja: {@link #sound(TextToSpeech)}
     * </pre>
     * @param event
     */

    @Override
    public void onSensorChanged(SensorEvent event) {

        boolean terminado = true;

        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            linearAcceleration = detectAcceleration(event.values.clone());
        }
        else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            mAzimuth = computeAzimuth(event.values.clone());

            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);

            float[] orientations = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientations);

            for (int i = 0; i < 3; i++) {
                orientations[i] = (float) (Math.toDegrees(orientations[i]));
            }
            if (Math.abs(orientations[1]) < 10) {
                FLAG_SOUND = true;
            } else {
                FLAG_SOUND = false;
            }

        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotation = detectRotation(event);
        }
        else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            params =((Activity)cxt).getWindow().getAttributes();
            if (event.values[0] < mProximity.getMaximumRange()) {
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = 0;
                ((Activity) cxt).getWindow().setAttributes(params);

                if (terminado && FLAG_SOUND) {
                    terminado = false;
                    sound(tts);
                    if (!tts.isSpeaking())
                        turnOnSpeaker();
                }

            } else {
                //far
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = -1f;
                ((Activity) cxt).getWindow().setAttributes(params);

                if (tts != null) {
                    tts.stop();
                    turnOnSpeaker();
                }

            }
            terminado = true;
        }

        if((linearAcceleration == linearAcceleration.y_minus || linearAcceleration == LinearAcceleration.y_plus) && rotation ==Rotation.x){
            Sup();
            rotation = Rotation.noRotation;
            linearAcceleration = LinearAcceleration.noAcceleration;
        }
        else if(linearAcceleration == LinearAcceleration.x_minus && (mAzimuth > 270  && mAzimuth < 360-10)){
            Sprevious();
            rotation = Rotation.noRotation;
            linearAcceleration = LinearAcceleration.noAcceleration;
        }
        else if(linearAcceleration == linearAcceleration.x_plus && (mAzimuth > 0+10 && mAzimuth < 90)){
            Snext();
            rotation = Rotation.noRotation;
            linearAcceleration = LinearAcceleration.noAcceleration;
        }
    }

    /**
     * Detecta en que eje se ha realizado la rotación
     * @param event
     * @return
     */

    private Rotation detectRotation(SensorEvent event) {
        long now = System.currentTimeMillis();

        if ((now - mRotationTime) > ROTATION_WAIT_TIME_MS) {
            mRotationTime = now;

            if(Math.abs(event.values[0]) > ROTATION_THRESHOLD){
                return Rotation.x;
            }
            else if(Math.abs(event.values[1]) > ROTATION_THRESHOLD){
                return Rotation.y;
            }
            else if(Math.abs(event.values[2]) > ROTATION_THRESHOLD){
                return Rotation.z;
            }
        }
        return Rotation.noRotation;
    }

    private int computeAzimuth(float[] values){
        // calculate th rotation matrix
        SensorManager.getRotationMatrixFromVector( rMat, values );
        // get the azimuth value (orientation[0]) in degree
        return (int)( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[2] ) + 360 ) % 360;

    }

    /**
     * Detecta en que sentido se ha realizado la aceleración
     * @param values
     * @return
     */

    private LinearAcceleration detectAcceleration(float[] values) {
        long now = System.currentTimeMillis();

        final float alpha = 0.8f;
        float[] linear_acceleration = new float[3];

        mGravity[0] = alpha * mGravity[0] + (1 - alpha) * values[0];
        mGravity[1] = alpha * mGravity[1] + (1 - alpha) * values[1];
        mGravity[2] = alpha * mGravity[2] + (1 - alpha) * values[2];

        linear_acceleration[0] = values[0] - mGravity[0];
        linear_acceleration[1] = values[1] - mGravity[1];
        linear_acceleration[2] = values[2] - mGravity[2];

        if ((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            if(linear_acceleration[0] > SHAKE_THRESHOLD){
                return LinearAcceleration.x_minus;
            }
            else if (linear_acceleration[0] < -SHAKE_THRESHOLD){
                return LinearAcceleration.x_plus;
            }
            else if(linear_acceleration[1] < -SHAKE_THRESHOLD){
                return LinearAcceleration.y_minus;
            }
            else if(linear_acceleration[1] > SHAKE_THRESHOLD){
                return LinearAcceleration.y_plus;
            }
            else if(linear_acceleration[2] < -SHAKE_THRESHOLD){
                return LinearAcceleration.z_minus;
            }
            else if(linear_acceleration[2] > SHAKE_THRESHOLD){
                return LinearAcceleration.z_plus;
            }
        }
        return LinearAcceleration.noAcceleration;
    }

    /**
     * Activar altavoz interno
     */

    public void turnOffSpeaker(){
        AudioManager audio = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
        audio.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audio.setSpeakerphoneOn(false);
    }

    /**
     * Activar altavoz externo
     */

    public void turnOnSpeaker(){
        AudioManager audio = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
        audio.setMode(AudioManager.MODE_NORMAL);
        audio.setSpeakerphoneOn(true);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Movimiento para invocarlo: Right Shake
     */

    public abstract void Snext();

    /**
     * Movimiento para invocarlo: Left Shake
     */

    public abstract void Sprevious();

    /**
     * Movimiento para invocarlo: Up Shake
     */

    public abstract void Sup();

    /**
     * Movimiento para invocarlo: LLevar el movil hacia la oreja
     */

    public abstract void sound(TextToSpeech tts) ;

}