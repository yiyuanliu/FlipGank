package com.yiyuanliu.flipgank.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by YiyuanLiu on 2016/12/15.
 */

public class GankDbHelper extends SQLiteOpenHelper {

    public static class Contract {
        public static final String DB_NAME = "gank";

        public static final String TABLE_DATA = "data";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DEST = "desc";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_WHO = "who";
        public static final String COLUMN_DAY = "day";
        public static final String COLUMN_CATEGORY = "category_name";

        public static final String CREATE_TABLE_DATA = "CREATE TABLE " + TABLE_DATA + " ( "
                + COLUMN_ID + " TEXT PRIMARY KEY, "
                + COLUMN_DEST + " TEXT, "
                + COLUMN_URL + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_WHO + " TEXT, "
                + COLUMN_DAY + " TEXT " + " )";

        public static final String TABLE_CATEGORY = "category";
        public static final String CREATE_TABLE_CATEGORY = "CREATE TABLE " + TABLE_CATEGORY + " ( "
                + COLUMN_CATEGORY + " TEXT PRIMARY KEY" + " )";

        public static final String TABLE_LOAD_HISTORY = "load_history";
        public static final String COLUMN_HAS_DATA = "has_data";
        public static final String CREATE_TABLE_HISTORY = "CREATE TABLE " + TABLE_LOAD_HISTORY + " ( "
                + COLUMN_DAY + " TEXT PRIMARY KEY, "
                + COLUMN_HAS_DATA + " INTEGER " + " )";
    }

    public GankDbHelper(Context context) {
        super(context, Contract.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contract.CREATE_TABLE_DATA);
        db.execSQL(Contract.CREATE_TABLE_CATEGORY);
        db.execSQL(Contract.CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
