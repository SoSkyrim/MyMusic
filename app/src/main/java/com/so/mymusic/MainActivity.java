package com.so.mymusic;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import static com.so.mymusic.utils.PermissionsUtil.verifyStoragePermissions;

public class MainActivity extends AppCompatActivity {

    private EditText et_input_path;
    private String mFilePath = "";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isSeek;
    private SeekBar sb_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //23+权限获取
        verifyStoragePermissions(this);

        et_input_path = (EditText) findViewById(R.id.et_input_path);


    }

    /**
     * 初始化播放器MediaPlayer
     */
    private void initMediaPlayer() {
        try {
            //设置音频文件路径, 设置为循环, 初始化MediaPlayer
            mediaPlayer.setDataSource(mFilePath);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sb_progress = (SeekBar) findViewById(R.id.sb_progress);
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

        new Thread() {
            @Override
            public void run() {
                while (true) {
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
     * 选择一个文件, 返回其绝对路径
     *
     * @param v
     */
    public void selectFile(View v) {
        Intent intent = new Intent(this, SelectFileActivity.class);
        startActivityForResult(intent, 0);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            //获取选中的mFilePath
            mFilePath = data.getStringExtra("path");
//            Toast.makeText(getApplicationContext(),
//                    mFilePath, Toast.LENGTH_SHORT).show();

            et_input_path.setText(mFilePath);

            initMediaPlayer();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param v
     */
    public void playMusic(View v) {
        sb_progress.setMax(mediaPlayer.getDuration());
        //如果没在播放中，立刻开始播放。
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    /**
     * @param v
     */
    public void pauseMusic(View v) {
        //如果在播放中，立刻暂停。
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /**
     * @param v
     */
    public void stopMusic(View v) {
        //如果在播放中，立刻停止
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
            initMediaPlayer();//初始化播放器 MediaPlayer
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
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //initMediaPlayer();
                } else {
                    Toast.makeText(this, "拒绝权限, 将无法使用程序.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }
}
