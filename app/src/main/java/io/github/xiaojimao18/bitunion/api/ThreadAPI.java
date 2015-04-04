package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;

/**
 * Created by cowx on 2015/4/3.
 */
public class ThreadAPI {
    private static ThreadAPI threadAPI;
    private final String url = "http://www.bitunion.org/open_api/bu_thread.php";

    private ThreadAPI() {}

    public static synchronized ThreadAPI getInstance() {
        if (threadAPI == null) {
            threadAPI = new ThreadAPI();
        }
        return threadAPI;
    }

    public List<Thread> thread(String username, String session, String fid, int from, int to) {
        List<Thread> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        try {
            for (int start = from; start < to; start += 20) {
                int end = start + 20 < to ? start + 20 : to;
                JSONObject params = new JSONObject();
                params.put("action", "thread");
                params.put("username", URLEncoder.encode(username, "UTF-8"));
                params.put("session", session);
                params.put("fid", fid);
                params.put("from", String.valueOf(start));
                params.put("to", String.valueOf(end));

                JSONObject response = HttpRequest.getInstance().post(url, params);
                if (response == null) {
                    return null;
                }else if (response.getString("result").equals("success")) {
                    JSONArray threadList = response.getJSONArray("threadlist");
                    for (int i = 0; i < threadList.length(); i ++) {
                        JSONObject obj = threadList.getJSONObject(i);

                        Thread thread = new Thread();
                        thread.tid = obj.getString("tid");
                        thread.author = URLDecoder.decode(obj.getString("author"), "UTF-8");
                        thread.authorid = obj.getString("authorid");
                        thread.subject = URLDecoder.decode(obj.getString("subject"), "UTF-8");
                        thread.dateline = dateFormat.format( new Date(Long.valueOf(obj.getString("dateline")) * 1000) );
                        thread.lastpost = dateFormat.format( new Date(Long.valueOf(obj.getString("lastpost")) * 1000) );
                        thread.lastposter = URLDecoder.decode(obj.getString("lastposter"), "UTF-8");
                        thread.views = obj.getString("views");
                        thread.replies = obj.getString("replies");
                        result.add(thread);
                    }

                    if (threadList.length() < 20) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ThreadAPI:thread", e.toString());
        }
        return result;
    }

    public class Thread {
        public String tid;
        public String author;
        public String authorid;
        public String subject;
        public String dateline;
        public String lastpost;
        public String lastposter;
        public String views;
        public String replies;
    }
}
