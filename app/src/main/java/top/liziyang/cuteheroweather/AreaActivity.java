package top.liziyang.cuteheroweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import top.liziyang.cuteheroweather.database.WeatherDataBase;
import top.liziyang.cuteheroweather.model.City;
import top.liziyang.cuteheroweather.model.County;
import top.liziyang.cuteheroweather.model.Province;
import top.liziyang.cuteheroweather.util.HttpCallbackListener;
import top.liziyang.cuteheroweather.util.HttpUtil;
import top.liziyang.cuteheroweather.util.Utility;


public class AreaActivity extends Activity {

    public static final String TAG = "Stefan";

    private List<Province> listProvinces;
    private List<City> listCities;
    private List<County> listCounties;

    private TextView textViewArea;
    private ListView listViewArea;

    private ArrayAdapter<String> adapter;
    private List<String> listString;

    private WeatherDataBase weatherDataBase;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY     = 1;
    public static final int LEVEL_COUNTY   = 2;
    /**
     * 标识当前listView显示的什么列表，Province、City or County
     */
    private int currentLEVEL;
    private Province selectedProvince;
    private City selectedCity;
    //private County selectedCounty;

    private ProgressDialog progressDialog;

    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFromWeatherActivity = getIntent().getBooleanExtra("is_from_weather_activity", false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AreaActivity.this);
        if (sharedPreferences.getBoolean("city_selected", false) && (!isFromWeatherActivity)) {
            // 若存在本地天气，直接显示
            Intent intent = new Intent(AreaActivity.this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        setContentView(R.layout.area);

        textViewArea = (TextView) findViewById(R.id.textview_area);
        listViewArea = (ListView) findViewById(R.id.listview_area);

        listString = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(AreaActivity.this, android.R.layout.simple_list_item_1, listString);
        listViewArea.setAdapter(adapter);

        weatherDataBase = WeatherDataBase.getInstance(AreaActivity.this);

        listViewArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLEVEL == LEVEL_PROVINCE) {
                    // province --> city
                    selectedProvince = listProvinces.get(position);
                    Log.d(TAG, "position : " + position + "  , id : " + id + ",     provinceId : " + selectedProvince.getId());
                    queryCities();
                } else if (currentLEVEL == LEVEL_CITY) {
                    // city --> county
                    selectedCity = listCities.get(position);
                    queryCounties();
                } else if (currentLEVEL == LEVEL_COUNTY) {
                    // county --> display
                    String countyCode = listCounties.get(position).getCountyCode();
                    Intent intent = new Intent(AreaActivity.this, WeatherActivity.class);
                    intent.putExtra("county_code", countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        queryProvinces();
    }

    /**
     * 获取Province信息，存储到 listString，优先从数据库获取，如果数据库没有就从网络获取
     */
    private void queryProvinces() {
        listProvinces = weatherDataBase.loadProvinces();
        if (listProvinces.size() > 0) {
            // 数据库中有数据
            for (Province province : listProvinces) {
                listString.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listViewArea.setSelection(0);
            textViewArea.setText("中国");

            currentLEVEL = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }

    /**
     * 查询City信息，存储到 listString
     */
    private void queryCities() {
        // 优先查询数据库
        Log.d(TAG, "sel province id : " + selectedProvince.getId());
        listCities = weatherDataBase.loadCities(selectedProvince.getId());
        if (listCities.size() > 0) {
            listString.clear();
            for (City city : listCities) {
                listString.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listViewArea.setSelection(0);
            textViewArea.setText(selectedProvince.getProvinceName());

            currentLEVEL = LEVEL_CITY;
        } else {
            // 数据库没有，从网络获取
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }
    }

    private void queryCounties() {
        // 优先查询数据库
        listCounties = weatherDataBase.loadCounties(selectedCity.getId());
        if (listCounties.size() > 0) {
            listString.clear();
            for (County county : listCounties) {
                listString.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listViewArea.setSelection(0);
            textViewArea.setText(selectedCity.getCityName());

            currentLEVEL = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 从服务器获取数据
     * @param queryCode 根据code来查询，code为空时查询Province
     * @param queryType
     */
    private void queryFromServer(String queryCode, final String queryType) {
        String address;
        if (!TextUtils.isEmpty(queryCode)) {
            address = "http://www.weather.com.cn/data/list3/city" + queryCode + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showLoadingDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                // 请求数据完成，解析
                boolean result = false;
                switch (queryType) {
                    case "province":
                        result = Utility.HandleProvinceResponse(weatherDataBase, response);
                        break;
                    case "city":
                        result = Utility.HandleCityResponse(weatherDataBase, response, selectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.HandleCountyResponse(weatherDataBase, response, selectedCity.getId());
                        break;
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                            switch (queryType) {
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

    }

    /**
     * 加载的时候显示ProcessDialog
     */
    private void showLoadingDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(AreaActivity.this);
            progressDialog.setMessage("正在加载城市信息......");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 加载完了，不显示ProcessDialog
     */
    private void hideLoadingDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
