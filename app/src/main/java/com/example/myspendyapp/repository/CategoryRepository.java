package com.example.myspendyapp.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myspendyapp.database.DatabaseHelper;

public class CategoryRepository {

    private DatabaseHelper dbHelper;

    public CategoryRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Thêm danh mục mới
    public boolean addCategory(String name, String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("type", type);
        values.put("count", 0);

        long res = db.insert(DatabaseHelper.TABLE_CATEGORY, null, values);
        db.close();
        return res != -1;
    }

    // Lấy tất cả danh mục
    public Cursor getAllCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM categories", null);
    }

    // Tăng số lượng khoản thu/chi
    public void increaseCount(int categoryId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE categories SET count = count + 1 WHERE id = " + categoryId);
        db.close();
    }
}
