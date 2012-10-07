package com.jeanrob.bottle;

import com.jeanrob.bottle.BottleView.BottleThread;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Le jeu de la bouteille
 */

public class Bottle extends Activity {
    
	/** Un handle pour le thread qui fera l'animation. */
	private BottleThread mBottleThread;
	
	/** Un handle pour la View où la bouteille est lancée. */
	private BottleView mBottleView;		
		
	/** Pour la création de l'Activity */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // On utilise le bon layout
        setContentView(R.layout.bottle_layout);
        
        // Récupere les handles pour la View et le thread
        mBottleView = (BottleView) findViewById(R.id.bottle);
        mBottleThread = mBottleView.getThread();
        
        // Fourni un handle a la View pour passer des messages dans le TextView
        mBottleView.setTextView((TextView) findViewById(R.id.text));       
        
        // Passe l'état à Prêt
        mBottleThread.setState(BottleThread.ETAT_PRET);
    }

    public void onClickMoins(View view) {
    	mBottleThread.setChances(-1);  
    }
    
    public void onClickPlus(View view) {
    	mBottleThread.setChances(+1);  
    } 
}