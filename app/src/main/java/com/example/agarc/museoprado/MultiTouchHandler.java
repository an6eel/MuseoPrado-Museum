package com.example.agarc.museoprado;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import java.util.Map;

import static android.view.MotionEvent.*;

public abstract class MultiTouchHandler implements OnGestureListener, GestureDetector.OnDoubleTapListener {

    private Context cxt;
    GestureDetector mGestureDetector;
    private static final float SCROLL_THRESHOLD = 500.0f;
    private static final float POINTS_THRESHOLD = 350.0f;

    public class Point{
        private float _x;
        private float _y;
        private int ind;

        public Point(float x,float y,int idx){
            _x=x;
            _y=y;
            ind=idx;
        }
    }

    private Point first1,first2,last1,last2;
    private boolean move;
    private boolean error;

    public MultiTouchHandler(Context cx){
        cxt = cx;
        mGestureDetector = new GestureDetector(cxt,this);
        move = false;
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

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distX = e1.getX()-e2.getX();
        float distY = e1.getY()-e2.getY();

        if(Math.abs(distX)> SCROLL_THRESHOLD){
            if(distX < 0){
                return scrollRight();
            }
            else{
                return scrollLeft();
            }

        }
        else if (Math.abs(distY)> POINTS_THRESHOLD){
            if(distY < 0) {
                return scrollDown();
            }
            else{
                 return  scrollUp();
            }
        }

        return false;

    }

    public void setEvent(MotionEvent e){
        if(e.getPointerCount() == 2){ // MULTI-TOUCH
            onMultiTouch(e);
        }
        else // SIMPLE-SCROLL
            mGestureDetector.onTouchEvent(e);
    }

    public void onMultiTouch(MotionEvent e){
        final int actionPeformed = e.getAction() & MotionEvent.ACTION_MASK;
        error=false;
        switch (actionPeformed){
            case ACTION_DOWN:
                break;
            case ACTION_POINTER_DOWN:
                int ind0=e.getX(0)<e.getX(1) ? 0:1;
                int ind1=ind0 == 0 ? 1:0;
                first1 = new Point(e.getX(ind0),e.getY(ind0),ind0);
                first2 = new Point(e.getX(ind1),e.getY(ind1),ind1);

                if(Math.abs(first1._y-first2._y) >= POINTS_THRESHOLD)
                    error = true;

                break;
            case ACTION_POINTER_UP:
                last2 = new Point(e.getX(first2.ind),e.getY(first2.ind),first2.ind);
                last1 = new Point(e.getX(first1.ind),e.getY(first1.ind),first1.ind);

                if( !error && (last1._y-first1._y) >= SCROLL_THRESHOLD ){
                    if( (last2._y-first2._y) >= SCROLL_THRESHOLD  && (last2._y-first2._y) < 2*SCROLL_THRESHOLD ){

                        if( (first1._x-last1._x)>= POINTS_THRESHOLD && Math.abs(last2._x-first2._x)<=POINTS_THRESHOLD )
                            mTouchLeft();
                        else if( (last2._x-first2._x) >= POINTS_THRESHOLD && Math.abs(last1._x-first1._x) <= POINTS_THRESHOLD )
                            mTouchRight();
                        else if( (Math.abs(last2._x-first2._x)<=POINTS_THRESHOLD) && (Math.abs(last1._x-first1._x) <= POINTS_THRESHOLD))
                            mTouchCenter();
                    }
                    else if((last2._y-first2._y) >= 2*SCROLL_THRESHOLD ){
                        mTouchDown();
                    }
                    else if( ( (last2._y-first2._y) < POINTS_THRESHOLD ) || ((last1._y-first1._y) < POINTS_THRESHOLD )) {
                        mTouchUp();
                    }
                }

                break;
            case ACTION_UP:
                break;
        }
    }

    public abstract boolean scrollRight();
    public abstract boolean scrollLeft();
    public abstract boolean scrollUp();
    public abstract boolean scrollDown();
    public abstract boolean mTouchUp();
    public abstract boolean mTouchLeft();
    public abstract boolean mTouchRight();
    public abstract boolean mTouchDown();
    public abstract boolean mTouchCenter();

}
