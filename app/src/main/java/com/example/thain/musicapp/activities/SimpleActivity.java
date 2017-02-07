package com.example.thain.musicapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.thain.musicapp.Config;
import com.example.thain.musicapp.R;

public class SimpleActivity extends AppCompatActivity {
    protected Config mConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mConfig = Config.newInstance(getApplicationContext());
        setTheme(mConfig.getIsDarkTheme() ? R.style.AppTheme_Dark : R.style.AppTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
