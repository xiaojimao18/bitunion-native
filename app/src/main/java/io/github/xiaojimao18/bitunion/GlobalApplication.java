package io.github.xiaojimao18.bitunion;

import android.app.Application;
import android.content.Context;

/**
 * Created by cowx on 2015/6/5.
 */
public class GlobalApplication extends Application {
    private static Context context;

    public void onCreate(){
        super.onCreate();
        GlobalApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return GlobalApplication.context;
    }
}
