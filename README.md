# MyCamer

video 录制与压缩

前人造的轮子，自己集成整合了一下，改写兼容了 Androidx

库主要来源：

- [CJT2325](https://github.com/CJT2325/CameraView)
- [Tourenathan-G5organisation](https://github.com/Tourenathan-G5organisation/SiliCompressor)
- [MasayukiSuda](https://github.com/MasayukiSuda/Mp4Composer-android)

# 主要用法

## 布局

```xml
 <com.android.zpp.videocamer.JCameraView
        android:id="@+id/jcameraview"
        app:duration_max="10000"
        app:iconMargin="20dp"
        app:iconLeft="@drawable/ic_arrow_camera"
        app:iconSize="30dp"
        app:iconSrc="@drawable/ic_camera_rotation"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

## java 代码

```java
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

```

可绑定的生命周期

```java
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

```

## 压缩

比较耗时 建议放到线程中去做

```java
//宽高和比特率自己定义
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

```
