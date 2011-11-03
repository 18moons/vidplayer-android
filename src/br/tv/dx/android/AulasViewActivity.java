package br.tv.dx.android;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AulasViewActivity extends Activity implements OnItemClickListener {

	private AulasViewAdapter m_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aulasview);

		Bundle extras = getIntent().getExtras();

		ImageView ivBackground = (ImageView) findViewById(R.id.ivBackground);
		TextView tvCategory = (TextView) findViewById(R.id.tvCategory);
		GridView gvAulas = (GridView) findViewById(R.id.gvAulas);

		CategoryData category = null;

		if (extras.containsKey("id")) {
			DXPlayerDBHelper helper = new DXPlayerDBHelper(this);
			SQLiteDatabase db = helper.getReadableDatabase();

			category = DXPlayerDBHelper.getCategory(db, (int) extras
					.getInt("id"));

			db.close();
		} else {
			gvAulas.setVisibility(View.GONE);
		}

		if (category != null && category.title != null) {
			tvCategory.setText(category.title);
		} else {
			tvCategory.setVisibility(View.GONE);
		}

		if (category != null && category.id != 0) {
			m_adapter = new AulasViewAdapter(this, (int) extras.getInt("id"));
			gvAulas.setOnItemClickListener(this);
			gvAulas.setAdapter(m_adapter);
		} else {
			gvAulas.setVisibility(View.GONE);
		}

		try {
			Bitmap img = BitmapFactory.decodeFile(category.imgBackground);
			ivBackground.setBackgroundDrawable(new BitmapDrawable(img));
		} catch (Exception e) {
			Log.e(DXPlayerActivity.TAG, "Error setting background", e);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.gvAulas) { // sanity check
			Intent intent = new Intent(this, AulaVideoPlayer.class);

			intent.putExtra("id", (int) id);

			startActivity(intent);
		}
	}
}
