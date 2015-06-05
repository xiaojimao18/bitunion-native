package io.github.xiaojimao18.bitunion;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.xiaojimao18.bitunion.api.LoginAPI;
import io.github.xiaojimao18.bitunion.api.PostAPI;
import io.github.xiaojimao18.bitunion.compenont.URLDrawable;
import io.github.xiaojimao18.bitunion.utils.HttpRequest;


public class PostActivity extends ActionBarActivity {
    private ListView mPostListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private PostTask mPostTask = null;

    private List<PostAPI.Post> mPostList;
    private PostAdapter mPostAdapter;
    private Map<String, SoftReference<Drawable>> mImgCache;

    private String mTid;
    private String mMajorName;
    private int mSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mPostListView = (ListView) findViewById(R.id.post_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.post_swipe_container);
        mSwipeRefreshLayout.setEnabled(false);

        mPostList = new ArrayList<>();
        mPostAdapter = new PostAdapter();
        mImgCache = new HashMap<>();

        Intent intent = getIntent();
        mTid = intent.getStringExtra("tid");
        mSum = intent.getIntExtra("sum", 0);
        setTitle(intent.getStringExtra("title"));

        mPostListView.setAdapter(mPostAdapter);

        mPostListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean isLastRow = false;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount == totalItemCount) {
                    isLastRow = true;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    int current = mPostList.size();
                    if (current < mSum) {
                        mPostTask = new PostTask(mTid, current, current + 20);
                        mPostTask.execute();
                    }
                    isLastRow = false;
                }
            }
        });

        mPostTask = new PostTask(mTid, 0, 20);
        mPostTask.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(PostActivity.this, SettingsActivity.class);
            startActivity(intent);
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
            // ImageView avatarView = (ImageView) convertView.findViewById(R.id.item_post_avatar);
            // if (post.avatar != null) {
            //    ImageLoader.getInstance().displayImage(post.avatar, avatarView);
            // }

            // 用户名
            TextView usernameView = (TextView) convertView.findViewById(R.id.item_post_username);
            String username = post.author;
            if (position == 0) {
                mMajorName = username;
            }
            if (mMajorName != null && mMajorName.equals(username)) {
                username += " [楼主]";
            }
            usernameView.setText(username);

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
            Spanned contentSpanned = Html.fromHtml(post.content, new URLImageParser(contentView), null);
            contentView.setText(contentSpanned);

            return convertView;
        }
    }

    public class URLImageParser implements Html.ImageGetter {
        private TextView mTextView;

        public URLImageParser(TextView textView) {
            mTextView = textView;
        }

        @Override
        public Drawable getDrawable(String source) {
            URLDrawable urlDrawable = new URLDrawable();

            source = source.replace("../", "http://www.bitunion.org/");
            if (mImgCache.containsKey(source)) {
                urlDrawable.drawable = mImgCache.get(source).get();

                DisplayMetrics dm = getResources().getDisplayMetrics();

                float width = urlDrawable.drawable.getIntrinsicWidth() * dm.density * 2;
                float height = urlDrawable.drawable.getIntrinsicHeight() * dm.density * 2;

                // 如果超出屏幕范围，则按比例缩小
                if (width > dm.widthPixels) {
                    float radio = height / width;
                    width = dm.widthPixels - 50;
                    height = radio * width;
                }

                urlDrawable.drawable.setBounds(0, 0, (int)width, (int)height);
                urlDrawable.setBounds(0, 0, (int)width, (int)height);
            } else {
                new ImgDownloadTask(urlDrawable).execute(source);
            }

            return urlDrawable;
        }

        public class ImgDownloadTask extends AsyncTask<String, Void, Drawable> {
            private URLDrawable mURLDrawable;

            public ImgDownloadTask(URLDrawable urlDrawable) {
                mURLDrawable = urlDrawable;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String url = params[0];
                Drawable drawable = HttpRequest.getInstance().getURLImage(url);
                if (drawable != null) {
                    mImgCache.put(url, new SoftReference<>(drawable));
                }
                return drawable;
            }

            @Override
            protected void onPostExecute(final Drawable drawable) {
                if (drawable != null) {
                    DisplayMetrics dm = getResources().getDisplayMetrics();

                    float width = drawable.getIntrinsicWidth() * dm.density * 2;
                    float height = drawable.getIntrinsicHeight() * dm.density * 2;

                    // 如果超出屏幕范围，则按比例缩小
                    if (width > dm.widthPixels) {
                        float radio = height / width;
                        width = dm.widthPixels - 50;
                        height = radio * width;
                    }

                    mURLDrawable.drawable = drawable;
                    mURLDrawable.drawable.setBounds(0, 0, (int)width, (int)height);
                    mURLDrawable.setBounds(0, 0, (int)width, (int)height);

                    URLImageParser.this.mTextView.invalidate();
                    URLImageParser.this.mTextView.setHeight((URLImageParser.this.mTextView.getHeight() + (int)height));
                    URLImageParser.this.mTextView.setEllipsize(null);
                }
            }
        }
    }

    public class PostTask extends AsyncTask<Void, Void, List<PostAPI.Post>> {
        private String mTid;
        private int mFrom;
        private int mTo;

        public PostTask(String tid, int from, int to) {
            mTid = tid;
            mFrom = from;
            mTo = to;
        }

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected List<PostAPI.Post> doInBackground(Void... params) {
            List<PostAPI.Post> result = PostAPI.getInstance().post(mTid, mFrom, mTo);
            try {
                // 请求成功但是没有数据，可能是session过期，获取新的session
                if (result != null && result.size() == 0) {
                    if (LoginAPI.getInstance().refreshSession()) {
                        result = PostAPI.getInstance().post(mTid, mFrom, mTo);
                    } else {
                        return null;
                    }
                }
            } catch (Exception e) {
                Log.e("PostActivity", e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final List<PostAPI.Post> result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else {
                mPostList.addAll(result);
                mPostAdapter.notifyDataSetChanged();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(false);
        }
    }
}
