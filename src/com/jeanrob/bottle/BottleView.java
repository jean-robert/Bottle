package com.jeanrob.bottle;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


/** Une View pour faire tourner la bouteille */
class BottleView extends SurfaceView implements SurfaceHolder.Callback {
	class BottleThread extends Thread {
		
		// Quelques constantes physiques pour la bouteille
		public static final double PHYS_DECEL = 60;
		
		// Quelques constantes d'états
		public static final int ETAT_PRET = 1;
		public static final int ETAT_ENCOURS = 2;
		public static final int ETAT_FINI = 3;		
		public static final int MIN_CHANCE = 2;
		
		/** Handler pour les messages du TextView */
		private Handler mHandler;					
		
		/** Hauteur du canvas
		 * @see #setSurfaceSize */
		private int mCanvasHeight = 1;
		
		/** Largeur du canvas
		 * @see #setSurfaceSize */
		private int mCanvasWidth = 1;

		/** Un bitmap pour le fond */
		private Bitmap mBackgroundImage;
		
		/** Hauteur en pixel de l'image de fond */
		private int mBackgroundHeight;
		
		/** Hauteur en pixel de l'image de fond */
		private int mBackgroundWidth;
		
		/** L'image de la bouteille */
		private Drawable mBottleImage;
		
		/** Hauteur en pixel de la bouteille */
		private int mBottleHeight;
		
		/** Largeur en pixel de la bouteille */
		private int mBottleWidth;
		
		/** Position X de la bouteille */
		private int mX;
		
		/** Position Y de la bouteille */
		private int mY;
		
		/** Angle actuel de la bouteille */
		private double mAngle;
		
		/** Vitesse de rotation */
		private double mDAngle;			
		
		/** Sens de rotation */
		private double mSens;
		
		/** Nombre de chances */
		private int mChances;
		
		/** Temps entre 2 frames */
		private double mLastTime;
	
		/** Etat du jeu */
		private double mEtat;
		
		/** Autre booléen pour savoir si on peut dessiner */
		private boolean mRun = false;
		
		/** Handler pour le gestionnaire de surface */
		private SurfaceHolder mSurfaceHolder;
				
		public BottleThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
			// recupere les handles
			mSurfaceHolder = surfaceHolder;
			mHandler = handler;
			mContext = context;
			
			Resources res = context.getResources();
			// recupere le dessin de la bouteille
			mBottleImage = res.getDrawable(R.drawable.bouteille);
			// recupere le dessin de fond en bitmap
			mBackgroundImage = BitmapFactory.decodeResource(res, R.drawable.rond);
			
			// recupere les dimensions de la teille et du fond
			mBottleHeight = mBottleImage.getIntrinsicHeight();
			mBottleWidth = mBottleImage.getIntrinsicWidth();
			mBackgroundHeight = mBackgroundImage.getHeight();
			mBackgroundWidth = mBackgroundImage.getWidth();
			
			// initialise la bouteille
			mX = mCanvasWidth / 2;
			mY = mCanvasHeight / 2;
			mAngle = 0;
			mDAngle = 0;			
			mChances = 4;
		}
		
		/** Lance la bouteille ! */
		public void doLancer(float vitesseRotation) {
			synchronized (mSurfaceHolder) {								
				// initialise la bouteille
                mX = mCanvasWidth / 2;
                mY = mCanvasHeight / 2;
                // mAngle = 0;
                mDAngle = vitesseRotation;
                mSens = Math.signum(vitesseRotation);
                
				mLastTime = System.currentTimeMillis();
				setState(ETAT_ENCOURS);
			}			
		}
		
