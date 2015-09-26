package com.het.greedimagesearch.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.het.greedimagesearch.models.Filter;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;
    // Database Info
    private static final String DATABASE_NAME = "filterDatabase2";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_FILTER = "filter";

    // Post Table Columns
    private static final String KEY_FILTER_ID = "id";
    private static final String KEY_FILTER_SIZE = "size";
    private static final String KEY_FILTER_COLOR = "color";
    private static final String KEY_FILTER_TYPE = "type";
    private static final String KEY_FILTER_SITE = "site";
    Cursor cursor = null;

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FILTER_TABLE = "CREATE TABLE " + TABLE_FILTER +
                "(" +
                KEY_FILTER_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_FILTER_SIZE + " TEXT," +
                KEY_FILTER_COLOR + " TEXT," +
                KEY_FILTER_TYPE + " TEXT," +
                KEY_FILTER_SITE + " TEXT" +
                ")";

        db.execSQL(CREATE_FILTER_TABLE);

//inserting empty values
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_FILTER_SIZE, "any");
            values.put(KEY_FILTER_COLOR, "any");
            values.put(KEY_FILTER_TYPE, "any");
            values.put(KEY_FILTER_SITE, "");

            db.insertOrThrow(TABLE_FILTER, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("", "Error while trying to add to database");
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Update the filter
    public int updateFilter(Filter filter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILTER_SIZE, filter.size);
        values.put(KEY_FILTER_COLOR, filter.color);
        values.put(KEY_FILTER_TYPE, filter.type);
        values.put(KEY_FILTER_SITE, filter.site);

        // Updating
        return db.update(TABLE_FILTER, values, null, null);
    }

    public Filter getFilter() {

        String SELECT_QUERY = "SELECT * FROM " + TABLE_FILTER;
        Filter filter = new Filter();

        SQLiteDatabase db = getReadableDatabase();

        try {
            cursor = db.rawQuery(SELECT_QUERY, null);
        } catch (Exception e) {
            e.printStackTrace();
            onCreate(db);
            cursor = db.rawQuery(SELECT_QUERY, null);
        }

        try {
            if (cursor.moveToFirst()) {
                filter.size = cursor.getString(cursor.getColumnIndex(KEY_FILTER_SIZE));
                filter.color = cursor.getString(cursor.getColumnIndex(KEY_FILTER_COLOR));
                filter.type = cursor.getString(cursor.getColumnIndex(KEY_FILTER_TYPE));
                filter.site = cursor.getString(cursor.getColumnIndex(KEY_FILTER_SITE));
            }
        } catch (Exception e) {
            Log.d("getAllTodo", "Error while trying to get todo from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return filter;
    }
}

