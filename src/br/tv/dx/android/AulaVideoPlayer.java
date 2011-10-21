package br.tv.dx.android;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

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
    	TextView tvBack = (TextView)findViewById(R.id.tvBack);
    	
    	GridView gvTags = (GridView)findViewById(R.id.gvTags);
    	
    	Bundle extras = getIntent().getExtras();
    	if (extras.containsKey("id")) {
    		DXPlayerDBHelper helper = new DXPlayerDBHelper(this);
    		SQLiteDatabase db = helper.getReadableDatabase();
    		
    		m_item = DXPlayerDBHelper.getItem(db, extras.getInt("id"));
    		
    		db.close();
    		
    		tvTitle.setText(m_item.title);
    		tvSubtitle.setText(m_item.subTitle);
    		tvLink.setText(m_item.link);
    		
    		tvLink.setOnClickListener(this);
    		tvAttachment.setOnClickListener(this);
    		tvBack.setOnClickListener(this);
    		
    		gvTags.setAdapter(new TagsViewAdapter(m_item.tags, this));
    	}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		
		case R.id.tvBack: {
			finish();
		}
		break;
		
		case R.id.tvLink: {
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(m_item.link));
			startActivity(myIntent);
		}
		break;
		
		case R.id.tvAttachment: {
			File file = new File(m_item.attachments.get(0).file);
			
			if (file.exists()) {
				Uri path = Uri.fromFile(file);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(path, "application/pdf");
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				
				try {
					startActivity(intent);
				} 
				catch (ActivityNotFoundException e) {
					Toast.makeText(AulaVideoPlayer.this, R.string.pdf_app_not_found, Toast.LENGTH_SHORT).show();
				}
			} else {
				String msg = String.format(getResources().getString(R.string.pdf_not_found), m_item.attachments.get(0).file);
				Toast.makeText(AulaVideoPlayer.this, msg, Toast.LENGTH_SHORT).show();
			}
		}
		break;
		
		}
	}
}
