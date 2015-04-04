package io.github.xiaojimao18.bitunion.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by cowx on 2015/4/3.
 */
public class SharedConfig {
    private static SharedConfig sharedConfig;

    private SharedConfig() {}

    public static SharedConfig getInstance() {
        if (sharedConfig == null) {
            sharedConfig = new SharedConfig();
        }
        return sharedConfig;
    }

    public String getConfig(Context context, String key) {
        SharedPreferences share = context.getSharedPreferences("bitunion", Context.MODE_PRIVATE);
        return share.getString(key, null);
    }

    public void setConfig(Context context, String key, String value) {
        SharedPreferences.Editor share = context.getSharedPreferences("bitunion", Context.MODE_PRIVATE).edit();
        share.putString(key, value);
        share.commit();
    }
}
