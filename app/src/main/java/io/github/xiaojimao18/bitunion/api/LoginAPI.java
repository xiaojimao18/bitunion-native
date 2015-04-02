package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONObject;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;

/**
 * Created by cowx on 2015/4/2.
 */
public class LoginAPI {
    private static LoginAPI loginAPI;
    private static String url = "http://www.bitunion.org/open_api/bu_logging.php";

    private LoginAPI() {}

    public static synchronized LoginAPI getInstance() {
        if (loginAPI == null) {
            loginAPI = new LoginAPI();
        }
        return loginAPI;
    }

    public JSONObject login(String username, String password) {
        try {
            JSONObject params = new JSONObject();
            params.put("action", "login");
            params.put("username", username);
            params.put("password", password);

            return HttpRequest.getInstance().post(url, params);
        } catch (Exception e) {
            Log.e("LoginAPI:login", e.toString());
            return null;
        }
    }
}
