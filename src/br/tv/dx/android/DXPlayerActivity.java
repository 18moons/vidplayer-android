package br.tv.dx.android;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;

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
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);        
        
        // Check if the SD card is mounted and readable
        String state = Environment.getExternalStorageState();
        
        if (!(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
        	showDialog(DIALOG_SD_CARD_MOUNT_ERROR);
        	return;
        }
        
        File dir;
        
        try {
        	String path;
        	
        	try {
        		File p = getExternalFilesDir(null);
        		path = p.getAbsolutePath();
        	} catch (Exception e) {
        		path = Environment.getExternalStorageDirectory().getAbsolutePath();
        		path += "/Android/data/br.tv.dx.android/files";
        	}
        	
        	path += "/xml/";
        	
        	dir = new File(path);
        	if ( !dir.exists() && !dir.mkdirs() ) {
        		showDialog(DIALOG_SD_CARD_ACCESS_ERROR);
            	return;
        	}
        	
        	Log.d(TAG, path);
        }
        catch (Exception e) {
        	m_errorMessage = e.getLocalizedMessage();
        	showDialog(DIALOG_SD_CARD_UNKNOWN_ERROR);
        	return;
        }
        
        File files[] = dir.listFiles(new XmlFileNameFilter());
        
        AulasDBHelper helper = new AulasDBHelper( this );
		SQLiteDatabase db = helper.getWritableDatabase();
        
        for(File f : files) {
        	readDataFile(f, db);
        }
        
		startActivity(new Intent(this, CategoryViewActivity.class));		
		finish();
    }
    
    protected void readDataFile(File file, SQLiteDatabase db) {
    	
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	alert.setCancelable(true)
		.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	                DXPlayerActivity.this.finish();
	           }
	       });
    	
	    switch(id) {
	    case DIALOG_SD_CARD_MOUNT_ERROR:
	    case DIALOG_SD_CARD_ACCESS_ERROR:
	        alert.setTitle(getResources().getString(R.string.sd_card_error_title))
	        		.setMessage(R.string.sd_card_mount_error);
	        break;
	    case DIALOG_SD_CARD_UNKNOWN_ERROR:
	    	 alert.setTitle(getResources().getString(R.string.sd_card_error_title))
     				.setMessage( m_errorMessage );
	    default:
	        return null;
	    }
	    
	    return alert.create();
	}
}