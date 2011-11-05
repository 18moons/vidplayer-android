package br.tv.dx.android;

import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class AulasViewAdapter extends BaseAdapter {

	private Activity m_activity;
	private List<ItemData> m_items;

	private int m_size;

	AulasViewAdapter(Activity activity, int categoryId) {
		m_activity = activity;
		DXPlayerDBHelper helper = new DXPlayerDBHelper(activity);
		SQLiteDatabase db = helper.getReadableDatabase();

		m_items = DXPlayerDBHelper.getItems(db, categoryId);

		db.close();

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Display display = activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();

		m_size = (width / 4) - (int) (20 * metrics.density);
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

	private boolean setBackground(View v, ItemData i) {
		if (i.image != null) {
			try {
				Bitmap img = BitmapFactory.decodeFile(i.image);
				v.setBackgroundDrawable(new BitmapDrawable(img));
				return true;
			} catch (Exception e) {
				Log.e(DXPlayerActivity.TAG, "Error setting background", e);
			}
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ItemData item = m_items.get(position);

		TextView textView;

		if (convertView == null) { // if it's not recycled, initialise some
			// attributes
			textView = new TextView(m_activity);
			textView.setLayoutParams(new GridView.LayoutParams(m_size, m_size));
			textView.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);

			textView.setTextColor(Color.WHITE);
		} else {
			textView = (TextView) convertView;
		}

		if (!setBackground(textView, item)) {
			textView.setText(item.title);
			textView.setBackgroundResource(R.drawable.item_bkg);
		}

		return textView;
	}
}
