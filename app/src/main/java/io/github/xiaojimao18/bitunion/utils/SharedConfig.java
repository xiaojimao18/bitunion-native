package io.github.xiaojimao18.bitunion.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import io.github.xiaojimao18.bitunion.GlobalApplication;

/**
 * Created by cowx on 2015/4/3.
 */
public class SharedConfig extends Application{
    private static SharedConfig sharedConfig;
    private static SharedPreferences sharedPref;

    private SharedConfig() {}

    public static SharedConfig getInstance() {
        if (sharedConfig == null) {
            sharedConfig = new SharedConfig();
            sharedPref = PreferenceManager.getDefaultSharedPreferences(GlobalApplication.getAppContext());
        }
        return sharedConfig;
    }

    public String getConfig(String key) {
        return sharedPref.getString(key, null);
    }

    public void setConfig(String key, String value) {
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(key, value);
        edit.commit();
    }
}
