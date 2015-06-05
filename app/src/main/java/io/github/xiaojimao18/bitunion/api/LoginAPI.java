package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;
import io.github.xiaojimao18.bitunion.utils.SharedConfig;

/**
 * Created by cowx on 2015/4/2.
 */
public class LoginAPI {
    private static LoginAPI loginAPI;
    private final String url = "open_api/bu_logging.php";

    private LoginAPI() {}

    public static synchronized LoginAPI getInstance() {
        if (loginAPI == null) {
            loginAPI = new LoginAPI();
        }
        return loginAPI;
    }

    public Boolean login(String username, String password) {
        try {
            JSONObject params = new JSONObject();
            params.put("action", "login");
            params.put("username", URLEncoder.encode(username));
            params.put("password", URLEncoder.encode(password));

            JSONObject obj = HttpRequest.getInstance().post(SharedConfig.getInstance().getConfig("nettype") + url, params);
            if (obj == null) {
                return null;
            } else if (obj.getString("result").equals("success")) {
                SharedConfig.getInstance().setConfig("username", username);
                SharedConfig.getInstance().setConfig("password", password);
                SharedConfig.getInstance().setConfig("session", obj.getString("session"));

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.e("LoginAPI:login", e.toString());
            return null;
        }
    }

    public Boolean refreshSession() {
        String username = SharedConfig.getInstance().getConfig("username");
        String password = SharedConfig.getInstance().getConfig("password");

        if (username != null && password != null) {
            return login(username, password);
        } else {
            return false;
        }
    }
}
