# AnimatedRecordingView
Android animated recording view .[中文版](https://github.com/HeZaiJin/AnimatedRecordingView/blob/master/README-cn.md)
## Preview
![预览](https://github.com/HeZaiJin/AnimatedRecordingView/blob/master/screen_shot/animated_recording.gif)
## Gradle
```java
compile 'com.haozhang.libary:android-animated-recording-view:1.0'
```
## How to use
### Use in xml
```java
<com.haozhang.lib.AnimatedRecordingView
    android:id="@+id/recording"
    android:layout_width="match_parent"
    android:layout_height="200px"
    />
```
### Use in method
```java
    AnimatedRecordingView mRecordingView = (AnimatedRecordingView) findViewById(R.id.recording);
    // start recording animation
    mRecordingView.start();
    // set the mic volume
    float vol;
    mRecordingView.setVolume(vol);
    // start loading animation
    mRecordingView.loading();
    // start finished animation
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
