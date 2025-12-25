package com.example.myspendyapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "myspendy.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USER = "users";
    public static final String TABLE_CATEGORY = "categories";
    public static final String TABLE_TRANSACTION = "transactions";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Bảng User
        db.execSQL("CREATE TABLE " + TABLE_USER + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE, "
                + "phone TEXT, "
                + "email TEXT, "
                + "password TEXT)");

        // Bảng Danh mục
        db.execSQL("CREATE TABLE " + TABLE_CATEGORY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "type TEXT, "
                + "count INTEGER DEFAULT 0)");

        // Bảng Khoản thu/chi
        db.execSQL("CREATE TABLE " + TABLE_TRANSACTION + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "category_id INTEGER, "
                + "amount REAL, "
                + "created_date TEXT, "
                + "note TEXT, "
                + "FOREIGN KEY(category_id) REFERENCES categories(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}
