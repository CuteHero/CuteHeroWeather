package top.liziyang.cuteheroweather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import top.liziyang.cuteheroweather.model.City;
import top.liziyang.cuteheroweather.model.County;
import top.liziyang.cuteheroweather.model.Province;

/**
 * Created by stefan on 15/5/27.
 */
public class WeatherDateBase {

    public static final String DB_NAME = "cutehero_weather";
    public static final int DB_VERSION = 1;

    private SQLiteDatabase sqLiteDatabase;
    private static WeatherDateBase weatherDateBase;

    private WeatherDateBase(Context context) {

        WeatherSQLiteOpenHelper weatherSQLiteOpenHelper = new WeatherSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION);
        sqLiteDatabase = weatherSQLiteOpenHelper.getWritableDatabase();
    }

    public synchronized static WeatherDateBase getInstance(Context context) {
        if (weatherDateBase == null) {
            weatherDateBase = new WeatherDateBase(context);
        }

        return weatherDateBase;
    }

    /**
     * Save Province information to sqLite
     *
     * @param province the object of Province class
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            contentValues.put("province_code", province.getProvinceCode());
            sqLiteDatabase.insert("Province", null, contentValues);
        }
    }

    /**
     * Load all Province information from sqLite
     *
     * @return ArrayList of Province
     */
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<Province>();
        Cursor cursor = sqLiteDatabase.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getColumnIndex("id"));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }

        return list;
    }

    public void saveCity(City city) {
        if (city != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("city_code", city.getCityCode());
            contentValues.put("province_id", city.getProvinceId());
            sqLiteDatabase.insert("City", null, contentValues);
        }
    }

    public List<City> loadCities() {
        List<City> list = new ArrayList<City>();
        Cursor cursor = sqLiteDatabase.query("City", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getColumnIndex("id"));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
                list.add(city);
            } while (cursor.moveToNext());
        }

        return list;
    }

    public void saveCounty(County county) {
        if (county != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("county_name", county.getCountyName());
            contentValues.put("county_code", county.getCountyCode());
            contentValues.put("city_id", county.getCityId());
            sqLiteDatabase.insert("County", null, contentValues);
        }
    }

    public List<County> loadCounties() {
        List<County> list = new ArrayList<County>();
        Cursor cursor = sqLiteDatabase.query("County", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setId(cursor.getColumnIndex("id"));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
                list.add(county);
            } while (cursor.moveToNext());
        }

        return list;
    }
}
