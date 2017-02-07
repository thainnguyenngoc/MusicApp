package com.example.thain.musicapp.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.thain.musicapp.BusProvider;
import com.example.thain.musicapp.Config;
import com.example.thain.musicapp.Constants;
import com.example.thain.musicapp.Events;
import com.example.thain.musicapp.MusicService;
import com.example.thain.musicapp.R;
import com.example.thain.musicapp.Song;
import com.example.thain.musicapp.SongAdapter;
import com.example.thain.musicapp.Utils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends SimpleActivity
        implements AdapterView.OnItemClickListener,
        MediaScannerConnection.OnScanCompletedListener, SeekBar.OnSeekBarChangeListener {
    private static final int STORAGE_PERMISSION = 1;

    @BindView(R.id.playPauseBtn) ImageView mPlayPauseBtn;
    @BindView(R.id.songs) ListView mSongsList;
    @BindView(R.id.songTitle) TextView mTitleTV;
    @BindView(R.id.songArtist) TextView mArtistTV;
    @BindView(R.id.progressbar) SeekBar mProgressBar;
    @BindView(R.id.song_progress) TextView mProgress;
    @BindView(R.id.previousBtn) ImageView mPreviousBtn;
    @BindView(R.id.nextBtn) ImageView mNextBtn;

    private static Bus mBus;
    private static Song mCurrentSong;
    private static List<Song> mSongs;

    private static List<String> mToBeDeleted;
    private static Bitmap mPlayBitmap;
    private static Bitmap mPauseBitmap;

    private static boolean mIsNumericProgressShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mBus = BusProvider.getInstance();
        mBus.register(this);
        mProgressBar.setOnSeekBarChangeListener(this);
        mConfig = Config.newInstance(getApplicationContext());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsNumericProgressShown = Config.newInstance(getApplicationContext()).getIsNumericProgressEnabled();
        setupIconColors();
        if (mIsNumericProgressShown) {
            mProgress.setVisibility(View.VISIBLE);
        } else {
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.enable_song_repetition).setVisible(!mConfig.getRepeatSong());
        menu.findItem(R.id.disable_song_repetition).setVisible(mConfig.getRepeatSong());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.enable_song_repetition:
                toggleSongRepetition(true);
                return true;
            case R.id.disable_song_repetition:
                toggleSongRepetition(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializePlayer();
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_permissions), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleSongRepetition(boolean enable) {
        mConfig.setRepeatSong(enable);
        invalidateOptionsMenu();
    }

    private void initializePlayer() {
        mToBeDeleted = new ArrayList<>();
        mSongsList.setOnItemClickListener(this);
        Utils.sendIntent(this, Constants.INIT);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    private void setupIconColors() {
        final Resources res = getResources();
        final int color = mTitleTV.getCurrentTextColor();
        mPreviousBtn.setImageBitmap(Utils.getColoredIcon(res, color, R.mipmap.previous));
        mNextBtn.setImageBitmap(Utils.getColoredIcon(res, color, R.mipmap.next));
        mPlayBitmap = Utils.getColoredIcon(res, color, R.mipmap.play);
        mPauseBitmap = Utils.getColoredIcon(res, color, R.mipmap.pause);
    }

    private void songPicked(int pos) {
        final Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(Constants.SONG_POS, pos);
        intent.setAction(Constants.PLAYPOS);
        startService(intent);
    }

    private void updateSongInfo(Song song) {
        if (song != null) {
            mTitleTV.setText(song.getTitle());
            mArtistTV.setText(song.getArtist());
            mProgressBar.setMax(song.getDuration());
            mProgressBar.setProgress(0);
        } else {
            mTitleTV.setText("");
            mArtistTV.setText("");
        }
    }

    private void fillSongsListView(ArrayList<Song> songs) {
        mSongs = songs;
        final SongAdapter adapter = new SongAdapter(this, songs);
        mSongsList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.newInstance(getApplicationContext()).setIsFirstRun(false);
        mBus.unregister(this);
    }

    @OnClick(R.id.previousBtn)
    public void previousClicked() {
        Utils.sendIntent(this, Constants.PREVIOUS);
    }

    @OnClick(R.id.playPauseBtn)
    public void playPauseClicked() {
        Utils.sendIntent(this, Constants.PLAYPAUSE);
    }

    @OnClick(R.id.nextBtn)
    public void nextClicked() {
        Utils.sendIntent(this, Constants.NEXT);
    }

    @Subscribe
    public void songChangedEvent(Events.SongChanged event) {
        mCurrentSong = event.getSong();
        updateSongInfo(mCurrentSong);
    }

    @Subscribe
    public void songStateChanged(Events.SongStateChanged event) {
        if (event.getIsPlaying()) {
            mPlayPauseBtn.setImageBitmap(mPauseBitmap);
        } else {
            mPlayPauseBtn.setImageBitmap(mPlayBitmap);
        }
    }

    @Subscribe
    public void playlistUpdated(Events.PlaylistUpdated event) {
        fillSongsListView(event.getSongs());
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        songPicked(position);
    }



    @Override
    public void onScanCompleted(String path, Uri uri) {
        Utils.sendIntent(this, Constants.REFRESH_LIST);
    }

    @Subscribe
    public void songChangedEvent(Events.ProgressUpdated event) {
        mProgressBar.setProgress(event.getProgress());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mIsNumericProgressShown) {
            final String duration = Utils.getTimeString(mProgressBar.getMax());
            final String formattedProgress = Utils.getTimeString(progress);

            final String progressText = String.format(getResources().getString(R.string.progress), formattedProgress, duration);
            mProgress.setText(progressText);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(Constants.PROGRESS, seekBar.getProgress());
        intent.setAction(Constants.SET_PROGRESS);
        startService(intent);
    }
}
