package br.tv.dx.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class CategoryViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	setContentView(R.layout.categoryview);
    }
}
