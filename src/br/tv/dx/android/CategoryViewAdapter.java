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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class CategoryViewAdapter extends BaseAdapter {

	private Activity m_activity;
	private List<CategoryData> m_categories;

	private int m_padding;
	private int m_size;
	private int m_textSize;

	CategoryViewAdapter(Activity activity) {
		m_activity = activity;
		DXPlayerDBHelper helper = new DXPlayerDBHelper(activity);
		SQLiteDatabase db = helper.getReadableDatabase();

		m_categories = DXPlayerDBHelper.getCategories(db);

		db.close();

		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

		m_padding = (int) (5 * metrics.density);
		m_textSize = (int) (15 * metrics.density);

		Display display = activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		m_size = (width / 4) - (int) (20 * metrics.density);
	}

	@Override
	public int getCount() {
		return m_categories.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position == 0)
			return null;
		else
			return m_categories.get(position - 1);
	}

	@Override
	public long getItemId(int position) {
		if (position == 0)
			return -1;
		else
			return m_categories.get(position - 1).id;
	}

	private static final int TV_TITLE_ID = 0;
	private static final int TV_COUNT_ID = 1;
	private static final int IV_BKG_ID = 2;

	private void setBackground(ImageView v, CategoryData c) {
		if (c.imgButton != null) {
			try {
				Bitmap img = BitmapFactory.decodeFile(c.imgButton);
				v.setBackgroundDrawable(new BitmapDrawable(img));
				return;
			} catch (Exception e) {
				Log.e(DXPlayerActivity.TAG, "Error setting background", e);
			}
		}

		try {
			v.setBackgroundResource(R.drawable.item_bkg);
		} catch (Exception e) {
			Log.e(DXPlayerActivity.TAG, "Error setting background", e);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout layout;
		TextView tvTitle;
		TextView tvCount;
		ImageView ivBkg;

		CategoryData category = null;
		if (position != 0) {
			category = m_categories.get(position - 1);
			if (category == null) {
				return null;
			}
		}

		if (convertView == null) { // if it's not recycled, initialise some
			// attributes

			RelativeLayout.LayoutParams params;

			// Layout
			layout = new RelativeLayout(m_activity);

			// Background
			ivBkg = new ImageView(m_activity);
			ivBkg.setId(IV_BKG_ID);
			ivBkg.setScaleType(ScaleType.CENTER);

			params = new RelativeLayout.LayoutParams(m_size, m_size);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);

			layout.addView(ivBkg, params);

			// Title
			tvTitle = new TextView(m_activity);
			tvTitle.setId(TV_TITLE_ID);
			tvTitle.setTextSize(m_textSize);

			tvTitle.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);

			tvTitle.setTextColor(Color.WHITE);

			params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

			layout.addView(tvTitle, params);

			// Count
			tvCount = new TextView(m_activity);
			tvCount.setId(TV_COUNT_ID);
			tvCount.setTextSize(m_textSize);
			tvCount.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			tvCount.setTextColor(Color.WHITE);
			tvCount.setPadding(0, 0, m_padding, 0);

			params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			layout.addView(tvCount, params);

		} else {
			layout = (RelativeLayout) convertView;

			tvTitle = (TextView) layout.findViewById(TV_TITLE_ID);
			tvCount = (TextView) layout.findViewById(TV_COUNT_ID);
			ivBkg = (ImageView) layout.findViewById(IV_BKG_ID);
		}

		if (position == 0) {
			tvTitle.setText("");
			tvCount.setText("");
			ivBkg.setBackgroundResource(R.drawable.logo_home);
		} else {
			tvTitle.setText(category.title);
			tvCount.setText(Integer.toString(category.count));
			setBackground(ivBkg, category);
		}

		return layout;
	}
}
