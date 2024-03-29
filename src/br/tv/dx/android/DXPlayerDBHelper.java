package br.tv.dx.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
			+ "	checked int);";

	private static final String DATABASE_CREATE_CATEGORIES = "create table categories ("
			+ "	id_category integer primary key, category text, image text, background text);";

	private static final String DATABASE_CREATE_ITEMS = "create table items ("
			+ " id_item integer primary key,"
			+ " id_file integer,"
			+ " id_category integer,"
			+ " title text,"
			+ " sub_title text,"
			+ " teacher text,"
			+ " image text,"
			+ " link text,"
			+ " video text,"
			+ " constraint fk_items_files foreign key (id_file) references xml_files (id_file) on delete cascade on update cascade,"
			+ " constraint fk_items_categories foreign key (id_category) references categories (id_category) on delete cascade on update cascade"
			+ ");";

	private static final String DATABASE_CREATE_ATTACHMENTS = "create table attachments ("
			+ "	id_attachment integer primary key,"
			+ "	id_item integer,"
			+ "	file_name text,"
			+ "	type text,"
			+ "	constraint fk_attachments_items foreign key (id_item) references items (id_item) on delete cascade on update cascade"
			+ ");";

	private static final String DATABASE_CREATE_TAGS = "create table tags ("
			+ "	id_tag integer primary key, tag text);";

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

	// static private String fmtCol(String str) {
	// final int size = 20;
	// 
	// if (str.length() > size) {
	// return str.substring(0, size);
	// } else {
	// return str
	// + new String("                    ").substring(0, size
	// - str.length());
	// }
	// }

	// static private void dumpTable(SQLiteDatabase db, String tableName) {
	// String sql = "select * from \"" + tableName + "\"";
	// Cursor cur = db.rawQuery(sql, new String[0]);
	// int cols = cur.getColumnCount();
	//
	// String str = "";
	//
	// for (int i = 0; i != cols; i++) {
	// str += fmtCol(cur.getColumnName(i));
	// }
	// Log.d(DXPlayerActivity.TAG, "DUMP: " + tableName + ": " + str);
	//
	// while (cur.moveToNext()) {
	// str = "";
	//
	// for (int i = 0; i != cols; i++) {
	// str += fmtCol(cur.getString(i));
	// }
	//
	// Log.d(DXPlayerActivity.TAG, "DUMP: " + tableName + ": " + str);
	// }
	//
	// cur.close();
	// }

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

		// Clear items
		db.execSQL("delete from items where"
				+ " (select xml_files.id_file from xml_files where"
				+ " xml_files.id_file = items.id_file limit 1) is null;");

		// Clear attachments
		db.execSQL("delete from attachments where"
				+ " (select items.id_item from items where"
				+ " items.id_item = attachments.id_item limit 1) is null;");

		// Clear tags
		db.execSQL("delete from items_tags where"
				+ " (select items.id_item from items where"
				+ " items.id_item = items_tags.id_item limit 1) is null;");

		db.execSQL("delete from tags where"
				+ " (select items_tags.id_tag from items_tags where"
				+ " items_tags.id_tag = tags.id_tag limit 1) is null;");

		// Clear categories
		db
				.execSQL("delete from categories where"
						+ " (select items.id_category from items where"
						+ " items.id_category = categories.id_category limit 1) is null;");
	}

	static public CategoryData getCategory(SQLiteDatabase db,
			String categoryName) {

		CategoryData result = new CategoryData();

		String args[] = { categoryName };
		Cursor stmt = db
				.rawQuery(
						"select id_category, image, background from categories where category = ?",
						args);

		if (stmt.moveToNext()) {
			result.id = stmt.getInt(0);
			result.imgButton = stmt.getString(1);
			result.imgBackground = stmt.getString(2);
		} else {
			stmt.close();
			db.execSQL(
					"insert or replace into categories(category) values (?)",
					args);

			String nullArgs[] = {};
			stmt = db.rawQuery("select last_insert_rowid();", nullArgs);

			stmt.moveToNext();
			result.id = stmt.getInt(0);
		}

		stmt.close();

		return result;
	}

	static public CategoryData getCategory(SQLiteDatabase db, int categoryId) {

		CategoryData result = null;

		String args[] = { Integer.toString(categoryId) };
		Cursor stmt = db
				.rawQuery(
						"select category, image, background from categories where id_category = ?",
						args);

		if (stmt.moveToNext()) {
			result = new CategoryData();
			result.id = categoryId;
			result.title = stmt.getString(0);
			result.imgButton = stmt.getString(1);
			result.imgBackground = stmt.getString(2);
		}

		stmt.close();

		return result;
	}

	static public void setCategoryID(SQLiteDatabase db, CategoryData category) {
		String args[] = { category.imgButton, category.imgBackground,
				Integer.toString(category.id) };

		db.execSQL("update categories set image = ?,"
				+ " background = ? where id_category = ?", args);
	}

	static public List<CategoryData> getCategories(SQLiteDatabase db) {
		List<CategoryData> result = new ArrayList<CategoryData>();

		String nullArgs[] = {};
		Cursor stmt = db
				.rawQuery(
						"select id_category, category, image, background,"
								+ " (select count(*) from items where items.id_category = categories.id_category)"
								+ " from categories order by category",
						nullArgs);

		while (stmt.moveToNext()) {
			CategoryData data = new CategoryData();
			data.id = stmt.getInt(0);
			data.title = stmt.getString(1);
			data.imgButton = stmt.getString(2);
			data.imgBackground = stmt.getString(3);
			data.count = stmt.getInt(4);
			result.add(data);
		}

		stmt.close();
		return result;
	}

	// For performance issues, these statements get cached.
	static private SQLiteStatement m_stmtInsertItem = null;

	static private SQLiteStatement m_stmtSelectTag = null;
	static private SQLiteStatement m_stmtInsertTag = null;

	static private SQLiteStatement m_stmtInsertItemTags = null;

	static private SQLiteStatement m_stmtInsertAttachment = null;

	static public void setItem(SQLiteDatabase db, ItemData data) {
		if (m_stmtInsertItem == null) {
			m_stmtInsertItem = db
					.compileStatement("insert into items (id_file, id_category, title, sub_title, teacher, image, link, video) values (?, ?, ?, ?, ?, ?, ?, ?)");

			m_stmtSelectTag = db
					.compileStatement("select id_tag from tags where tag = ?");
			m_stmtInsertTag = db
					.compileStatement("insert or replace into tags(tag) values (?)");

			m_stmtInsertItemTags = db
					.compileStatement("insert into items_tags (id_tag, id_item) values (?, ?)");

			m_stmtInsertAttachment = db
					.compileStatement("insert into attachments (id_item, file_name, type) values (?, ?, ?)");
		}

		m_stmtInsertItem.bindLong(1, data.file);
		m_stmtInsertItem.bindLong(2, data.category);

		if (data.title != null)
			m_stmtInsertItem.bindString(3, data.title);
		else
			m_stmtInsertItem.bindNull(3);

		if (data.subTitle != null)
			m_stmtInsertItem.bindString(4, data.subTitle);
		else
			m_stmtInsertItem.bindNull(4);

		if (data.teacher != null)
			m_stmtInsertItem.bindString(5, data.teacher);
		else
			m_stmtInsertItem.bindNull(5);

		if (data.image != null)
			m_stmtInsertItem.bindString(6, data.image);
		else
			m_stmtInsertItem.bindNull(6);

		if (data.link != null)
			m_stmtInsertItem.bindString(7, data.link);
		else
			m_stmtInsertItem.bindNull(7);

		if (data.video != null)
			m_stmtInsertItem.bindString(8, data.video);
		else
			m_stmtInsertItem.bindNull(8);

		long itemId = m_stmtInsertItem.executeInsert();

		for (String tag : data.tags) {
			try {
				long tagId;

				m_stmtSelectTag.bindString(1, tag);
				try {
					tagId = m_stmtSelectTag.simpleQueryForLong();
				} catch (SQLiteDoneException e) {
					m_stmtInsertTag.bindString(1, tag);
					tagId = m_stmtInsertTag.executeInsert();
				}

				m_stmtInsertItemTags.bindLong(1, tagId);
				m_stmtInsertItemTags.bindLong(2, itemId);
				m_stmtInsertItemTags.execute();
			} catch (SQLiteException e) {
				// This usually means more than 1 tag in the same
				// item
				Log.e(DXPlayerActivity.TAG, "Failed inserting tag: '" + tag
						+ "'", e);
			}
		}

		for (Attachment attach : data.attachments) {
			try {
				m_stmtInsertAttachment.bindLong(1, itemId);
				m_stmtInsertAttachment.bindString(2, attach.file);
				m_stmtInsertAttachment.bindString(3, attach.type);
				m_stmtInsertAttachment.execute();
			} catch (SQLiteException e) {
				Log.e(DXPlayerActivity.TAG, "Failed inserting attachment: '"
						+ attach + "'", e);
			}
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
						"select id_item, id_file, title, sub_title, teacher, image, link, video from items where id_category = ? order by id_item",
						args);

		while (stmt.moveToNext()) {
			ItemData data = new ItemData();
			data.id = stmt.getInt(0);
			data.file = stmt.getInt(1);
			data.category = categoryId;
			data.title = stmt.getString(2);
			data.subTitle = stmt.getString(3);
			data.teacher = stmt.getString(4);
			data.image = stmt.getString(5);
			data.link = stmt.getString(6);
			data.video = stmt.getString(7);

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
						"select id_item, id_file, id_category, title, sub_title, teacher, image, link, video from items where id_item = ?",
						args);

		if (stmt.moveToNext()) {
			result.id = stmt.getInt(0);
			result.file = stmt.getInt(1);
			result.category = stmt.getInt(2);
			result.title = stmt.getString(3);
			result.subTitle = stmt.getString(4);
			result.teacher = stmt.getString(5);
			result.image = stmt.getString(6);
			result.link = stmt.getString(7);
			result.video = stmt.getString(8);

			addItemData(db, result);
		}

		stmt.close();
		return result;
	}
}
