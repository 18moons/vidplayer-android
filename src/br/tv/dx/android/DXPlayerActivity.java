package br.tv.dx.android;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class DXPlayerActivity extends Activity {

	static final public String TAG = "DXPlayer";

	static final private int DIALOG_SD_CARD_UNKNOWN_ERROR = 0;
	static final private int DIALOG_SD_CARD_MOUNT_ERROR = 1;
	static final private int DIALOG_SD_CARD_ACCESS_ERROR = 2;

	private String m_errorMessage;

	private class XmlFileNameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String filename) {
			return filename.endsWith(".xml");
		}
	};

	public void debugGetFiles(String outPath) {
		String urlBase = "http://bitforge.com.br/dx-player-android/samples/";
		try {
			URL url = new URL(urlBase + "files.lst");

			URLConnection con = url.openConnection();

			InputStream is = con.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			ByteArrayBuffer baf = new ByteArrayBuffer(1024);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			is.close();

			for (String file : new String(baf.buffer()).split("\n")) {
				file = file.trim();
				if (file.length() < 5)
					continue;

				url = new URL(urlBase + file);

				con = url.openConnection();

				try {
					is = con.getInputStream();
				} catch (FileNotFoundException e) {
					Log.e(TAG, "File not found: '" + urlBase + file + "'", e);
					continue;
				}

				bis = new BufferedInputStream(is);

				Log.d(TAG, "downloading file: '" + outPath + "/" + file + "'");
				FileOutputStream fos = new FileOutputStream(outPath + "/"
						+ file);

				// Notify the user
				updateUI("Downloading: " + file);

				byte data[] = new byte[1024];

				int count;
				while ((count = bis.read(data)) != -1) {
					fos.write(data, 0, count);
				}

				fos.flush();
				fos.close();
				is.close();
			}

		} catch (Exception e) {
			Log.e(TAG, "Error", e);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		// Check if the SD card is mounted and readable
		String state = Environment.getExternalStorageState();

		if (!(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY
				.equals(state))) {
			showDialog(DIALOG_SD_CARD_MOUNT_ERROR);
			return;
		}

		final File dir;

		try {
			String path;

			try {
				File p = getExternalFilesDir(null);
				path = p.getAbsolutePath();
			} catch (Exception e) {
				path = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				path += "/Android/data/br.tv.dx.android/files";
			}

			// TODO Debug
			new File(path + "/midia/").mkdirs();

			path += "/xml/";

			dir = new File(path);
			if (!dir.exists() && !dir.mkdirs()) {
				showDialog(DIALOG_SD_CARD_ACCESS_ERROR);
				return;
			}

			Log.d(TAG, path);
		} catch (Exception e) {
			m_errorMessage = e.getLocalizedMessage();
			showDialog(DIALOG_SD_CARD_UNKNOWN_ERROR);
			return;
		}

		// Run in background thread so the UI is not frozen
		new Thread() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();

				File files[] = dir.listFiles(new XmlFileNameFilter());

				// TODO Debug
				if (files.length == 0) {
					debugGetFiles(dir.getParent());
					files = dir.listFiles(new XmlFileNameFilter());
				}
				// end debug

				DXPlayerDBHelper helper = new DXPlayerDBHelper(
						DXPlayerActivity.this);
				SQLiteDatabase db = helper.getWritableDatabase();

				DXPlayerDBHelper.removeIncompleteFiles(db);
				DXPlayerDBHelper.resetFiles(db);

				final String xmlProcessing = getResources().getString(
						R.string.xml_processing);

				for (File f : files) {
					Pair<Integer, Boolean> fileId = DXPlayerDBHelper.getFileID(
							db, f.getName());
					try {
						if (fileId.second) {
							// Notify the user
							updateUI(String.format(xmlProcessing, f.getName()));

							// Process the XML data into the DB
							readDataFile(f, fileId.first, db);

							// Mark the file as OK. If exception is thrown, file
							// is
							// removed immediately or on next boot
							DXPlayerDBHelper
									.setFileAsFinished(db, fileId.first);
						}
					} catch (Exception e) {
						DXPlayerDBHelper.removeFile(db, fileId.first);
						Log
								.e(TAG, "Error reading file: "
										+ f.getAbsolutePath(), e);
					}
				}

				DXPlayerDBHelper.cleanUpDb(db);
				db.close();

				// Make sure the splash screen is show for at least X seconds
				try {
					long time = 2000 - (System.currentTimeMillis() - start);
					if (time > 0)
						sleep(time);
				} catch (InterruptedException e) {
				}

				startActivity(new Intent(DXPlayerActivity.this,
						CategoryViewActivity.class));
				finish();
			}
		}.start();
	}

	// If a file is being processed, notify the user
	private void updateUI(final String msg) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View pb = findViewById(R.id.ProgressBar);
				if (pb.getVisibility() != View.VISIBLE)
					pb.setVisibility(View.VISIBLE);

				TextView tv = (TextView) findViewById(R.id.TextView);
				if (tv.getVisibility() != View.VISIBLE)
					tv.setVisibility(View.VISIBLE);

				tv.setText(msg);
			}
		});
	}

	protected void readDataFile(File file, int fileId, SQLiteDatabase db) {
		// sax stuff
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();

			XMLReader reader = sp.getXMLReader();

			reader.setContentHandler(new XMLFileParser(fileId,
					file.getParent(), db));

			reader.parse(new InputSource(new FileInputStream(file)));

		} catch (ParserConfigurationException pce) {
			Log.e(TAG, "sax parse error", pce);
		} catch (SAXException se) {
			Log.e(TAG, "sax error", se);
		} catch (IOException ioe) {
			Log.e(TAG, "sax parse io error", ioe);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setCancelable(true).setNegativeButton(R.string.exit,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						DXPlayerActivity.this.finish();
					}
				});

		switch (id) {
		case DIALOG_SD_CARD_MOUNT_ERROR:
		case DIALOG_SD_CARD_ACCESS_ERROR:
			alert.setTitle(
					getResources().getString(R.string.sd_card_error_title))
					.setMessage(R.string.sd_card_mount_error);
			break;
		case DIALOG_SD_CARD_UNKNOWN_ERROR:
			if (m_errorMessage == null)
				m_errorMessage = getString(R.string.sd_card_read_error);

			alert.setTitle(
					getResources().getString(R.string.sd_card_error_title))
					.setMessage(m_errorMessage);
			break;
		default:
			return null;
		}

		return alert.create();
	}
}