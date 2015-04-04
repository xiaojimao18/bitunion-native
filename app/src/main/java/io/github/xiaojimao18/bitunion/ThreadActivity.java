package io.github.xiaojimao18.bitunion;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.xiaojimao18.bitunion.api.LoginAPI;
import io.github.xiaojimao18.bitunion.api.ThreadAPI;
import io.github.xiaojimao18.bitunion.utils.SharedConfig;


public class ThreadActivity extends ActionBarActivity {

    private List<ThreadAPI.Thread> mThreadList;
    private ThreadAdapter mThreadAdapter;

    private ListView mThreadListView;
    private ListView mForumListView;
    private SwipeRefreshLayout mSwipeFreshView;

    private ThreadTask mThreadTask;

    private String mFid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        mThreadList = new ArrayList<>();
        mThreadAdapter = new ThreadAdapter();

        mThreadListView = (ListView) findViewById(R.id.thread_list);
        mForumListView  = (ListView) findViewById(R.id.forum_list);
        mSwipeFreshView = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mThreadListView.setAdapter(mThreadAdapter);
        mThreadListView.setDividerHeight(18);

        mFid = "14";

        mSwipeFreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mThreadTask = new ThreadTask(mFid, 0, 20, true);
                mThreadTask.execute((Void) null);
            }
        });

        mThreadTask = new ThreadTask(mFid, 0, 20, false);
        mThreadTask.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            int num = Integer.parseInt(thread.replies);
            if (num >= 100) {
                replyNum.setTextColor(android.graphics.Color.RED);
            } else if(num >= 10) {
                replyNum.setTextColor(android.graphics.Color.BLUE);
            }

            // 帖子题目
            TextView title = (TextView) convertView.findViewById(R.id.item_thread_title);
            title.setText(thread.subject);

            return convertView;
        }
    }

    public class ThreadTask extends AsyncTask<Void, Void, List<ThreadAPI.Thread>> {
        private final String mUsername;
        private final String mSession;
        private final String mFid;
        private final int mFrom;
        private final int mTo;
        private final boolean mIsSwipe;

        public ThreadTask(String fid, int from, int to, boolean isSwipe) {
            mUsername = SharedConfig.getInstance().getConfig(getApplicationContext(), "username");
            mSession = SharedConfig.getInstance().getConfig(getApplicationContext(), "session");
            mFid = fid;
            mFrom = from;
            mTo = to;
            mIsSwipe = isSwipe;
        }

        @Override
        protected List<ThreadAPI.Thread> doInBackground(Void... params) {
            List<ThreadAPI.Thread> result = ThreadAPI.getInstance().thread(mUsername, mSession, mFid, mFrom, mTo);
            try {
                if (result != null && result.size() == 0) {
                    String username = SharedConfig.getInstance().getConfig(getApplicationContext(), "username");
                    String password = SharedConfig.getInstance().getConfig(getApplicationContext(), "password");

                    // 重新获取sesssion
                    JSONObject obj = LoginAPI.getInstance().login(username, password);
                    if (obj == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                    }
                    String session = obj.getString("session");
                    SharedConfig.getInstance().setConfig(getApplicationContext(), "session", session);

                    // 重新请求数据
                    result = ThreadAPI.getInstance().thread(mUsername, mSession, mFid, mFrom, mTo);
                }
            } catch (Exception e) {
                Log.e("ThreadActivity:doInBackground", e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(final List<ThreadAPI.Thread> result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            } else {
                if (mIsSwipe && result.size() > 0) {
                    mThreadList.clear();
                }
                mThreadList.addAll(result);
                mThreadAdapter.notifyDataSetChanged();
                //Toast.makeText(getApplicationContext(), "更新了"+result.size()+"条数据", Toast.LENGTH_SHORT).show();
            }
            if (mSwipeFreshView.isRefreshing()) {
                mSwipeFreshView.setRefreshing(false);
            }
        }
    }
}
