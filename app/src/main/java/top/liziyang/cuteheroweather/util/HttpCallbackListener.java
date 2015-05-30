package top.liziyang.cuteheroweather.util;

/**
 * Created by stefan on 15/5/29.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
