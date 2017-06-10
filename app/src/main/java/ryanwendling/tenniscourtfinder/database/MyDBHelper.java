package ryanwendling.tenniscourtfinder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ryanwendling.tenniscourtfinder.markLatLng;

/**
 * Created by wendlir on 5/23/17.
 */
public class MyDBHelper extends SQLiteOpenHelper {

    String TAG = "DbHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "markLatLngDB.db";
    private static final String TABLE_MARKLATLNG = "markLatLng";

    public static final String COLUMN_LAT = "_Lat";
    public static final String COLUMN_LNG = "_Lng";



    public MyDBHelper(Context context, String name,
                      SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MARKLATLNG_TABLE = "CREATE TABLE " +
                TABLE_MARKLATLNG + "("
                + COLUMN_LAT + " DOUBLE," + COLUMN_LNG + " DOUBLE" + ")";
        db.execSQL(CREATE_MARKLATLNG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKLATLNG);
        onCreate(db);
    }

    public void addLatLng(markLatLng aLatLng) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_LAT, aLatLng.getLat());
        values.put(COLUMN_LNG, aLatLng.getLng());

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_MARKLATLNG, null, values);
        db.close();
    }

    public String findLatLng() {

        Log.d(TAG, "getTableAsString called");
        SQLiteDatabase db = this.getWritableDatabase();

        String tableString = String.format("Table %s:\n", TABLE_MARKLATLNG);
        Cursor allRows  = db.rawQuery("SELECT * FROM " + TABLE_MARKLATLNG, null);
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }
        db.close();
        return tableString;
    }

    public void deleteAll() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MARKLATLNG, null, null);
    }
}


