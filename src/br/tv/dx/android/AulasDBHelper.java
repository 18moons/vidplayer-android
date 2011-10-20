package br.tv.dx.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AulasDBHelper extends SQLiteOpenHelper {
	
	static final public String TAG = DXPlayerActivity.TAG;
	
	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aulas";

    private static final String DATABASE_CREATE_XML_FILES =
    	"create table xml_files (" +
		"	id_file INTEGER PRIMARY KEY," +
		"	file_name text," +
		"	checked int " +
		");";


	public AulasDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
    	Log.d(TAG, "Create tables");
        db.execSQL(DATABASE_CREATE_XML_FILES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Update tables");
		//if ( oldVersion < 2 ) db.execSQL( "alter table ...." );	
	}

}
