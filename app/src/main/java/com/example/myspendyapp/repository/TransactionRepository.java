package com.example.myspendyapp.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myspendyapp.database.DatabaseHelper;

public class TransactionRepository {

    private DatabaseHelper dbHelper;

    public TransactionRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Thêm khoản thu/chi
    public boolean addTransaction(int categoryId, double amount, String date, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("category_id", categoryId);
        values.put("amount", amount);
        values.put("created_date", date);
        values.put("note", note);

        long res = db.insert(DatabaseHelper.TABLE_TRANSACTION, null, values);

        // Tăng số lượng trong Category
        db.execSQL("UPDATE categories SET count = count + 1 WHERE id = " + categoryId);

        db.close();
        return res != -1;
    }

    public Cursor getTransactionsByCategory(int categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM transactions WHERE category_id = ?",
                new String[]{String.valueOf(categoryId)}
        );
    }
}

