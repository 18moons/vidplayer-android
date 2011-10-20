package br.tv.dx.android;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;

public class AulasDBHelper extends SQLiteOpenHelper {
	
	static final public String TAG = DXPlayerActivity.TAG;
	
	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aulas";

    private static final String DATABASE_CREATE_XML_FILES =
    	"create table xml_files (" +
		"	id_file integer primary key," +
		"	file_name text," +
		"	checked int " +
		");";
    
    private static final String DATABASE_CREATE_CATEGORIES =
    	"create table categories (" +
		"	id_category integer primary key," +
		"	category text" +
		");";

    private static final String DATABASE_CREATE_ITEMS =
    	"create table items (" +
		"	id_item integer primary key," +
		"   id_file integer," +
		"	title text," +
		"	sub_title text," +
		"	link text," +
		"	video text," +
		"	constraint fk_items_files foreign key (id_file) references xml_files (id_file) on delete cascade on update cascade" +
		");";
    
    private static final String DATABASE_CREATE_TAGS =
    	"create table tags (" +
		"	id_tag integer primary key," +
		"	tag text" +
		");";
    
    private static final String DATABASE_CREATE_ITEMS_TAGS =
    	"create table items_tags (" +
		"	id_tag integer," +
		"	id_item integer," +
		"	constraint pk_items_tags primary key (id_tag,id_item)," +
		"	constraint fk_items_tags_tag foreign key (id_tag) references tags (id_tag) on delete cascade on update cascade," +
		"	constraint fk_items_tags_item foreign key (id_item) references items (id_item) on delete cascade on update cascade" +
		");";
	
	public AulasDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
    	Log.d(TAG, "Create tables");
        db.execSQL(DATABASE_CREATE_XML_FILES);
        db.execSQL(DATABASE_CREATE_CATEGORIES);
        db.execSQL(DATABASE_CREATE_ITEMS);
        db.execSQL(DATABASE_CREATE_TAGS);
        db.execSQL(DATABASE_CREATE_ITEMS_TAGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Update tables");
		//if ( oldVersion < 2 ) db.execSQL( "alter table ...." );	
	}
	
	static private Pair<Integer, Boolean> getUniqueID(SQLiteDatabase db, String arg, String select, String insert){
		String args[] = {arg};
		
		Cursor stmt = db.rawQuery(select, args);
		
		if ( stmt.moveToNext() ) {
			return new Pair<Integer, Boolean>(stmt.getInt(0),false);
		} else {
			db.execSQL(insert, args);
			
			String nullArgs[] = {};
			stmt = db.rawQuery("select last_insert_rowid();", nullArgs);
			
			stmt.moveToNext();
			return new Pair<Integer, Boolean>(stmt.getInt(0),true);
		}
	}
	
	static public Pair<Integer, Boolean> getFileID(SQLiteDatabase db, String fileName){
		Pair<Integer, Boolean> result = getUniqueID(db, fileName, "select id_file from xml_files where file_name = ?", "insert or replace into xml_files(file_name, checked) values (?, 1)");
		
		if (!result.second){
			String args[] = { Integer.toString(result.first) };
			db.execSQL("update xml_files set checked = 1 where id_file = ?", args);
		}
		
		return result;
	}
	
	static public void resetFiles(SQLiteDatabase db){
		db.execSQL("update xml_files set checked = 0");
	}
	
	static public void clearFiles(SQLiteDatabase db){
		db.execSQL("delete from xml_files where checked = 0");
	}
	
	static public int getCategoryID(SQLiteDatabase db, String categoryName){
		return getUniqueID(db, categoryName, "select id_cetegory from categories where category = ?", "insert or replace into categories(category) values (?)").first;
	}
	
	static public void setItem(SQLiteDatabase db, ItemData data){
		
	}
}
