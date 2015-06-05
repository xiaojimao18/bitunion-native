package io.github.xiaojimao18.bitunion;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
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

import java.util.ArrayList;
import java.util.List;

import io.github.xiaojimao18.bitunion.api.ForumAPI;
import io.github.xiaojimao18.bitunion.api.LoginAPI;
import io.github.xiaojimao18.bitunion.api.ThreadAPI;


public class ThreadActivity extends ActionBarActivity {
    private List<ForumAPI.Forum> mForumList;
    private ForumAdapter mForumAdapter;
    private List<ThreadAPI.Thread> mThreadList;
    private ThreadAdapter mThreadAdapter;

    private DrawerLayout mDrawerLayout;
    private ListView mThreadListView;
    private ListView mForumListView;
    private SwipeRefreshLayout mSwipeFreshView;

    private ThreadTask mThreadTask;
    private ForumTask mForumTask;

    private String mFid;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        mForumList = new ArrayList<>();
        mForumAdapter = new ForumAdapter();
        mThreadList = new ArrayList<>();
        mThreadAdapter = new ThreadAdapter();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.thread_drawer_layout);
        mThreadListView = (ListView) findViewById(R.id.thread_list);
        mForumListView  = (ListView) findViewById(R.id.forum_list);
        mSwipeFreshView = (SwipeRefreshLayout) findViewById(R.id.thread_swipe_container);

        mThreadListView.setAdapter(mThreadAdapter);
        mForumListView.setAdapter(mForumAdapter);

        mFid = "14";
        mTitle = "灌水乐园";

        mSwipeFreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadTask = new ThreadTask(0, 20, true);
                mThreadTask.execute((Void) null);
            }
        });

        mThreadListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                    int current = mThreadList.size();

                    mThreadTask = new ThreadTask(current, current + 20, false);
                    mThreadTask.execute((Void) null);

                    isLastRow = false;
                }
            }
        });

        mThreadTask = new ThreadTask(0, 20, false);
        mThreadTask.execute((Void) null);

        mForumTask = new ForumTask();
        mForumTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_thread, menu);
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
            Intent intent = new Intent(ThreadActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 列表样式
    public class ForumAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mForumList == null) {
                return 0;
            } else {
                return mForumList.size();
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
            TextView forumView = new TextView(ThreadActivity.this);

            final ForumAPI.Forum forum = mForumList.get(position);

            forumView.setHeight(60);
            if (forum.type.equalsIgnoreCase("group")) {
                forumView.setTextSize(22);
                forumView.setHeight(80);
                forumView.setTextColor(getResources().getColor(R.color.title_color));
                forumView.setBackgroundColor(getResources().getColor(R.color.primary_color));
                forumView.setGravity(Gravity.CENTER);
                forumView.setClickable(false);
            } else if (forum.type.equalsIgnoreCase("forum")) {
                forumView.setTextSize(20);
                forumView.setPadding(10, 0, 0, 0);
            } else if (forum.type.equalsIgnoreCase("sub")) {
                forumView.setTextSize(18);
                forumView.setPadding(40, 0, 0, 0);
            }

            String name = forum.name.replace("<font color=red>", "").replace("</font>", "");
            forumView.setText(name);

            forumView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!forum.type.equals("group")) {
                        mFid = forum.fid;
                        mTitle = forum.name;
                        mThreadTask = new ThreadTask(0, 20, true);
                        mThreadTask.execute();
                        mDrawerLayout.closeDrawers();
                    }
                }
            });

            return forumView;
        }
    }

    public class ThreadAdapter extends BaseAdapter{

        // listView在开始绘制的时候，系统首先调用getCount()函数，
        // 得到listView的长度，然后根据这个长度逐一绘制每一行
        @Override
        public int getCount() {
            if (mThreadList == null) {
                return 0;
            } else {
                return mThreadList.size();
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
            convertView = LayoutInflater.from(ThreadActivity.this).inflate(R.layout.item_thread, null);

            final ThreadAPI.Thread thread = mThreadList.get(position);

            // 头像
            // ImageView avatar = (ImageView) convertView.findViewById(R.id.item_thread_avatar);

            // 用户名
            TextView username = (TextView) convertView.findViewById(R.id.item_thread_username);
            username.setText(thread.author);

            // 时间和日期
            TextView time = (TextView) convertView.findViewById(R.id.item_thread_time);
            time.setText(thread.dateline);

            // 回复数
            TextView replyNum = (TextView) convertView.findViewById(R.id.item_thread_reply_num);
            replyNum.setText(thread.replies);
            //  int num = Integer.parseInt(thread.replies);
            //  if (num >= 100) {
            //      replyNum.setTextColor(android.graphics.Color.RED);
            //  } else if(num >= 10) {
            //      replyNum.setTextColor(android.graphics.Color.BLUE);
            //  }

            // 帖子题目
            TextView title = (TextView) convertView.findViewById(R.id.item_thread_title);
            title.setText(thread.subject);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ThreadActivity.this, PostActivity.class);
                    intent.putExtra("tid", thread.tid);
                    intent.putExtra("title", thread.subject);
                    intent.putExtra("sum", Integer.valueOf(thread.replies) + 1);
                    startActivity(intent);
                }
            });

            return convertView;
        }
    }

    public class ForumTask extends AsyncTask<Void, Void, List<ForumAPI.Forum>> {
        @Override
        protected List<ForumAPI.Forum> doInBackground(Void... params) {
            return ForumAPI.getInstance().forum();
        }

        @Override
        protected void onPostExecute(final List<ForumAPI.Forum> result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else {
                mForumList.addAll(result);
                mForumAdapter.notifyDataSetChanged();
            }
        }
    }

    public class ThreadTask extends AsyncTask<Void, Void, List<ThreadAPI.Thread>> {
        private final int mFrom;
        private final int mTo;
        private final boolean mClearData;

        public ThreadTask(int from, int to, boolean clearData) {
            mFrom = from;
            mTo = to;
            mClearData = clearData;
        }

        @Override
        protected void onPreExecute() {
            mSwipeFreshView.setRefreshing(true);
            setTitle(mTitle);
        }

        @Override
        protected List<ThreadAPI.Thread> doInBackground(Void... params) {

            List<ThreadAPI.Thread> result = ThreadAPI.getInstance().thread(mFid, mFrom, mTo);
            try {
                // 请求成功但是没有数据，可能是session过期，获取新的session
                if (result != null && result.size() == 0) {
                    if (LoginAPI.getInstance().refreshSession()) {
                        result = ThreadAPI.getInstance().thread(mFid, mFrom, mTo);
                    } else {
                        return null;
                    }
                }
            } catch (Exception e) {
                Log.e("ThreadActivity", e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final List<ThreadAPI.Thread> result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else {
                if (mClearData && result.size() > 0) {
                    mThreadList.clear();
                }

                mThreadList.addAll(result);
                mThreadAdapter.notifyDataSetChanged();

                if (mClearData && result.size() > 0) {
                    mThreadListView.setSelectionAfterHeaderView();
                }
            }
            if (mSwipeFreshView.isRefreshing()) {
                mSwipeFreshView.setRefreshing(false);
            }
        }
    }
}
