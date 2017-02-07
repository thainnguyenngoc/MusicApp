package com.example.thain.musicapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;

import com.example.thain.musicapp.Config;
import com.example.thain.musicapp.Constants;
import com.example.thain.musicapp.MusicService;
import com.example.thain.musicapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SettingsActivity extends SimpleActivity {
    @BindView(R.id.settings_dark_theme) SwitchCompat mDarkThemeSwitch;
    @BindView(R.id.settings_shuffle) SwitchCompat mShuffleSwitch;
    @BindView(R.id.settings_numeric_progress) SwitchCompat mNumericProgressSwitch;
    @BindView(R.id.settings_sorting) AppCompatSpinner mSortingSpinner;

    private static Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mConfig = Config.newInstance(getApplicationContext());
        ButterKnife.bind(this);

        setupDarkTheme();
        setupShuffle();
        setupNumericProgress();
        setupSorting();
    }

    private void setupDarkTheme() {
        mDarkThemeSwitch.setChecked(mConfig.getIsDarkTheme());
    }

    private void setupShuffle() {
        mShuffleSwitch.setChecked(mConfig.getIsShuffleEnabled());
    }

    private void setupNumericProgress() {
        mNumericProgressSwitch.setChecked(mConfig.getIsNumericProgressEnabled());
    }

    private void setupSorting() {
        mSortingSpinner.setSelection(mConfig.getSorting());
    }

    @OnClick(R.id.settings_dark_theme_holder)
    public void handleDarkTheme() {
        mDarkThemeSwitch.setChecked(!mDarkThemeSwitch.isChecked());
        mConfig.setIsDarkTheme(mDarkThemeSwitch.isChecked());
        restartActivity();
    }

    @OnClick(R.id.settings_shuffle_holder)
    public void handleShuffle() {
        mShuffleSwitch.setChecked(!mShuffleSwitch.isChecked());
        mConfig.setShuffle(mShuffleSwitch.isChecked());
    }

    @OnClick(R.id.settings_numeric_progress_holder)
    public void handleNumericProgress() {
        mNumericProgressSwitch.setChecked(!mNumericProgressSwitch.isChecked());
        mConfig.setIsNumericProgressEnabled(mNumericProgressSwitch.isChecked());
    }

    @OnItemSelected(R.id.settings_sorting)
    public void handleSorting() {
        mConfig.setSorting(mSortingSpinner.getSelectedItemPosition());
        updatePlaylist();
    }

    private void restartActivity() {
        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(getIntent()).startActivities();
    }


    private void updatePlaylist() {
        final Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(Constants.UPDATE_ACTIVITY, true);
        intent.setAction(Constants.REFRESH_LIST);
        startService(intent);
    }
}
