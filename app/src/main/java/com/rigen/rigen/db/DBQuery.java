package com.rigen.rigen.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;

public class DBQuery {
	DBHelper dbHelper;
	SQLiteDatabase db;
	private final Context context;

	public DBQuery(Context context) {
		this.context = context;
	}

	public DBQuery open() throws SQLException {
		dbHelper = DBHelper.getInstance(context);
		db = dbHelper.getWritableDatabase();
		return this;
	}
	public void close() {
		dbHelper.close();
	}
	public Cursor getAll(){
		return db.query("account", new String[]{
				"*"
		}, null, null, null, null, null,null);
	}
	public void clear(){
		db.execSQL("DELETE FROM account");
	}
	public void insert(EntityAccount e){
		db.execSQL("INSERT INTO account(password,nama,no_telp,alamat,email) values('"+e.getPassword()+"'," +
				"'"+e.getNama()+"','"+e.getNo_telp()+"','"+e.getAlamat()+"','"+e.getEmail()+"')");
	}
}
