package br.tv.dx.android;

import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AulasViewAdapter extends BaseAdapter {
	
	private Activity m_activity;
	private List<ItemData> m_items;
	
	private int m_size;
	
	AulasViewAdapter(Activity activity, int categoryId) {
		m_activity = activity;
		DXPlayerDBHelper helper = new DXPlayerDBHelper(activity);
		SQLiteDatabase db = helper.getReadableDatabase();
		
		m_items = DXPlayerDBHelper.getItems(db, categoryId);
		
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		m_size = (int) (0.5 * metrics.ydpi);
	}

	@Override
	public int getCount() {
		return m_items.size();
	}

	@Override
	public Object getItem(int position) {
		return m_items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return m_items.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
        if (convertView == null) {  // if it's not recycled, initialise some attributes
        	textView = new TextView(m_activity);
        	textView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, m_size));
        	textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        	textView.setBackgroundResource(R.drawable.item_bkg);
        	textView.setTextColor(Color.WHITE);
        } else {
        	textView = (TextView) convertView;
        }

        textView.setText(m_items.get(position).title);
        return textView;
	}
}
