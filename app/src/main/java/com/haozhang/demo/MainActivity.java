package com.haozhang.demo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.haozhang.demo.util.RecorderHelper;
import com.haozhang.lib.AnimatedRecordingView;

public class MainActivity extends AppCompatActivity implements RecorderHelper.onRecorderListener {
    static final int REQUEST_CODE = 1001;
    private static final String TAG = "MainActivity";
    AnimatedRecordingView mRecordingView;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();
    }

    void initView() {
        mRecordingView = (AnimatedRecordingView) findViewById(R.id.recording);
        mRecordingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
                } else {
                    if (!mRecordingView.isWorking()) {
                        RecorderHelper.getInstance().startRecord();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE){
            if(grantResults.length >0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //用户同意授权
                if (!mRecordingView.isWorking()) {
                    RecorderHelper.getInstance().startRecord();
                }
            }else{
                //用户拒绝授权
                Toast.makeText(this,"cancel",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void init() {
        RecorderHelper.getInstance().setPath(getExternalCacheDir()+"/hand.amr").setRecorderListener(this);
    }

    @Override
    public void recorderStart() {
        Log.d(TAG, "recorderStart() called");
        mHandler.removeCallbacks(mLoading);
        mRecordingView.start();
    }

    @Override
    public void recorderStop() {
        Log.d(TAG, "recorderStop() called");
        mRecordingView.loading();
        mHandler.removeCallbacks(mLoading);
        mHandler.postDelayed(mLoading, 1500);
    }

    Runnable mLoading = new Runnable() {
        @Override
        public void run() {
            mRecordingView.stop();
        }
    };

    @Override
    public void volumeChange(float vol) {
        Log.d(TAG, "volumeChange() called with: vol = [" + vol + "]");
        mRecordingView.setVolume(vol);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecorderHelper.getInstance().cancel();
    }
}
