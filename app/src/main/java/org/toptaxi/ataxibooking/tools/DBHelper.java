package org.toptaxi.ataxibooking.tools;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS RoutePoint");
        onCreate(sqLiteDatabase);
    }
}
