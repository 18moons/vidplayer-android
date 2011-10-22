package br.tv.dx.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;
import br.tv.dx.android.ItemData.Attachment;

public class DXPlayerDBHelper extends SQLiteOpenHelper {

	static final public String TAG = DXPlayerActivity.TAG;

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "aulas";

	private static final String DATABASE_CREATE_XML_FILES = "create table xml_files ("
			+ "	id_file integer primary key,"
			+ "	file_name text,"
			+ "	checked int " + ");";

	private static final String DATABASE_CREATE_CATEGORIES = "create table categories ("
			+ "	id_category integer primary key," + "	category text" + ");";

	private static final String DATABASE_CREATE_ITEMS = "create table items ("
			+ "	id_item integer primary key,"
			+ "   id_file integer,"
			+ "	id_category integer,"
			+ "	title text,"
			+ "	sub_title text,"
			+ "	link text,"
			+ "	video text,"
			+ "	constraint fk_items_files foreign key (id_file) references xml_files (id_file) on delete cascade on update cascade,"
			+ "	constraint fk_items_categories foreign key (id_category) references categories (id_category) on delete cascade on update cascade"
			+ ");";

	private static final String DATABASE_CREATE_ATTACHMENTS = "create table attachments ("
			+ "	id_attachment integer primary key,"
			+ "	id_item integer,"
			+ "	file_name text,"
			+ "	type text,"
			+ "	constraint fk_attachments_items foreign key (id_item) references items (id_item) on delete cascade on update cascade"
			+ ");";

	private static final String DATABASE_CREATE_TAGS = "create table tags ("
			+ "	id_tag integer primary key," + "	tag text" + ");";

	private static final String DATABASE_CREATE_ITEMS_TAGS = "create table items_tags ("
			+ "	id_tag integer,"
			+ "	id_item integer,"
			+ "	constraint pk_items_tags primary key (id_tag,id_item),"
			+ "	constraint fk_items_tags_tag foreign key (id_tag) references tags (id_tag) on delete cascade on update cascade,"
			+ "	constraint fk_items_tags_item foreign key (id_item) references items (id_item) on delete cascade on update cascade"
			+ ");";

	public DXPlayerDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Create tables");
		db.execSQL(DATABASE_CREATE_XML_FILES);
		db.execSQL(DATABASE_CREATE_CATEGORIES);
		db.execSQL(DATABASE_CREATE_ITEMS);
		db.execSQL(DATABASE_CREATE_ATTACHMENTS);
		db.execSQL(DATABASE_CREATE_TAGS);
		db.execSQL(DATABASE_CREATE_ITEMS_TAGS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Update tables");
		// if ( oldVersion < 2 ) db.execSQL( "alter table ...." );
	}

	static private Pair<Integer, Boolean> getUniqueID(SQLiteDatabase db,
			String arg, String select, String insert) {
		String args[] = { arg };

		Cursor stmt = db.rawQuery(select, args);

		Pair<Integer, Boolean> result;

		if (stmt.moveToNext()) {
			result = new Pair<Integer, Boolean>(stmt.getInt(0), false);
		} else {
			stmt.close();
			db.execSQL(insert, args);

			String nullArgs[] = {};
			stmt = db.rawQuery("select last_insert_rowid();", nullArgs);

			stmt.moveToNext();
			result = new Pair<Integer, Boolean>(stmt.getInt(0), true);
		}

		stmt.close();
		return result;
	}

	static public Pair<Integer, Boolean> getFileID(SQLiteDatabase db,
			String fileName) {
		Pair<Integer, Boolean> result = getUniqueID(db, fileName,
				"select id_file from xml_files where file_name = ?",
				"insert or replace into xml_files(file_name, checked) values (?, 0)");

		if (!result.second) {
			String args[] = { Integer.toString(result.first) };
			db.execSQL("update xml_files set checked = 1 where id_file = ?",
					args);
		}

		return result;
	}

	static public void resetFiles(SQLiteDatabase db) {
		db.execSQL("update xml_files set checked = 0");
	}

	static public void removeFile(SQLiteDatabase db, int fileId) {
		String args[] = { Integer.toString(fileId) };
		db.execSQL("delete from xml_files where id_file = ?", args);
	}

	static public void setFileAsFinished(SQLiteDatabase db, int fileId) {
		String args[] = { Integer.toString(fileId) };
		db.execSQL("update xml_files set checked = 1 where id_file = ?", args);
	}

	static public void removeIncompleteFiles(SQLiteDatabase db) {
		db.execSQL("delete from xml_files where checked = 0");
	}

	static public void cleanUpDb(SQLiteDatabase db) {
		db.execSQL("delete from xml_files where checked = 0");
		// TODO clear tags
		// TODO clear categories
	}

