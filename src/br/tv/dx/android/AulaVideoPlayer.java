package br.tv.dx.android;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class AulaVideoPlayer extends Activity implements OnClickListener {

	static final private int DIALOG_VIDEO_NOT_FOUND = 0;
	static final private int DIALOG_VIDEO_PREPARE_ERROR = 1;

	private ItemData m_item;

	// UI Elements
	private RelativeLayout m_layDetails;

	private TextView m_tvTitle;
	private TextView m_tvCategory;
	private TextView m_tvSubtitle;
	private TextView m_tvTeacher;
	private TextView m_tvLink;
	private TextView m_tvAttachment;
	private TextView m_tvBack;
	private TextView m_tvTags;

	private VideoView m_vvVideo;
	private MediaController m_contoller;

	private int m_msgIndex = 0;

	private class PlayerHandler extends Handler {
		public void handleMessage(Message msg) {
			if (msg.what == m_msgIndex) {
				m_layDetails.setVisibility(View.INVISIBLE);
				m_tvBack.setVisibility(View.INVISIBLE);
			}
		}
	};

	private PlayerHandler m_handler = new PlayerHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.aulavideoplayer);

		m_layDetails = (RelativeLayout) findViewById(R.id.layDetails);

		m_tvTitle = (TextView) findViewById(R.id.tvTitle);
		m_tvCategory = (TextView) findViewById(R.id.tvCategory);
		m_tvSubtitle = (TextView) findViewById(R.id.tvSubtitle);
		m_tvTeacher = (TextView) findViewById(R.id.tvTeacher);
		m_tvLink = (TextView) findViewById(R.id.tvLink);
		m_tvAttachment = (TextView) findViewById(R.id.tvAttachment);
		m_tvBack = (TextView) findViewById(R.id.tvBack);
		m_tvTags = (TextView) findViewById(R.id.tvTags);

		m_vvVideo = (VideoView) findViewById(R.id.vvVideo);

		Bundle extras = getIntent().getExtras();
		if (extras.containsKey("id")) {
			DXPlayerDBHelper helper = new DXPlayerDBHelper(this);
			SQLiteDatabase db = helper.getReadableDatabase();

			m_item = DXPlayerDBHelper.getItem(db, extras.getInt("id"));

			CategoryData category = DXPlayerDBHelper.getCategory(db,
					m_item.category);

			db.close();

			if (m_item.title == null) {
				m_tvTitle.setVisibility(View.GONE);
			} else {
				m_tvTitle.setText(m_item.title);
			}

			if (m_item.subTitle == null) {
				m_tvSubtitle.setVisibility(View.GONE);
			} else {
				m_tvSubtitle.setText(m_item.subTitle);
			}

			if (m_item.teacher == null) {
				m_tvTeacher.setVisibility(View.GONE);
			} else {
				m_tvTeacher.setText(m_item.teacher);
			}

			if (m_item.link == null) {
				m_tvLink.setVisibility(View.GONE);
			} else {
				m_tvLink.setText(m_item.link);
			}

			if (category == null || category.title == null) {
				m_tvCategory.setVisibility(View.GONE);
			} else {
				m_tvCategory.setText(category.title);
			}

			try {
				if (!new File(m_item.video).exists()) {
					showDialog(DIALOG_VIDEO_NOT_FOUND);
					return;
				}
			} catch (NullPointerException e) {
				showDialog(DIALOG_VIDEO_NOT_FOUND);
				return;
			}

			try {
				// get current window information, and set format, set it up
				// differently, if you need some special effects
				getWindow().setFormat(PixelFormat.TRANSLUCENT);
				// MediaController is the ui control howering above the video
				// (just
				// like in the default youtube player).
				m_contoller = new MediaController(this);
				m_vvVideo.setMediaController(m_contoller);
				// passing a video file to the video holder
				m_vvVideo.setVideoURI(Uri.parse(m_item.video));
				// get focus, before playing the video.
				m_vvVideo.requestFocus();

				m_vvVideo.start();
				m_handler.sendEmptyMessageDelayed(++m_msgIndex, 3000);

			} catch (Exception e) {
				Log.e(DXPlayerActivity.TAG, "Error preparing video: "
						+ m_item.video, e);
				showDialog(DIALOG_VIDEO_PREPARE_ERROR);
				return;
			}

			findViewById(R.id.rlLayout).setOnClickListener(this);

			m_tvLink.setOnClickListener(this);
			m_tvAttachment.setOnClickListener(this);
			m_tvBack.setOnClickListener(this);
			m_contoller.setOnClickListener(this);
			m_vvVideo.setOnClickListener(this);

			String tags = "";
			for (String s : m_item.tags) {
				tags += s + " - ";
			}

			tags.trim();
			if (tags.length() > 2)
				m_tvTags.setText(tags.substring(0, tags.length() - 2));
			else
				m_tvTags.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tvBack: {
			finish();
			break;
		}

		case R.id.tvLink: {
			try {
				Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse(m_item.link));
				startActivity(myIntent);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(AulaVideoPlayer.this, R.string.link_invalid,
						Toast.LENGTH_SHORT).show();
			}
			break;
		}

		case R.id.tvAttachment: {
			File file = new File(m_item.attachments.get(0).file);

			if (file.exists()) {
				try {
					Uri path = Uri.fromFile(file);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(path, "application/pdf");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(AulaVideoPlayer.this,
							R.string.pdf_app_not_found, Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				String msg = String
						.format(getResources()
								.getString(R.string.pdf_not_found),
								m_item.attachments.get(0).file);
				Toast.makeText(AulaVideoPlayer.this, msg, Toast.LENGTH_SHORT)
						.show();
			}

			break;
		}

		case R.id.rlLayout:
			m_contoller.show();
		case R.id.vvVideo:
			m_layDetails.setVisibility(View.VISIBLE);
			m_tvBack.setVisibility(View.VISIBLE);
			m_handler.sendEmptyMessageDelayed(++m_msgIndex, 3000);
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		Resources res = getResources();

		alert.setCancelable(true).setNegativeButton(R.string.exit,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						AulaVideoPlayer.this.finish();
					}
				});

		switch (id) {
		case DIALOG_VIDEO_NOT_FOUND:
			alert.setTitle(res.getString(R.string.video_error_title));
			alert.setMessage(String.format(res
					.getString(R.string.video_not_found), m_item.video));
			break;
		case DIALOG_VIDEO_PREPARE_ERROR:
			alert.setTitle(res.getString(R.string.video_error_title));
			alert.setMessage(String.format(res
					.getString(R.string.video_error_preparing), m_item.video));
			break;
		default:
			return null;
		}

		return alert.create();
	}
}
