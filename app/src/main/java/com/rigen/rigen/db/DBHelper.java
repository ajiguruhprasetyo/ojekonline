package com.rigen.rigen.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "dbs.sqlite";
	private static File DATABASE_FILE;

	private boolean mInvalidDatabaseFile = false;
	private boolean mIsUpgraded = false;
	private Context mContext;

	private int mOpenConnections = 0;

	private static DBHelper mInstance;

	synchronized static public DBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DBHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	private DBHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		this.mContext = context;

		SQLiteDatabase db = null;
		try {
			db = getReadableDatabase();
			if (db != null) {
				db.close();
			}

			DATABASE_FILE = context.getDatabasePath(DATABASE_NAME);

			if (mInvalidDatabaseFile) {
				copyDatabase();
			}
			if (mIsUpgraded) {
				doUpgrade();
			}
		} catch (SQLiteException e) {
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mInvalidDatabaseFile = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase database,
						  int old_version, int new_version) {
		mInvalidDatabaseFile = true;
		mIsUpgraded = true;
	}
	private void doUpgrade() {
	}

	@Override
	public synchronized void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		mOpenConnections++;
		if (!db.isReadOnly()) {
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public synchronized void close() {
		mOpenConnections--;
		if (mOpenConnections == 0) {
			super.close();
		}
	}

	private void copyDatabase() {
		AssetManager assetManager = mContext.getResources().getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(DATABASE_NAME);
			out = new FileOutputStream(DATABASE_FILE);
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {}
			}
		}
		setDatabaseVersion();
		mInvalidDatabaseFile = false;
	}

	private void setDatabaseVersion() {
		SQLiteDatabase db = null;
		try {
			db = SQLiteDatabase.openDatabase(DATABASE_FILE.getAbsolutePath(), null,
					SQLiteDatabase.OPEN_READWRITE);
			db.execSQL("PRAGMA user_version = " + VERSION);
		} catch (SQLiteException e ) {
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

}
