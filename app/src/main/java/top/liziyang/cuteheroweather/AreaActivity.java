package top.liziyang.cuteheroweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                //
            }
        });

        queryProvinces();
    }

    /**
     * 获取Province信息，存储到 listString 里面，优先从数据库获取，如果数据库没有就从网络获取
     */
    private void queryProvinces() {
        listProvinces = weatherDataBase.loadProvinces();
        if (listProvinces.size() > 0) {
            // 数据库中有数据
            for (Province province : listProvinces) {
                listString.add(province.getProvinceName());
                adapter.notifyDataSetChanged();
                listViewArea.setSelection(0);
                textViewArea.setText("中国");

                currentLEVEL = LEVEL_PROVINCE;
            }
        } else {
            queryFromServer(null, "province");
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
                        break;
                    case "county":
                        break;
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                            queryProvinces();
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
