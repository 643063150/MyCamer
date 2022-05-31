package com.android.zpp.mycamer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.zpp.mycamer.databinding.MyLayoutBinding;
import com.android.zpp.silicompressor.SiliCompressor;
import com.android.zpp.videocamer.JCameraView;
import com.android.zpp.videocamer.listener.ClickListener;
import com.android.zpp.videocamer.listener.ErrorListener;
import com.android.zpp.videocamer.listener.JCameraListener;
import com.android.zpp.videocamer.listener.RecordStateListener;
import com.android.zpp.videocamer.util.FileUtil;

import java.io.File;

/**
 * @ProjectName: MyCamer
 * @Package: com.android.zpp.mycamer
 * @ClassName: CamerActivity
 * @Description:
 * @Author: zpp
 * @CreateDate: 2022/5/31 14:28
 * @UpdateUser:
 * @UpdateDate: 2022/5/31 14:28
 * @UpdateRemark:
 */
public class CamerActivity extends AppCompatActivity {
    MyLayoutBinding myLayoutBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        myLayoutBinding = MyLayoutBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(myLayoutBinding.getRoot());
        myLayoutBinding.jcameraview.setSaveVideoPath(
                getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());
        //设置最短录制时长
        myLayoutBinding.jcameraview.setMinDuration(3000);
        //设置最长录制时长
        myLayoutBinding.jcameraview.setDuration(30000);
        //设置录制模式直录制视频
        myLayoutBinding.jcameraview.setFeatures(JCameraView.BUTTON_STATE_ONLY_RECORDER);
        myLayoutBinding.jcameraview.setTip("长按拍摄, 3~30秒");
        myLayoutBinding.jcameraview.setRecordShortTip("录制时间3~30秒");
        //设置视频的比特率
        myLayoutBinding.jcameraview.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        myLayoutBinding.jcameraview.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.d("CJT", "camera error");
                Toast.makeText(CamerActivity.this, "录制发生错误", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void AudioPermissionError() {
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                startActivity(intent);
                Toast.makeText(CamerActivity.this, "未获取到录音权限", Toast.LENGTH_SHORT).show();
            }
        });
        //JCameraView监听
        myLayoutBinding.jcameraview.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                String path = FileUtil.saveBitmap("small_video", bitmap);
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
//                String path = FileUtil.saveBitmap("small_video", firstFrame);
//                Intent intent = new Intent();
//                intent.putExtra("videoPath", url);
//                setResult(600, intent);
//                finish();
//                Log.d("CJT", "url:" + url + ", firstFrame:" + path);
//                finish();
                if (shouldCompressVideo(url)){
                    compressVideo(url);
                }
            }
        });
        myLayoutBinding.jcameraview.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        myLayoutBinding.jcameraview.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(CamerActivity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });
        myLayoutBinding.jcameraview.setRecordStateListener(new RecordStateListener() {
            @Override
            public void recordStart() {

            }

            @Override
            public void recordEnd(long time) {
                Log.e("录制状态回调", "录制时长：" + time);
            }

            @Override
            public void recordCancel() {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myLayoutBinding.jcameraview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myLayoutBinding.jcameraview.onPause();
    }

    /**
     * 压缩视频
     *
     * @param srcPath
     */
    public void compressVideo(String srcPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String destDirPath = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
                    int outWidth = 0;
                    int outHeight = 0;
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(srcPath);
                    double mOriginalWidth = Double.parseDouble(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    double mOriginalHeight = Double.parseDouble(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    if (mOriginalWidth > mOriginalHeight) {
                        //横屏
                        outWidth = 960;
                        outHeight = 544;
                    } else {
                        //竖屏
                        outWidth = 544;
                        outHeight = 960;
                    }
                    String compressedFilePath = SiliCompressor.with(CamerActivity.this)
                            .compressVideo(srcPath, destDirPath, outWidth, outHeight, 950000);
                    Log.e("压缩", compressedFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /**
     * 是否需要压缩视频
     *
     * @param path
     * @return
     */
    public boolean shouldCompressVideo(String path) {
        boolean shouldCompress = true;
        //小于3m不压缩
        if (FileSizeUtil.getFileOrFilesSize(path, 3) < 3) {
            shouldCompress = false;
        } else {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            double mOriginalWidth = Double.parseDouble(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            double mOriginalHeight = Double.parseDouble(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            double bitrate = Double.parseDouble(mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE));
            //视频信息小于预设值则不压缩，某则越压缩越大
            if (mOriginalWidth > mOriginalHeight) {
                //横屏
                if (mOriginalWidth < 960 && mOriginalHeight < 544 && bitrate < 950000) {
                    shouldCompress = false;
                }

            } else {
                //竖屏
                if (mOriginalWidth < 544 && mOriginalHeight < 960 && bitrate < 950000) {
                    shouldCompress = false;
                }
            }
        }

        return shouldCompress;
    }
}
