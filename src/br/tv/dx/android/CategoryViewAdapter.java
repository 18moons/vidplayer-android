package br.tv.dx.android;

import java.util.List;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class CategoryViewAdapter extends BaseAdapter {
	
	private Activity m_activity;
	private List<CategoryData> m_categories;
	
	CategoryViewAdapter(Activity activity) {
		m_activity = activity;
		AulasDBHelper helper = new AulasDBHelper(activity);
		SQLiteDatabase db = helper.getReadableDatabase();
		
		m_categories = AulasDBHelper.getCategories(db);
	}

	@Override
	public int getCount() {
		return m_categories.size();
	}

	@Override
	public Object getItem(int position) {
		return m_categories.get(position);
	}

	@Override
	public long getItemId(int position) {
		return m_categories.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	imageView = new ImageView(m_activity);
            imageView.setLayoutParams(new GridView.LayoutParams( m_size, m_size ));
            imageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
            //imageView.setPadding( m_size / 10, m_size / 10, m_size / 10, m_size / 10 );
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource( m_wallpapers[ position ].iconId );
        return imageView;
	}

}
