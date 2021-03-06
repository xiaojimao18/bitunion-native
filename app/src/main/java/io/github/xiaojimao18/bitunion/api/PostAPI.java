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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.xiaojimao18.bitunion.utils.HttpRequest;
import io.github.xiaojimao18.bitunion.utils.SharedConfig;

/**
 * Created by cowx on 2015/4/4.
 */
public class PostAPI {
    private static PostAPI postAPI;
    private final String url = "open_api/bu_post.php";

    private PostAPI() {}

    public static synchronized PostAPI getInstance() {
        if (postAPI == null) {
            postAPI = new PostAPI();
        }
        return postAPI;
    }

    public List<Post> post(String tid, int from, int to) {
        List<Post> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        try {
            String username = SharedConfig.getInstance().getConfig("username");
            String session = SharedConfig.getInstance().getConfig("session");

            for (int start = from; start < to; start += 20) {
                int end = start + 20 < to ? start + 20 : to;
                JSONObject params = new JSONObject();
                params.put("action", "post");
                params.put("username", URLEncoder.encode(username, "UTF-8"));
                params.put("session", session);
                params.put("tid", tid);
                params.put("from", String.valueOf(start));
                params.put("to", String.valueOf(end));

                JSONObject response = HttpRequest.getInstance().post(SharedConfig.getInstance().getConfig("nettype") + url, params);
                if (response == null) {
                    return null;
                } else if (response.getString("result").equals("success")) {
                    JSONArray postList = response.getJSONArray("postlist");
                    for (int i = 0; i < postList.length(); i ++) {
                        JSONObject obj = postList.getJSONObject(i);

                        Post post = new Post();
                        post.fid = obj.getString("fid");
                        post.tid = obj.getString("tid");
                        post.pid = obj.getString("pid");
                        post.authorid = obj.getString("authorid");
                        post.usesig = obj.getString("usesig");
                        post.dateline = dateFormat.format( new Date(Long.valueOf(obj.getString("dateline")) * 1000) );
                        post.avatar = URLDecoder.decode(obj.getString("avatar"), "UTF-8");
                        post.author = URLDecoder.decode(obj.getString("author"), "UTF-8");
                        post.subject = URLDecoder.decode(obj.getString("subject"), "UTF-8");
                        post.message = URLDecoder.decode(obj.getString("message"), "UTF-8");
                        post.attachment = URLDecoder.decode(obj.getString("attachment"), "UTF-8");

                        post.avatar = parseAvatar(post.avatar);
                        post.content = "<h5>" + post.subject + "</h5>";
                        post.content += parseQuotes(post.message);
                        if (!post.attachment.equals("null")) {
                            post.content += "<br><img src=\"http://www.bitunion.org/" + post.attachment + "\">";
                        }

                        result.add(post);
                    }

                    if (postList.length() < 20) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PostAPI:post", e.toString());
        }
        return result;
    }

    private String parseQuotes(String html) {
        String regex = "<table.*?<table.*?<b>(.*?)</b>(.*?)<br />([\\s\\S]*?)</td></tr></table>.*?</table>";
        Pattern pattern = Pattern.compile(regex);
        try {
            Matcher m = pattern.matcher(html);
            while (m.find()) {
                String name = m.group(1);
                String time = m.group(2);
                String content = m.group(3).trim();

                StringBuilder sb = new StringBuilder();
                sb.append("<blockquote><font color=\"#4b9ce0\">@");
                sb.append(name + " : " + content);
                sb.append("</font></blockquote>");
                html = html.replace(m.group(), sb.toString());

                m = pattern.matcher(html);
            }
        } catch (Exception e) {
            Log.e("PostAPI:htmlParser", e.toString());
        }
        return html;
    }

    private String parseAvatar(String str) {
        String regex = "src=\"(.*?)\"";
        Pattern pattern = Pattern.compile(regex);
        try {
            Matcher m = pattern.matcher(str);
            if (m.find()) {
                return "http://www.bitunion.org/" + m.group(1);
            }
        } catch (Exception e) {
            Log.e("PostAPI:parseAvatar", e.toString());
        }

        return null;
    }

    public class Post {
        public String fid;
        public String tid;
        public String pid;
        public String author;
        public String authorid;
        public String avatar;
        public String dateline;
        public String subject;
        public String message;
        public String usesig;
        public String attachment;
        public String content;
    }
}
