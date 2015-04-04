package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONObject;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;

/**
 * Created by cowx on 2015/4/2.
 */
public class LoginAPI {
    private static LoginAPI loginAPI;
    private final String url = "http://www.bitunion.org/open_api/bu_logging.php";

    private LoginAPI() {}

    public static synchronized LoginAPI getInstance() {
        if (loginAPI == null) {
            loginAPI = new LoginAPI();
        }
        return loginAPI;
    }

    public String login(String username, String password) {
        try {
            JSONObject params = new JSONObject();
            params.put("action", "login");
            params.put("username", username);
            params.put("password", password);

            JSONObject obj = HttpRequest.getInstance().post(url, params);
            if (obj == null) {
                return null;
            } else if (obj.getString("result").equals("success")) {
                return obj.getString("session");
            } else {
                return "";
            }
        } catch (Exception e) {
            Log.e("LoginAPI:login", e.toString());
            return null;
        }
    }
}
