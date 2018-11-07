package com.example.agarc.museoprado;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.Toast;

import static android.view.MotionEvent.*;

/**
 * Clase que gestiona los eventos simple-touch y multitouch sobre la pantalla
 */

public abstract class MultiTouchHandler implements OnGestureListener, GestureDetector.OnDoubleTapListener {

    /**
     * Contexto del Activity donde se gestionan los eventos
     */

    private Context cxt;

    /**
     * Atributo que detecta los eventos simple-touch
     */

    GestureDetector mGestureDetector;

    /**
     * Screen-Height
     */

    private final float HEIGHT;

    /**
     * Screen-Width
     */

    private final float WIDTH;

    /**
     * Scroll Width Scale
     */

    private static final float WIDTH_SCALE = 2.7f;

    /**
     * Scroll Height Scale
     */

    private static final float HEIGHT_SCALE = 3.7f;

    /**
     * Punto que indica la posicion del dedo sobre la pantalla
     */

    public class PointM{
        private float _x;
        private float _y;
        private int ind;

        public PointM(float x,float y,int idx){
            _x=x;
            _y=y;
            ind=idx;
        }
    }

    /**
     * Puntos que corresponden con la posicion de los dos dedos cuando tocan la pantalla
     * y cuando abandonan la pantalla
     */

    private PointM first1,first2,last1,last2;

    /**
     * Flag que indica si ha habido algun error en el scroll
     */
    private boolean error;

    /**
     * <pre>
     * Constructor del MultiTouchHandler
     * Inicializa el detector de eventos singletouch y obtiene las dimensiones de la pantalla
     * </pre>
     * @param cx
     * @param screen
     */

    public MultiTouchHandler(Context cx,Display screen){
        cxt = cx;
        mGestureDetector = new GestureDetector(cxt,this);
        Point scr = new Point();
        screen.getSize(scr);
        HEIGHT = scr.y;
        WIDTH = scr.x;
        error = false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    /**
     * <pre>
     * Metodo que se lanza cuando se ha producido un simple-touch scroll.
     *
     * Si se ha producido un scroll hacia la derecha se llama a @see {@link #scrollRight()} \n
     * Si se ha producido un scroll hacia la izquierda se llama a @see {@link #scrollLeft()} \n
     * Si se ha producido un scroll hacia arriba se llama a @see {@link #scrollUp()} \n
     * Si se ha producido un scroll hacia la derecha se llama a @see {@link #scrollDown()}
     * </pre>
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distX = e1.getX()-e2.getX();
        float distY = e1.getY()-e2.getY();

        if(Math.abs(distX)> WIDTH/WIDTH_SCALE){
            if(distX < 0){
                return scrollRight();
            }
            else{
                return scrollLeft();
            }

        }
        else if (Math.abs(distY)> HEIGHT/HEIGHT_SCALE){
            if(distY < 0) {
                return scrollDown();
            }
            else{
                 return  scrollUp();
            }
        }

        return false;

    }

    /**
     * Maneja el evento recibido en funcion si es de tipo single-touch
     * o si es multi-touch
     * @param e
     */

    public void setEvent(MotionEvent e){
        if(e.getPointerCount() == 2){ // MULTI-TOUCH
            onMultiTouch(e);
        }
        else // SIMPLE-SCROLL
            mGestureDetector.onTouchEvent(e);
    }

    /**
     * <pre>
     * Maneja los eventos multi-touch
     *
     * Gestos:
     *        Scroll con dos dedos hacia el centro: @see {@link #mTouchCenter()} \n
     *        Scroll con dos dedos hacia abajo: @see {@link #mTouchDown()} \n
     *        Scroll con dos dedos hacia el centro y un dedo hacia la izquierda: @see {@link #mTouchLeft()} \n
     *        Scroll con dos dedos hacia el centro y un dedo hacia la derecha: @see {@link #mTouchRight()} \n
     *        Scroll de un dedo al centro y el otro queda arriba: @see {@link #mTouchUp()}
     * </pre>
     * @param e
     */

    public void onMultiTouch(MotionEvent e){
        final int actionPeformed = e.getAction() & MotionEvent.ACTION_MASK;
        error=false;
        switch (actionPeformed){
            case ACTION_DOWN:
                break;
            case ACTION_POINTER_DOWN:
                int ind0=e.getX(0)<e.getX(1) ? 0:1;
                int ind1=ind0 == 0 ? 1:0;
                first1 = new PointM(e.getX(ind0),e.getY(ind0),ind0);
                first2 = new PointM(e.getX(ind1),e.getY(ind1),ind1);

                if(Math.abs(first1._y-first2._y) >= WIDTH/WIDTH_SCALE)
                    error = true;

                break;
            case ACTION_POINTER_UP:
                last2 = new PointM(e.getX(first2.ind),e.getY(first2.ind),first2.ind);
                last1 = new PointM(e.getX(first1.ind),e.getY(first1.ind),first1.ind);

                if( !error && (last1._y-first1._y) >= HEIGHT/HEIGHT_SCALE ){
                    if( (last2._y-first2._y) >= HEIGHT/HEIGHT_SCALE && (last2._y-first2._y) < 2*HEIGHT/HEIGHT_SCALE ){

                        if( (first1._x-last1._x)>= WIDTH/WIDTH_SCALE && Math.abs(last2._x-first2._x)< WIDTH/2.7)
                            mTouchLeft();
                        else if( (last2._x-first2._x) >= WIDTH/WIDTH_SCALE && Math.abs(last1._x-first1._x) <= WIDTH/WIDTH_SCALE)
                            mTouchRight();
                        else if( (Math.abs(last2._x-first2._x)<= WIDTH/WIDTH_SCALE) && (Math.abs(last1._x-first1._x) <= WIDTH/WIDTH_SCALE))
                            mTouchCenter();
                    }
                    else if((last2._y-first2._y) >= HEIGHT/HEIGHT_SCALE ){
                        mTouchDown();
                    }
                    else if( ( (last2._y-first2._y) < HEIGHT/HEIGHT_SCALE) || ((last1._y-first1._y) < HEIGHT/HEIGHT_SCALE)) {
                        mTouchUp();
                    }
                }

                break;
            case ACTION_UP:
                break;
        }
    }

    /**
     * Scroll con un dedo hacia la derecha
     * @return
     */

    public abstract boolean scrollRight();

    /**
     * Scroll con un dedo hacia la izquierda
     * @return
     */

    public abstract boolean scrollLeft();

    /**
     * Scroll con un dedo hacia arriba
     * @return
     */

    public abstract boolean scrollUp();

    /**
     * Scroll con un dedo hacia abajo
     * @return
     */

    public abstract boolean scrollDown();

    /**
     * Scroll de un dedo al centro y el otro queda arriba
     * @return
     */

    public abstract boolean mTouchUp();

    /**
     * Scroll con dos dedos hacia el centro y un dedo hacia la izquierda
     * @return
     */

    public abstract boolean mTouchLeft();

    /**
     * Scroll con dos dedos hacia el centro y un dedo hacia la derecha
     * @return
     */

    public abstract boolean mTouchRight();

    /**
     * Scroll con dos dedos hacia abajo
     * @return
     */

    public abstract boolean mTouchDown();

    /**
     * Scroll con dos dedos hacia el centro
     * @return
     */

    public abstract boolean mTouchCenter();

}
