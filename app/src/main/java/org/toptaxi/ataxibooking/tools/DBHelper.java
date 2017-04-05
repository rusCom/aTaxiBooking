package org.toptaxi.ataxibooking.tools;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.data.Constants;

public class DBHelper extends SQLiteOpenHelper {
    protected static String TAG = "#########" + DBHelper.class.getName();

    public DBHelper() {
        super(MainApplication.getInstance().getApplicationContext(), MainApplication.getInstance().getApplicationContext().getPackageName(), null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL = "create table RoutePoint(" +
                "Id text primary key," +
                "Name text,"+
                "Address text,"+
                "Latitude float,"+
                "Longitude float,"+
                "PlaceType integer,"+
                "count integer," +
                "rating integer"+
                ")";
        sqLiteDatabase.execSQL(SQL);
        Log.d(TAG, "create table RoutePoint");
        SQL = "create table RoutePointNote(" +
                "Id text primary key," +
                "Note text"+
                ")";
        sqLiteDatabase.execSQL(SQL);
        Log.d(TAG, "create table RoutePointNote");

        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putFloat("lastFastRoutePointLatitude", (float) 0);
        editor.putFloat("lastFastRoutePointLongitude", (float) 0);
        editor.apply();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS RoutePoint");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS RoutePointNote");
        onCreate(sqLiteDatabase);
    }
}
