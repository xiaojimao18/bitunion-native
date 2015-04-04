package io.github.xiaojimao18.bitunion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import io.github.xiaojimao18.bitunion.utils.SharedConfig;


public class IndexActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        String session = SharedConfig.getInstance().getConfig(getApplicationContext(), "session");
        Intent intent = new Intent();
        if (session == null) {
            intent.setClass(IndexActivity.this, LoginActivity.class);
        } else {
            intent.setClass(IndexActivity.this, ThreadActivity.class);
        }
        startActivity(intent);

        finish();
    }
}
