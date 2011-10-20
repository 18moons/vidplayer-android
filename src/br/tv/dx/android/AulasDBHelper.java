package br.tv.dx.android;

import android.content.Context;
import android.database.Cursor;
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
    
    private static final String DATABASE_CREATE_CATEGORIES =
    	"create table categories (" +
		"	id_category INTEGER PRIMARY KEY," +
		"	category text" +
		");";


	public AulasDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
    	Log.d(TAG, "Create tables");
        db.execSQL(DATABASE_CREATE_XML_FILES);
        db.execSQL(DATABASE_CREATE_CATEGORIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Update tables");
		//if ( oldVersion < 2 ) db.execSQL( "alter table ...." );	
	}

	
	static public int getCategoryID(SQLiteDatabase db, String categoryName){
		String args[] = {categoryName.toLowerCase()};
		
		Cursor stmt = db.rawQuery("select id_cetegory from categories where category = ?", args);
		
		if ( stmt.moveToNext() ) {
			return stmt.getInt(0);
		} else {
			db.execSQL("insert or replace into categories(category) values (?)", args);
			
			String nullArgs[] = {};
			stmt = db.rawQuery("select last_insert_rowid();", nullArgs);
			
			stmt.moveToNext();
			return stmt.getInt(0);
		}
	}
	
	static public void setItem(SQLiteDatabase db, DXPlayerActivity.ItemData data){
		
	}
}