		@Override
		public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mEtat == ETAT_ENCOURS) updateAngle();
                        doDessin(c);
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
		}
		
		/**
         * Utile pour dire au thread de tourner ou non
         * 
         * @param b true pour runner, false pour arreter 
         */
        public void setRunning(boolean b) {
            mRun = b;
        }
        
        /**
         * Change l'état du jeu
         * 
         * @see #setState(int, CharSequence)
         * @param mode un des ETAT_*
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }
        
        /**
         * Change l'état du jeu, mais en envoyant des messages
         * 
         * @param mode un des ETAT_*
         * @param message une string avec le message a afficher, ou null
         */
        public void setState(int mode, CharSequence message) {
        	synchronized (mSurfaceHolder) {
        		mEtat = mode;
        		
        		if (mEtat == ETAT_ENCOURS) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
        		} else {
        			mDAngle = 0;
                    Resources res = mContext.getResources();
                    CharSequence str = "";
                    if (mEtat == ETAT_PRET)
                    	str = res.getText(R.string.mode_pret);
                    else if (mEtat == ETAT_FINI)
                        str = res.getString(R.string.mode_fini)
                                + " " + (int) Math.ceil(mAngle / 360 * mChances) + " !";

                    if (message != null) {
                        str = message + "\n" + str;
                    }
                    
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
        		}
        	}
        }
        
        /* Un callback si les dimensions changent */
        public void setSurfaceSize(int width, int height) {
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // on remet l'image correctement
                if (width > height) {
                	mBackgroundImage = mBackgroundImage.createScaledBitmap(
                            mBackgroundImage, mBackgroundWidth * height / mBackgroundHeight, height, true);                	
                } else {
                	mBackgroundImage = mBackgroundImage.createScaledBitmap(
                        mBackgroundImage, width, mBackgroundHeight * width / mBackgroundWidth, true);
                }
            }
        }
        
        /** Pour remettre la bouteille a sa place */
        public void resetBottle() {
            mX = mCanvasWidth / 2;
            mY = mCanvasHeight / 2;
            mAngle = 0;
        }
        
        /** Change le nombre de chances */
        public void setChances(int change) {
        	synchronized (mSurfaceHolder) {     
        		if(mChances == MIN_CHANCE && change < 0) {
        	        Toast.makeText(mContext, "Pas moins de 2 chances !", Toast.LENGTH_SHORT).show();        			
        		} else {
        			mChances += change;        			
        		}
        	}
        }
                
        /** Quand on fait un single tap confirmed */
        boolean doSingleTapConfirmed() {
        	synchronized(mSurfaceHolder) {

        		if (mEtat == ETAT_FINI) {       
        			setState(ETAT_PRET);
        			return true;
        		}
        		
        		return false;
        	}        	
        }
        
        /** Quand on fait un fling */
        boolean doFling(float vitesseRotation) {
        	synchronized(mSurfaceHolder) {

        		if (mEtat == ETAT_PRET) {
        			doLancer(vitesseRotation);
        			return true;
        		}
        		
        		return false;
        	}        	
        }
        
        /** Pour récuperer le CanvasHeight */
        float getCanvasHeight() {
        	return (float) mCanvasHeight;        	
        }
        
        /** Pour récuperer le CanvasWidth */
        float getCanvasWidth() {
        	return (float) mCanvasWidth;        	
        }
        
        /** Dessine tout */
        private void doDessin(Canvas canvas) {
        	// dessine le cercle
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            float cX = (float) mCanvasWidth / 2;
            float cY = (float) mCanvasHeight / 2;
            float cR = (float) 0.9*(Math.min(mCanvasWidth, mCanvasHeight) / 2);
            canvas.drawCircle(cX, cY, cR, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(4);            
            
            paint.setColor(Color.GREEN);
            for (int i = 1; i <= mChances; i = i + 1) {
            	canvas.drawLine(cX, cY, (float) (cX + cR*Math.sin(i * 2 * Math.PI / mChances)), (float) (cY - cR*Math.cos(i * 2 * Math.PI / mChances)), paint);            	
            }
            paint.setColor(Color.WHITE);
            canvas.drawCircle(cX, cY, cR, paint);
            
            // dessine la bouteille
            if(mEtat == ETAT_PRET) resetBottle();
	        canvas.save();
	        canvas.rotate((float) mAngle, (float) mX, mCanvasHeight
	                    - (float) mY);
	        int yTop = mCanvasHeight - ((int) mY + mBottleHeight / 2);
	        int xLeft = (int) mX - mBottleWidth / 2;
	        mBottleImage.setBounds(xLeft, yTop, xLeft + mBottleWidth, yTop
	                        + mBottleHeight);
	        mBottleImage.draw(canvas);
	        canvas.restore();
        }
        
        /** Change l'angle */
        private void updateAngle() {
        	long now = System.currentTimeMillis();
        	
        	double elapsed = (now - mLastTime) / 1000.0;
        	
        	// mets à jour l'angle par rapport à la vitesse
        	mAngle += mDAngle * elapsed;
        	if(mAngle > 360) {
        		mAngle -= 360;
        	} else if(mAngle < 0) {
        		mAngle += 360;
        	}
        	
        	// ajoute des frottements et change la vitesse
        	mDAngle -= mSens * PHYS_DECEL * elapsed;
        	
        	mLastTime = now;
        	
        	// vois si on a fini de tourner...
        	if (mSens*mDAngle <= 0) {
        		mDAngle = 0;     		
        		setState(ETAT_FINI);
        	}        	
        }
	}
        
    /** Handle pour le Context qui ramene les images */
    private Context mContext;

    /** Pointeur vers le TextView */
    private TextView mStatusText;
    
    /** Le thread qui va montrer l'animation */
    private BottleThread thread;
    
    /** Le gesture detector */
    private GestureDetector mGestureDetector;

    public BottleView(Context context, AttributeSet attrs) {
    	super(context, attrs);

        // ajoute des callbacks et un appel pour changement
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // crée le thread; on le demarre dans surfaceCreated()
        thread = new BottleThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });
        
        // construit le gesture detector
        mGestureDetector = new GestureDetector(context, new GestureListener(this));
        
        setFocusable(true); // pour recuperer les key events
    }
	
    /** Recupere le thread */
    public BottleThread getThread() {
        return thread;
    }
    
    /** Override le touch event du GestureDetector */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	return mGestureDetector.onTouchEvent(event);
    }
    
    /** Ajoute un pointeur vers le TextView */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }
        
    /* Callback si les surfaces changent */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
    }
    
    /* Callback quand la surface a été crée et prete a l'emploi */
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }
    
    /* Callback quand la surface est détruite */
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

}