	static public int getCategoryID(SQLiteDatabase db, String categoryName) {
		return getUniqueID(db, categoryName,
				"select id_category from categories where category = ?",
				"insert or replace into categories(category) values (?)").first;
	}

	static public List<CategoryData> getCategories(SQLiteDatabase db) {
		List<CategoryData> result = new ArrayList<CategoryData>();

		String nullArgs[] = {};
		Cursor stmt = db
				.rawQuery(
						"select id_category, category from categories order by category",
						nullArgs);

		while (stmt.moveToNext()) {
			CategoryData data = new CategoryData();
			data.id = stmt.getInt(0);
			data.title = stmt.getString(1);
			result.add(data);
		}

		stmt.close();
		return result;
	}

	static public void setItem(SQLiteDatabase db, ItemData data) {
		String itemArgs[] = { Integer.toString(data.file),
				Integer.toString(data.category), data.title, data.subTitle,
				data.link, data.video };
		db
				.execSQL(
						"insert into items (id_file, id_category, title, sub_title, link, video) values (?, ?, ?, ?, ?, ?)",
						itemArgs);

		String nullArgs[] = {};
		Cursor stmt = db.rawQuery("select last_insert_rowid();", nullArgs);
		stmt.moveToNext();
		int itemId = stmt.getInt(0);
		stmt.close();
		stmt = null;

		for (String tag : data.tags) {
			int tagId = getUniqueID(db, tag,
					"select id_tag from tags where tag = ?",
					"insert or replace into tags(tag) values (?)").first;

			String itemTagsArgs[] = { Integer.toString(tagId),
					Integer.toString(itemId) };
			db.execSQL(
					"insert into items_tags (id_tag, id_item) values (?, ?)",
					itemTagsArgs);
		}

		for (Attachment attach : data.attachments) {
			String attachArgs[] = { Integer.toString(itemId), attach.file,
					attach.type };
			db
					.execSQL(
							"insert into attachments (id_item, file_name, type) values (?, ?, ?)",
							attachArgs);
		}
	}

	static private void addItemData(SQLiteDatabase db, ItemData item) {
		String itemId[] = { Integer.toString(item.id) };

		Cursor stmtAttach = db.rawQuery(
				"select file_name, type from attachments where id_item = ?",
				itemId);
		while (stmtAttach.moveToNext()) {
			Attachment attach = new Attachment();
			attach.file = stmtAttach.getString(0);
			attach.type = stmtAttach.getString(1);

			// For now, only the first PDF is considered
			if (attach.type == null
					|| attach.type.compareToIgnoreCase("pdf") == 0) {
				item.attachments.add(attach);
				break;
			}
		}
		stmtAttach.close();
		stmtAttach = null;

		Cursor stmtTags = db
				.rawQuery(
						"select tags.tag from tags join items_tags on tags.id_tag == items_tags.id_tag where items_tags.id_item = ?",
						itemId);
		while (stmtTags.moveToNext()) {
			item.tags.add(stmtTags.getString(0));
		}
		stmtTags.close();
		stmtTags = null;
	}

	static public List<ItemData> getItems(SQLiteDatabase db, int categoryId) {
		List<ItemData> result = new ArrayList<ItemData>();

		String args[] = { Integer.toString(categoryId) };
		Cursor stmt = db
				.rawQuery(
						"select id_item, id_file, title, sub_title, link, video from items where id_category = ? order by id_item",
						args);

		while (stmt.moveToNext()) {
			ItemData data = new ItemData();
			data.id = stmt.getInt(0);
			data.file = stmt.getInt(1);
			data.category = categoryId;
			data.title = stmt.getString(2);
			data.subTitle = stmt.getString(3);
			data.link = stmt.getString(4);
			data.video = stmt.getString(5);

			// For now, this data is not used anywhere so don't bother loading
			// addItemData(db, data);

			result.add(data);
		}

		stmt.close();
		return result;
	}

	static public ItemData getItem(SQLiteDatabase db, int itemId) {
		ItemData result = new ItemData();

		String args[] = { Integer.toString(itemId) };
		Cursor stmt = db
				.rawQuery(
						"select id_item, id_file, id_category, title, sub_title, link, video from items where id_item = ?",
						args);

		if (stmt.moveToNext()) {
			result.id = stmt.getInt(0);
			result.file = stmt.getInt(1);
			result.category = stmt.getInt(2);
			result.title = stmt.getString(3);
			result.subTitle = stmt.getString(4);
			result.link = stmt.getString(5);
			result.video = stmt.getString(6);

			addItemData(db, result);
		}

		stmt.close();
		return result;
	}
}
