package com.so.mymusic;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import static com.so.mymusic.utils.PermissionsUtil.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity {

    private EditText et_input_path;
    private SeekBar sb_progress;
    private String mFilePath = "";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isSeek;
    private String TAG = "sorrower";
    private boolean keepTrue = true;
    private int mType = 0;
    private String mRawMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //23+权限获取
        verifyStoragePermissions(this);

        initUI();
        initSeekBar();
    }

    /**
     * 初始化进度条
     */
    private void initSeekBar() {
        //1. 设置进度条监听
        isSeek = false;
        sb_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                isSeek = false;
            }
        });

        //2. 开启线程同步进度条
        new Thread() {
            @Override
            public void run() {
                while (keepTrue) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!isSeek) {
                        sb_progress.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            }
        }.start();
    }

    /**
     * 初始化ui
     */
    private void initUI() {
        et_input_path = (EditText) findViewById(R.id.et_input_path);
        sb_progress = (SeekBar) findViewById(R.id.sb_progress);
    }

    /**
     * 初始化播放器MediaPlayer
     */
    private void initMediaPlayer() {
        //1. 加载选中歌曲
        try {
            //1.1 设置音频文件路径, 设置为循环, 初始化MediaPlayer
            if (mType == 1) {
                mediaPlayer.setDataSource(mFilePath);

            } else {
                Uri uri = Uri.parse("android.resource://com.so.mymusic/" + R.raw.one);
                mediaPlayer.setDataSource(this, uri);
            }

            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择一个文件, 返回其绝对路径
     *
     * @param v 选择按钮
     */
    public void selectMusic(View v) {
        Intent intent = new Intent(this, SelectFileActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        意图
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            //1. 获取选中的mFilePath
            mFilePath = data.getStringExtra("path");
//            Toast.makeText(getApplicationContext(),
//                    mFilePath, Toast.LENGTH_SHORT).show();

            et_input_path.setText(mFilePath);

            //2. 初始化播放器
            mType = 1;
            initMediaPlayer();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param v 使用资源文件中的音乐
     */
    public void addMusic(View v) {
        mRawMusic = "R.raw.one";
        et_input_path.setText(mRawMusic);
        //2. 初始化播放器
        mType = 2;
        initMediaPlayer();
    }

    /**
     * @param v 播放按钮
     */
    public void playMusic(View v) {
        //1. 获取文件的持续时间
        sb_progress.setMax(mediaPlayer.getDuration());

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * @param v 暂停按钮
     */
    public void pauseMusic(View v) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * @param v 停止按钮
     */
    public void stopMusic(View v) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            initMediaPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    /**
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 授予结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //initMediaPlayer();
                    Log.i(TAG, "成功获取权限");
                } else {
                    Toast.makeText(this, "拒绝权限, 将无法使用程序.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }
}
