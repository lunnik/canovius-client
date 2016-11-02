package com.lionsquare.canoviusclient.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

/**
 * Created by archivaldo on 21/01/16.
 */
public class SQLite extends SQLiteOpenHelper {

    private static final String TAG = SQLite.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "canovius_cliente";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    // private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASS = "pass";
    private static final String KEY_SESSION = "session";
    private static final String KEY_ID_USER = "idUser";
    private static final String KEY_EMEI = "emei";

    public SQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASS + " TEXT,"
                + KEY_ID_USER + " TEXT,"
                + KEY_EMEI + " TEXT,"
                +  KEY_SESSION+ " BOOLEAN" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        //Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser( String email,  String idUser,String pass ,String emei,Boolean session) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_ID_USER, idUser); // user
        values.put(KEY_EMEI, emei); // user
        values.put(KEY_PASS, pass); // pass
        values.put(KEY_SESSION, session); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        //Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("email", cursor.getString(1));
            user.put("idUser", cursor.getString(2));
            user.put("pass", cursor.getString(3));
            user.put("emei", cursor.getString(4));
            user.put("session", cursor.getString(5));
        }
        cursor.close();
        db.close();
        // return user
        //Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

       // Log.e(TAG, "Deleted all user info from sqlite");
    }
}
