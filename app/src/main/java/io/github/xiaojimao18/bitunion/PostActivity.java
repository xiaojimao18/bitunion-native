package io.github.xiaojimao18.bitunion;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.xiaojimao18.bitunion.api.PostAPI;
import io.github.xiaojimao18.bitunion.utils.HttpRequest;
import io.github.xiaojimao18.bitunion.utils.SharedConfig;


public class PostActivity extends ActionBarActivity {
    private ListView mPostListView;

    private PostTask mPostTask = null;

    private List<PostAPI.Post> mPostList;
    private PostAdapter mPostAdapter;
    private Map<String, Drawable> imgCache;

    private String mTid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostListView = (ListView) findViewById(R.id.post_list);

        mPostList = new ArrayList<>();
        mPostAdapter = new PostAdapter();
        imgCache = new HashMap<>();

        Intent intent = getIntent();
        mTid = intent.getStringExtra("tid");
        setTitle(intent.getStringExtra("title"));

        mPostListView.setAdapter(mPostAdapter);

        mPostTask = new PostTask(mTid, 0, 20);
        mPostTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class PostAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mPostList == null) {
                return 0;
            } else {
                return mPostList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(PostActivity.this).inflate(R.layout.item_post, null);

            PostAPI.Post post = mPostList.get(position);

            // 头像
            //ImageView avatar = (ImageView) convertView.findViewById(R.id.item_thread_avatar);

            // 用户名
            TextView usernameView = (TextView) convertView.findViewById(R.id.item_post_username);
            String username = post.author;
            String majorName = null;
            if (position == 0) {
                majorName = username;
            }
            if (majorName != null && majorName.equals(username)) {
                username += " <font color='red'>（楼主）</font>";
            }
            usernameView.setText(Html.fromHtml(username));

            // 时间和日期
            TextView timeView = (TextView) convertView.findViewById(R.id.item_post_time);
            timeView.setText(post.dateline);

            // 楼层
            TextView replyNumView = (TextView) convertView.findViewById(R.id.item_post_floor);
            replyNumView.setText("#" + (position + 1));

            // 帖子题目
            // TextView titleView = (TextView) convertView.findViewById(R.id.item_post_title);
            // titleView.setText(post.subject);

            // 帖子内容
            TextView contentView = (TextView) convertView.findViewById(R.id.item_post_content);
            Spanned contentSpanned = Html.fromHtml(post.content, new URLImageParser(contentView, post.content), null);
            contentView.setText(contentSpanned);

            return convertView;
        }
    }

    public class URLImageParser implements Html.ImageGetter {
        private TextView mTextView;
        private String mContent;

        public URLImageParser(TextView textView, String content) {
            mTextView = textView;
            mContent = content;
        }

        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable;

            source = source.replace("../", "http://www.bitunion.org/");
            if (imgCache.containsKey(source)) {
                drawable = imgCache.get(source);
            } else {
                new ImgDownloadTask(mTextView, mContent, source).execute();
                drawable = getResources().getDrawable(R.drawable.default_picture);
            }

            if (drawable != null) {
                DisplayMetrics dm = getResources().getDisplayMetrics();

                float width = drawable.getIntrinsicWidth() * dm.density * 2;
                float height = drawable.getIntrinsicHeight() * dm.density * 2;

                // 如果超出屏幕范围，则按比例缩小
                if (width > dm.widthPixels) {
                    float radio = height / width;
                    width = dm.widthPixels - 10;
                    height = radio * width;
                }

                drawable.setBounds(0, 0, (int)width, (int)height);
            }
            return drawable;
        }
    }

    public class ImgDownloadTask extends AsyncTask<Void, Void, Drawable> {
        private TextView mTextView;
        private String mContent;
        private String mImgURL;

        public ImgDownloadTask(TextView textView, String content, String imgURL) {
            mTextView = textView;
            mContent = content;
            mImgURL = imgURL;
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            return HttpRequest.getInstance().getURLImage(mImgURL);
        }

        @Override
        protected void onPostExecute(final Drawable drawable) {
            if (drawable != null) {
                imgCache.put(mImgURL, drawable);
            }
            mTextView.setText(Html.fromHtml(mContent, new Html.ImageGetter() {
                @Override
                public Drawable getDrawable(String source) {
                    Drawable draw;
                    if (drawable == null) {
                        draw = getResources().getDrawable(R.drawable.default_picture);
                    } else {
                        draw = drawable;
                    }

                    if (draw != null) {
                        DisplayMetrics dm = getResources().getDisplayMetrics();

                        float width = draw.getIntrinsicWidth() * dm.density * 2;
                        float height = draw.getIntrinsicHeight() * dm.density * 2;

                        // 如果超出屏幕范围，则按比例缩小
                        if (width > dm.widthPixels) {
                            float radio = height / width;
                            width = dm.widthPixels - 60;
                            height = radio * width;
                        }

                        draw.setBounds(0, 0, (int)width, (int)height);
                    }
                    return draw;
                }
            }, null));
        }
    }

    public class PostTask extends AsyncTask<Void, Void, List<PostAPI.Post>> {
        private String mUsername;
        private String mSession;
        private String mTid;
        private int mFrom;
        private int mTo;

        public PostTask(String tid, int from, int to) {
            mUsername = SharedConfig.getInstance().getConfig(getApplicationContext(), "username");
            mSession = SharedConfig.getInstance().getConfig(getApplicationContext(), "session");
            mTid = tid;
            mFrom = from;
            mTo = to;
        }

        @Override
        protected List<PostAPI.Post> doInBackground(Void... params) {
            return PostAPI.getInstance().post(mUsername, mSession, mTid, mFrom, mTo);
        }

        @Override
        protected void onPostExecute(final List<PostAPI.Post> result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else {
                mPostList.addAll(result);
                mPostAdapter.notifyDataSetChanged();
            }
        }
    }
}
