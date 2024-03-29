package br.tv.dx.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class CategoryViewActivity extends Activity implements
		OnItemClickListener {

	private CategoryViewAdapter m_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.categoryview);

		m_adapter = new CategoryViewAdapter(this);

		GridView v = (GridView) findViewById(R.id.gvCategories);
		v.setOnItemClickListener(this);
		v.setAdapter(m_adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent.getId() == R.id.gvCategories && id != -1) { // sanity check
			Intent intent = new Intent(this, AulasViewActivity.class);

			intent.putExtra("id", (int) id);

			startActivity(intent);
		}
	}
}
