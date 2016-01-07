package com.aware.plugin.template;

/**
 * Created by Comet on 15/12/15.
 */
import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

public class Provider extends ContentProvider {
    public static final int DATABASE_VERSION = 2;
    /**
     * Provider authority: com.aware.plugin.template.provider.template
     */
    public static String AUTHORITY = "com.aware.plugin.template.provider.template";
    private static final int CHARGING_MONITOR = 1;
    private static final int CHARGING_MONITOR_ID = 2;
    private static final int CHARGING_MONITOR2 = 3;
    private static final int CHARGING_MONITOR2_ID = 4;

    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/template_water.db";

    //table 2 for watermarking data!
    public static final String[] DATABASE_TABLES = {
            "plugin_template", "plugin_template2"
    };
    public static final class Template_Data implements BaseColumns {
        private Template_Data(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_template");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.template";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.template";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";

        //accelerometer
        public static final String Accelerometer_X = "double_accelerometer_x";
        public static final String Accelerometer_Y = "double_accelerometer_y";
        public static final String Accelerometer_Z = "double_accelerometer_z";

        //GPS
        public static final String LATITUDE = "double_Latitude";    //the location’s latitude, in degrees
        public static final String LONGITUDE = "double_Longitude";    //the location’s longitude, in degrees
        public static final String BEARING = "double_Bearing";    //the location’s bearing, in degrees
        public static final String SPEED = "double_Speed";    //speed
        public static final String ALTITUDE = "double_Altitude";    //altitude

        //HR
        public static final String HeartRate = "double_HeartRate";
    }

    public static final class Template_Data2 implements BaseColumns {
        private Template_Data2(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_template2");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.template2";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.template2";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";

        //accelerometer
        public static final String Accelerometer_X = "double_accelerometer_x";
        public static final String Accelerometer_Y = "double_accelerometer_y";
        public static final String Accelerometer_Z = "double_accelerometer_z";
    }

    //alert
    //because SQLite only has real number type, as IEEE 8-byte float, we will try this first. If there is something wrong, we have to change a database to use IEEE 4-byte.
    public static final String[] TABLES_FIELDS = {
            Template_Data._ID + " integer primary key autoincrement," +
                    Template_Data.TIMESTAMP + " real default 0," +
                    Template_Data.DEVICE_ID + " text default ''," +
                    Template_Data.Accelerometer_X + " real default 0," +
                    Template_Data.Accelerometer_Y + " real default 0," +
                    Template_Data.Accelerometer_Z + " real default 0," +
                    Template_Data.LATITUDE + " real default 0," +
                    Template_Data.LONGITUDE + " real default 0," +
                    Template_Data.BEARING + " real default 0," +
                    Template_Data.SPEED + " real default 0," +
                    Template_Data.ALTITUDE + " real default 0," +
                    Template_Data.HeartRate + " real default 0," +
                    "UNIQUE("+ Template_Data.TIMESTAMP+","+ Template_Data.DEVICE_ID+")",

            Template_Data2._ID + " integer primary key autoincrement," +
                    Template_Data2.TIMESTAMP + " real default 0," +
                    Template_Data2.DEVICE_ID + " text default ''," +
                    Template_Data2.Accelerometer_X + " real default 0," +
                    Template_Data2.Accelerometer_Y + " real default 0," +
                    Template_Data2.Accelerometer_Z + " real default 0," +
                    "UNIQUE("+ Template_Data2.TIMESTAMP+","+ Template_Data2.DEVICE_ID+")",
    };

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static HashMap<String, String> databaseMap2;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], CHARGING_MONITOR);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", CHARGING_MONITOR_ID);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], CHARGING_MONITOR2);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", CHARGING_MONITOR2_ID);
        databaseMap = new HashMap<>();
        databaseMap.put(Template_Data._ID, Template_Data._ID);
        databaseMap.put(Template_Data.TIMESTAMP, Template_Data.TIMESTAMP);
        databaseMap.put(Template_Data.DEVICE_ID, Template_Data.DEVICE_ID);
        databaseMap.put(Template_Data.Accelerometer_X, Template_Data.Accelerometer_X);
        databaseMap.put(Template_Data.Accelerometer_Y, Template_Data.Accelerometer_Y);
        databaseMap.put(Template_Data.Accelerometer_Z, Template_Data.Accelerometer_Z);
        databaseMap.put(Template_Data.LATITUDE, Template_Data.LATITUDE);
        databaseMap.put(Template_Data.LONGITUDE, Template_Data.LONGITUDE);
        databaseMap.put(Template_Data.BEARING, Template_Data.BEARING);
        databaseMap.put(Template_Data.SPEED, Template_Data.SPEED);
        databaseMap.put(Template_Data.ALTITUDE, Template_Data.ALTITUDE);
        databaseMap.put(Template_Data.HeartRate, Template_Data.HeartRate);
        databaseMap2 = new HashMap<>();
        databaseMap2.put(Template_Data2._ID, Template_Data2._ID);
        databaseMap2.put(Template_Data2.TIMESTAMP, Template_Data2.TIMESTAMP);
        databaseMap2.put(Template_Data2.DEVICE_ID, Template_Data2.DEVICE_ID);
        databaseMap2.put(Template_Data2.Accelerometer_X, Template_Data2.Accelerometer_X);
        databaseMap2.put(Template_Data2.Accelerometer_Y, Template_Data2.Accelerometer_Y);
        databaseMap2.put(Template_Data2.Accelerometer_Z, Template_Data2.Accelerometer_Z);
        return true;
    }

    private boolean initializeDB() {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case CHARGING_MONITOR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case CHARGING_MONITOR:
                return Template_Data.CONTENT_TYPE;
            case CHARGING_MONITOR_ID:
                return Template_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case CHARGING_MONITOR:
                long weather_id = database.insert(DATABASE_TABLES[0], Template_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Template_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case CHARGING_MONITOR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case CHARGING_MONITOR:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}