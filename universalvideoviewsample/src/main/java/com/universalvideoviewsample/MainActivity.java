/*
* Copyright (C) 2015 Andy Ke <dictfb@gmail.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.universalvideoviewsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

import static com.universalvideoviewsample.Constant.BASE_API_URL;
import static com.universalvideoviewsample.Constant.PATH_PLAY_LIST;

public class MainActivity extends AppCompatActivity implements UniversalVideoView.VideoViewCallback{
    private static final String KEY_PLAY_LIST = "play_list";
    private static final String TAG = "MainActivity";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";
    private static final String VIDEO_URL = "http://cyson.drinkdevice.com/App.Data/FixedFile/bdf45bab_7bc9_4d83_8061_0230ac193469/MachineVideo/BLVideo01.mp4";
    int i = 0;
    UniversalVideoView mVideoView;
    UniversalMediaController mMediaController;
    HttpProxyCacheServer proxy;
    View mVideoLayout;
    private int mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoLayout = findViewById(R.id.video_layout);
        //mBottomLayout = findViewById(R.id.bottom_layout);
        mVideoView = (UniversalVideoView) findViewById(R.id.videoView);
        mMediaController = (UniversalMediaController) findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
        proxy = new HttpProxyCacheServer(this);
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onCompletion ");
            }
        });
        mVideoView.setFullscreen(true);
        this.setFinishOnTouchOutside(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            mVideoView.pause();
        }
    }

    /**
     * 置视频区域大小
     */
    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
            @Override
            public void run() {
                int width = mVideoLayout.getWidth();
                cachedHeight = (int) (width * 405f / 720f);
                ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
                videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                videoLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mVideoLayout.setLayoutParams(videoLayoutParams);
                getUrl();
                mVideoView.requestFocus();
            }
        });
    }
    private void getUrl() {

        final String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpUtils.get(BASE_API_URL + PATH_PLAY_LIST + androidId, new HttpUtils.ResponseCallback() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(MainActivity.this, "请检查网络连接是否正常", Toast.LENGTH_SHORT).show();
                //Uri uri = Uri.parse(getCacheDir().toString());
                //Log.e("111111111111",uri.toString());
            }

            @Override
            public void onSuccess(int responseCode, String response) {
                Log.d(TAG, response);
                PlayListResp playListResp = new Gson().fromJson(response, PlayListResp.class);
                if (playListResp != null && playListResp.isSuccess()) {
                    List<PlayListResp.DataBean> dataBeen = playListResp.getData();
                    if (dataBeen != null && !dataBeen.isEmpty()) {
                        Toast.makeText(MainActivity.this, androidId, Toast.LENGTH_SHORT).show();
                        String proxyUrl = proxy.getProxyUrl(dataBeen.get(i).getUri());
                        Toast.makeText(MainActivity.this, proxyUrl, Toast.LENGTH_SHORT).show();
                        mVideoView.setVideoPath(proxyUrl);
                        i++;
                        if(i == dataBeen.size()){
                            i = 0;
                        }
                        getPreferences(Context.MODE_PRIVATE).edit().putString(KEY_PLAY_LIST, response).commit();
                    } else {
                        Toast.makeText(MainActivity.this, "获取播放列表失败", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Log.d(TAG, playListResp.getMessage());
                    Toast.makeText(MainActivity.this, playListResp.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(String contentType, InputStream inputStream) {

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
        Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
    }


    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
            //mBottomLayout.setVisibility(View.GONE);

        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
           // mBottomLayout.setVisibility(View.VISIBLE);
        }

        switchTitleBar(!isFullscreen);
    }

    private void switchTitleBar(boolean show) {
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPause UniversalVideoView callback");
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingEnd UniversalVideoView callback");
    }

    @Override
    public void onRestart(MediaPlayer mediaPlayer) {
        setVideoAreaSize();
    }

    @Override
    public void onBackPressed() {
        if (this.isFullscreen) {
            mVideoView.setFullscreen(false);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.ACTION_DOWN == 0){
            //finish();
        }

        return super.dispatchTouchEvent(ev);
    }

}
