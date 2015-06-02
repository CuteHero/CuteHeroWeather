package top.liziyang.cuteheroweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import top.liziyang.cuteheroweather.database.WeatherDataBase;
import top.liziyang.cuteheroweather.model.City;
import top.liziyang.cuteheroweather.model.County;
import top.liziyang.cuteheroweather.model.Province;

/**
 * Created by stefan on 15/5/29.
 */
public class Utility {

    /**
     * 处理http请求的之后得到的数据，存储到数据库
     *
     * @param weatherDataBase 数据库操作类
     * @param response        http请求返回的数据
     * @return 操作是否成功
     */
    public synchronized static boolean HandleProvinceResponse(WeatherDataBase weatherDataBase, String response) {
        if (response != null && !TextUtils.isEmpty(response)) {
            String[] arrayProvinces = response.split(",");
            if (arrayProvinces != null && arrayProvinces.length > 0) {
                for (String provinceInfo : arrayProvinces) {
                    String[] arrayProvinceInfo = provinceInfo.split("\\|");
                    if (arrayProvinceInfo != null && arrayProvinceInfo.length > 0) {
                        Province province = new Province();
                        province.setProvinceCode(arrayProvinceInfo[0]);
                        province.setProvinceName(arrayProvinceInfo[1]);
                        weatherDataBase.saveProvince(province);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 处理http请求的之后得到的数据，存储到数据库
     *
     * @param weatherDataBase 数据库操作类
     * @param response        http请求返回的数据
     * @param provinceId      与该City关联的Province
     * @return 操作是否成功
     */
    public synchronized static boolean HandleCityResponse(WeatherDataBase weatherDataBase, String response, int provinceId) {
        if (response != null && !TextUtils.isEmpty(response)) {
            String[] arrayCities = response.split(",");
            if (arrayCities != null && arrayCities.length > 0) {
                for (String cityInfo : arrayCities) {
                    String[] arrayCityInfo = cityInfo.split("\\|");
                    if (arrayCityInfo != null && arrayCityInfo.length > 0) {
                        City city = new City();
                        city.setCityCode(arrayCityInfo[0]);
                        city.setCityName(arrayCityInfo[1]);
                        city.setProvinceId(provinceId);
                        weatherDataBase.saveCity(city);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 处理http请求的之后得到的数据，存储到数据库
     *
     * @param weatherDataBase 数据库操作类
     * @param response        http请求返回的数据
     * @param cityId          与该County关联的City
     * @return 操作是否成功
     */
    public synchronized static boolean HandleCountyResponse(WeatherDataBase weatherDataBase, String response, int cityId) {
        if (response != null && !TextUtils.isEmpty(response)) {
            String[] arrayCounties = response.split(",");
            if (arrayCounties != null && arrayCounties.length > 0) {
                for (String countyInfo : arrayCounties) {
                    String[] arrayCountyInfo = countyInfo.split("\\|");
                    if (arrayCountyInfo != null && arrayCountyInfo.length > 0) {
                        County county = new County();
                        county.setCountyCode(arrayCountyInfo[0]);
                        county.setCountyName(arrayCountyInfo[1]);
                        county.setCityId(cityId);
                        weatherDataBase.saveCounty(county);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static void handleWeatherXmlResponse(Context context, String response, String countyCode) {
        String cityName = "";
        String cityId = countyCode;
        String updateTime = "";
        String currentTemperature = "";
        String fengli = "";
        String fengxiang = "";
        String shidu = "";
        String sunrise = "";
        String sunset = "";

        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(new StringReader(response));
            int eventType = xmlPullParser.getEventType();


            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        switch (nodeName) {
                            case "city":
                                cityName = xmlPullParser.nextText();
                                break;
                            case "updatetime":
                                updateTime = xmlPullParser.nextText();
                                break;
                            case "wendu":
                                currentTemperature = xmlPullParser.nextText();
                                break;
                            case "fengli":
                                fengli = xmlPullParser.nextText();
                                break;
                            case "shidu":
                                shidu = xmlPullParser.nextText();
                                break;
                            case "fengxiang":
                                fengxiang = xmlPullParser.nextText();
                                break;
                            case "sunrise_1":
                                sunrise = xmlPullParser.nextText();
                                break;
                            case "sunset_1":
                                sunset = xmlPullParser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        saveWeatherInfo(context, cityName, cityId, updateTime, currentTemperature, fengli, fengxiang, shidu, sunrise, sunset);
    }

    public static void saveWeatherInfo(Context context, String cityName, String cityId, String updateTime, String currentTemperature, String fengli, String fengxiang, String shidu, String sunrise, String sunset) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true);
        editor.putString("city_name", cityName);
        editor.putString("city_id", cityId);
        editor.putString("current_temperature", currentTemperature);
        editor.putString("update_time", updateTime);
        editor.putString("feng_li", fengli);
        editor.putString("feng_xiang", fengxiang);
        editor.putString("shi_du", shidu);
        editor.putString("sun_rise", sunrise);
        editor.putString("sun_set", sunset);
        editor.putString("current_date", simpleDateFormat.format(new Date()));
        editor.commit();
    }
}
