package edu.buffalo.cse.cse486586.simpledht;

import android.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

public class DB_Helper extends SQLiteOpenHelper {

    
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "DHTDB.sqlite";
    private static final String TABLE_NAME = "DHT";
    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + "("+ "'key'"+ " TEXT PRIMARY KEY,"+ "'value'"+ "TEXT);";
    Context context;
    
   
    public DB_Helper(Context context) {
	
        super(context,DB_NAME , null, DATABASE_VERSION);
        this.context = context;
        // TODO Auto-generated constructor stub
        
    }
    
    public static String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	// TODO Auto-generated method stub
        db.execSQL(TABLE_CREATE);
        Log.v("DB", "table creation called");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
