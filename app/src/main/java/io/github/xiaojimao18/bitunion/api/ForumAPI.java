package io.github.xiaojimao18.bitunion.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;

/**
 * Created by cowx on 2015/4/5.
 */
public class ForumAPI {
    private static ForumAPI forumAPI;
    private final String url = "open_api/bu_forum.php";

    private ForumAPI() {}

    public static synchronized ForumAPI getInstance() {
        if (forumAPI == null) {
            forumAPI = new ForumAPI();
        }
        return forumAPI;
    }

    public List<Forum> forum(String username, String session) {
        List<Forum> result = new ArrayList<>();
        try {
            JSONObject params = new JSONObject();
            params.put("action", "forum");
            params.put("username", URLEncoder.encode(username, "UTF-8"));
            params.put("session", URLEncoder.encode(session, "UTF-8"));

            JSONObject response = HttpRequest.getInstance().post(url, params);
            if (response == null) {
                return null;
            } else if (response.getString("result").equals("success")) {
                JSONObject forumList = response.getJSONObject("forumslist");

                //13:苦中作乐区;129:直通理工区;166:时尚生活区;16:技术讨论区;2:系统管理区
                String[] forumIds = {"13", "129", "166", "16", "2"};
                for (String forumId : forumIds) {
                    JSONObject forumObj = forumList.getJSONObject(forumId);

                    // 首先获得论坛组名
                    JSONObject group = forumObj.getJSONObject("main");
                    Forum forum = new Forum();
                    forum.name = URLDecoder.decode(group.getString("name"), "UTF-8");
                    forum.fid = group.getString("fid");
                    forum.type = group.getString("type");
                    result.add(forum);

                    //每个论坛和论坛信息
                    for (int id = 0; forumObj.has(String.valueOf(id)); id ++) {
                        JSONObject part = forumObj.getJSONObject(String.valueOf(id));

                        JSONObject main = part.getJSONArray("main").getJSONObject(0);
                        forum = new Forum();
                        forum.name = URLDecoder.decode(main.getString("name"), "UTF-8");
                        forum.fid = main.getString("fid");
                        forum.type = main.getString("type");
                        result.add(forum);

                        if (part.has("sub")) {
                            JSONArray subList = part.getJSONArray("sub");
                            for (int j = 0; j < subList.length(); j ++) {
                                JSONObject sub = subList.getJSONObject(j);
                                forum = new Forum();
                                forum.name = URLDecoder.decode(sub.getString("name"), "UTF-8");
                                forum.fid = sub.getString("fid");
                                forum.type = sub.getString("type");
                                result.add(forum);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ForumAPI:forum", e.toString());
        }

        return result;
    }

    public class Forum {
        public String name;
        public String fid;
        public String type;
    }
}
