package com.jeanrob.bottle;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jeanrob.bottle.BottleView;

class GestureListener implements GestureDetector.OnGestureListener,
	GestureDetector.OnDoubleTapListener {
    private static final String DEBUG_TAG = "Bottle";
    
	BottleView mView;
	
	public GestureListener(BottleView view) {
		this.mView = view;
	}
	
	@Override
    public boolean onDown(MotionEvent e) {        
		Log.v(DEBUG_TAG, "onDown");
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
            final float velocityX, final float velocityY) {
        Log.v(DEBUG_TAG, "onFling, velocity X is " + velocityX);
        
        // envoie le fling avec la vitesse de rotation
        // selon si le fling est en haut ou en bas, et selon l'angle actuel de rotation
        if((e1.getY() + e2.getY())/2 < mView.getThread().getCanvasHeight() / 2) {
        	mView.getThread().doFling(velocityX / 5);
        } else {
        	mView.getThread().doFling(- velocityX / 5);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.v(DEBUG_TAG, "onDoubleTap");
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.v(DEBUG_TAG, "onLongPress");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2,
            float distanceX, float distanceY) {
        Log.v(DEBUG_TAG, "onScroll");        
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.v(DEBUG_TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.v(DEBUG_TAG, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.v(DEBUG_TAG, "onDoubleTapEvent");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.v(DEBUG_TAG, "onSingleTapConfirmed");
        mView.getThread().doSingleTapConfirmed();
        return false;
    }

	
}