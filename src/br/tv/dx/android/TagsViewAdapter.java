package br.tv.dx.android;

import java.util.List;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class TagsViewAdapter extends BaseAdapter {

	private AulaVideoPlayer m_activity;
	private List<String> m_tags;
	private float m_textSize;

	TagsViewAdapter(List<String> tags, AulaVideoPlayer activity) {
		m_tags = tags;
		m_activity = activity;

		m_textSize = ((TextView) m_activity.findViewById(R.id.tvSubtitle))
				.getTextSize();
	}

	@Override
	public int getCount() {
		return m_tags.size();
	}

	@Override
	public Object getItem(int position) {
		return m_tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if (convertView == null) { // if it's not recycled, initialise some
									// attributes
			textView = new TextView(m_activity);
			textView.setLayoutParams(new GridView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			textView.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL);
			textView.setTextColor(Color.WHITE);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, m_textSize);
		} else {
			textView = (TextView) convertView;
		}

		textView.setText(m_tags.get(position));
		return textView;
	}

}
