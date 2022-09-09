package com.snhu.vincentdelvecchio_final_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabase extends SQLiteOpenHelper {

    // Final Variables for database information
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "userDetails.db";

    // Static instance for Singleton pattern
    private static UserDatabase mUserDb;

    // Create and return instance for singleton pattern
    public static UserDatabase getInstance(Context context) {
        if(mUserDb == null) {
            mUserDb = new UserDatabase(context);
        }

        return  mUserDb;
    }

    // Default constructor
    public UserDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Variables for table and column names
    private static final String TABLE = "userDetails";
    private static final String COL_USERNAME = "userName";
    private static final String COL_PASSWORD = "password";

    @Override
    // Set Up database with table and column name variables
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + UserDatabase.TABLE + " (" +
                UserDatabase.COL_USERNAME + " text primary key, " +
                UserDatabase.COL_PASSWORD + " text)");
    }

    @Override
    // Drop Table on upgrade
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + UserDatabase.TABLE);
        onCreate(sqLiteDatabase);
    }

    // Add a user to the database with the given username and password, username must be unique being the primary key
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserDatabase.COL_USERNAME, username);
        values.put(UserDatabase.COL_PASSWORD, password);

        long result = db.insert(UserDatabase.TABLE, null, values);
        return result != -1;
    }

    // Return a cursor containing the information of the database query on given username
    public Cursor getUser(String username) {
        SQLiteDatabase db = getReadableDatabase();

        String sql = "select * from " + UserDatabase.TABLE + " where " + UserDatabase.COL_USERNAME + " = ?";
        return db.rawQuery(sql, new String[] {username});
    }
}
