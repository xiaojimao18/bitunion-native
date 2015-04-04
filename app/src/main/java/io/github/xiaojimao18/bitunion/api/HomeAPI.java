package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;

/**
 * Created by cowx on 2015/4/3.
 */
public class HomeAPI {
    private static HomeAPI homeAPI;
    private final String url = "http://www.bitunion.org/open_api/bu_home.php";

    private HomeAPI() {}

    public static synchronized HomeAPI getInstance() {
        if (homeAPI == null) {
            homeAPI = new HomeAPI();
        }
        return homeAPI;
    }

    public List<NewThread> home() {
        List<NewThread> result = new ArrayList<>();

        try {
            JSONObject response = HttpRequest.getInstance().post(url, null);
            if (response == null) {
                return null;
            }
            if (response.getString("result").equals("success")) {
                JSONArray threadList = response.getJSONArray("newlist");
                for (int i = 0; i < threadList.length(); i ++) {
                    JSONObject obj = threadList.getJSONObject(i);

                    NewThread thread = new NewThread();
                    thread.pname = obj.getString("pname");
                    thread.author = obj.getString("author");
                    thread.fname = obj.getString("fname");
                    thread.fid = obj.getString("fid");
                    thread.fid_sum = obj.getString("fid_sum");
                    thread.tid = obj.getString("tid");
                    thread.tid_sum = obj.getString("tid_sum");

                    JSONObject last = obj.getJSONArray("lastreply").getJSONObject(0);
                    thread.lastWho = last.getString("who");
                    thread.lastWhen = last.getString("when");
                    thread.lastWhat = last.getString("what");

                    result.add(thread);
                }
            }
        } catch (Exception e) {
            Log.e("HomeAPI:home", e.toString());
        }

        return result;
    }

    public class NewThread {
        public String pname;    // 帖子的标题
        public String author;   // 作者
        public String fname;    // 论坛版块名称
        public String fid;      // 论坛版块的ID
        public String fid_sum;  // 论坛版块的总帖子数
        public String tid;      // 帖子的ID
        public String tid_sum;  // 帖子的回复数
        public String lastWho;  // 最后回复人
        public String lastWhen; // 最后回复时间
        public String lastWhat; // 最后回复内容
    }
}
