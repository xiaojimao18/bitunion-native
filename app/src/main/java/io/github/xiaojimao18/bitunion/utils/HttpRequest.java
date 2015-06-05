package io.github.xiaojimao18.bitunion.utils;

import android.graphics.drawable.Drawable;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cowx on 2015/4/2.
 */
public class HttpRequest {
    private static HttpRequest httpRequest;
    private static int timeout = 3000;

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
            Log.d("post", SharedConfig.getInstance().getConfig("nettype") + urlString);
            URL url = new URL(SharedConfig.getInstance().getConfig("nettype") + urlString);
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
                return new JSONObject(response.toString());
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

    public Drawable getURLImage(String imgURL) {

        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpGet httpGet = new HttpGet(imgURL);
            httpGet.setHeader("Referer", SharedConfig.getInstance().getConfig("nettype"));
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                } else {
                    InputStream imgStream = entity.getContent();
                    return Drawable.createFromStream(imgStream, imgURL);
                }
            }
        } catch (Exception e) {
            Log.e("HttpRequest:getURLImage:" + imgURL, e.toString());
        }
        return null;
    }
}
