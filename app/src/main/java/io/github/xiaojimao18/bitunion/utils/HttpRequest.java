package io.github.xiaojimao18.bitunion.utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by cowx on 2015/4/2.
 */
public class HttpRequest {
    private static HttpRequest httpRequest;
    private static int timeout = 4000;

    private HttpRequest() {}

    public static synchronized HttpRequest getInstance() {
        if (httpRequest == null) {
            httpRequest = new HttpRequest();
        }
        return httpRequest;
    }

    public JSONObject post(String urlString, JSONObject params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeout);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.connect();

            OutputStream out = conn.getOutputStream();
            out.write(params.toString().getBytes());
            out.flush();

            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return new JSONObject(URLDecoder.decode(response.toString(), "UTF-8"));
            }
        } catch (Exception e) {
            Log.e("HttpRequest:post", e.toString());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }
}
