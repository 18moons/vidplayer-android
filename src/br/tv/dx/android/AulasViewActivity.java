package br.tv.dx.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AulasViewActivity extends Activity implements OnItemClickListener {
	
	private AulasViewAdapter m_adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.aulasview);
    	
    	Bundle extras = getIntent().getExtras();
    	
    	TextView tvCategory = (TextView)findViewById(R.id.tvCategory);
    	GridView gvAulas = (GridView)findViewById(R.id.gvAulas);
    	
    	if (extras.containsKey("title")) {
    		tvCategory.setText(extras.getString("title"));
    	} else {
    		tvCategory.setVisibility(View.GONE);
    	}
    	
    	if (extras.containsKey("id")) {
	    	m_adapter = new AulasViewAdapter(this, (int) extras.getInt("id"));
	    	gvAulas.setOnItemClickListener(this);
	    	gvAulas.setAdapter(m_adapter);
    	} else {
    		gvAulas.setVisibility(View.GONE);
    	}
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(parent.getId() == R.id.gvAulas) { // sanity check
			Intent intent = new Intent(this, AulaVideoPlayer.class);
						
			intent.putExtra("id", (int) id);
						
			startActivity(intent);
		}
	}
}
