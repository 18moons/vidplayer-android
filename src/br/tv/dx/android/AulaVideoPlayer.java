package br.tv.dx.android;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AulaVideoPlayer extends Activity implements OnClickListener {

	private ItemData m_item;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    	setContentView(R.layout.aulavideoplayer);
    	
    	TextView tvTitle = (TextView)findViewById(R.id.tvTitle);
    	TextView tvSubtitle = (TextView)findViewById(R.id.tvSubtitle);
    	TextView tvLink = (TextView)findViewById(R.id.tvLink);
    	TextView tvAttachment = (TextView)findViewById(R.id.tvAttachment);
    	
    	
    	Bundle extras = getIntent().getExtras();
    	if (extras.containsKey("id")) {
    		DXPlayerDBHelper helper = new DXPlayerDBHelper( this );
    		 SQLiteDatabase db = helper.getWritableDatabase();
    			
    		m_item = DXPlayerDBHelper.getItem(db, extras.getInt("id"));
    		
    		tvTitle.setText(m_item.title);
    		tvSubtitle.setText(m_item.subTitle);
    		tvLink.setText(m_item.link);
    		
    		tvAttachment.setOnClickListener(this);
    	}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.tvAttachment: {
			
		}
		}
	}
}
