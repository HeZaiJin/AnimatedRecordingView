# AnimatedRecordingView
Android slanted TextView .
## Preview
![预览](https://github.com/HeZaiJin/AnimatedRecordingView/blob/master/screen_shot/animated_recording.gif)
## Gradle
```java
compile 'com.haozhang.libary:android-animated-recording-view:1.0'
```
## 如何使用
### 配置XML
```java
<com.haozhang.lib.AnimatedRecordingView
    android:id="@+id/recording"
    android:layout_width="match_parent"
    android:layout_height="200px"
    />
```
### 方法简析
```java
    AnimatedRecordingView mRecordingView = (AnimatedRecordingView) findViewById(R.id.recording);
    // 开始录音动画,一般紧接着录音动作
    mRecordingView.start();
    // 设置volume 设置麦克风的分贝值
    float vol;
    mRecordingView.setVolume(vol);
    // 录音结束的loading动画,一般用于后台处理操作(识别匹配,网络数据等等)
    mRecordingView.loading();
    // loading结束动画
    mRecordingView.stop();
```

#License
```
Copyright 2016 Hand HaoZhang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
