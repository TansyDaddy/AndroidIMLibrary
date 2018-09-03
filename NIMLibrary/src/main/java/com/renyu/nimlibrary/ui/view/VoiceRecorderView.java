package com.renyu.nimlibrary.ui.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.renyu.nimlibrary.R;

import java.io.File;

public class VoiceRecorderView extends RelativeLayout implements IAudioRecordCallback {

    protected Drawable[] micImages;

    protected ImageView micImage;
    protected TextView recordingHint;

    AudioRecorder audioMessageHelper;

    private boolean isRecording = false;

    private int maxDuration = 60;
    private long currentSecond = 0;//当前秒数

    private static final int WHAT_CURRENT_RECORD_DURATION = 11;//当前录音的时间
    private Runnable timeRunnable;
    private boolean cancel = false;
    private boolean isShowTime = false;//显示倒计时

    // 图片刷新使用
    @SuppressLint("HandlerLeak")
    Handler micImageHandler = new Handler() {
        @SuppressLint("DefaultLocale")
        @Override
        public void handleMessage(android.os.Message msg) {
            if (!cancel) {
                micImage.setImageDrawable(micImages[msg.what]);
            }
        }
    };

    //计时器
    @SuppressLint("HandlerLeak")
    private Handler timeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_CURRENT_RECORD_DURATION) {
                if (audioMessageHelper.isRecording()) {
                    long time = (long) msg.obj;
                    if (time >= maxDuration - 10 && !cancel) {//倒计时10秒
                        isShowTime = true;
                        recordingHint.setText(String.format("还能继续说%d秒", maxDuration - time));
                    } else {
                        isShowTime = false;
                    }
                } else {
                    timeHandler.removeCallbacks(timeRunnable);
                }

            }
        }
    };


    // 被点击的View
    View v;

    // 录音成功回调接口
    IAudioRecordCallback callback;

    public interface IAudioRecordCallback {
        void onRecordSuccess(File audioFile, long audioLength, RecordType recordType);
    }

    public void setIAudioRecordCallback(IAudioRecordCallback callback) {
        this.callback = callback;
    }

    public VoiceRecorderView(Context context) {
        super(context);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceRecorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ease_widget_voice_recorder, this);

        micImage = findViewById(R.id.mic_image);
        recordingHint = findViewById(R.id.recording_hint);
        micImages = new Drawable[]{getResources().getDrawable(R.mipmap.ease_record_animate_01),
                getResources().getDrawable(R.mipmap.ease_record_animate_02),
                getResources().getDrawable(R.mipmap.ease_record_animate_03),
                getResources().getDrawable(R.mipmap.ease_record_animate_04),
                getResources().getDrawable(R.mipmap.ease_record_animate_05),
                getResources().getDrawable(R.mipmap.ease_record_animate_06),
                getResources().getDrawable(R.mipmap.ease_record_animate_07)};

        if (maxDuration < 0) {
            maxDuration = AudioRecorder.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND;
        }
        audioMessageHelper = new AudioRecorder(context, RecordType.AAC, maxDuration, this);
    }

    /**
     * 长按开始录音
     *
     * @param v
     * @param event
     * @return
     */
    public boolean onPressToSpeakBtnTouch(View v, MotionEvent event) {
        this.v = v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecording(v);
                audioMessageHelper.startRecord();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getY()) > 300) {
                    cancel = true;
                    showReleaseToCancelHint();
                } else {
                    cancel = false;
                    if (!isShowTime) {
                        showMoveUpToCancelHint();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!cancel && event.getEventTime() - event.getDownTime() < 1000) {
                    showTooShortHint();
                    cancel = true;
                    audioMessageHelper.completeRecord(true);
                    new Handler().postDelayed(() -> stopRecording(v), 1000);
                } else {
                    audioMessageHelper.completeRecord(cancel);
                    stopRecording(v);
                }

                return true;
            default:
                audioMessageHelper.completeRecord(true);
                return false;
        }
    }

    @Override
    public void onRecordReady() {

    }

    @Override
    public void onRecordStart(File audioFile, RecordType recordType) {
        Log.d("NIM_APP", "onRecordStart");
        currentSecond = 0;
        timeRunnable = () -> {
            try {
                if (audioMessageHelper.isRecording()) {
                    currentSecond = currentSecond + 1;
                    Log.d("onRecordStart", " currentSecond= " + currentSecond);
                    Message message = timeHandler.obtainMessage();
                    message.what = WHAT_CURRENT_RECORD_DURATION;
                    message.obj = currentSecond;
                    timeHandler.sendMessage(message);
                    timeHandler.postDelayed(timeRunnable, 1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
        if (isRecording) {
            timeHandler.postDelayed(timeRunnable, 1000);
        }
    }

    @Override
    public void onRecordSuccess(File audioFile, long audioLength, RecordType recordType) {
        Log.d("NIM_APP", audioFile.getAbsolutePath() + "  " + audioLength);
        if (callback != null)
            callback.onRecordSuccess(audioFile, audioLength, recordType);
    }

    @Override
    public void onRecordFail() {
        if (isRecording) {
            Toast.makeText(Utils.getApp(), "录音失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecordCancel() {

    }

    @Override
    public void onRecordReachedMaxTime(int maxTime) {
        Toast.makeText(Utils.getApp(), "录音已到达最大时间，直接发送", Toast.LENGTH_SHORT).show();
        stopRecording(v);
        audioMessageHelper.handleEndRecord(true, maxTime);
    }

    public void startRecording(View view) {
        ((Activity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVisibility(View.VISIBLE);
        showMoveUpToCancelHint();
        view.setPressed(true);

        isRecording = true;
        new Thread(() -> {
            try {
                while (isRecording) {
                    Message msg = new Message();
                    msg.what = audioMessageHelper.getCurrentRecordMaxAmplitude() * 6 / 0x7FFF;
                    micImageHandler.sendMessage(msg);
                    SystemClock.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void stopRecording(View view) {
        ((Activity) getContext()).getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVisibility(GONE);
        if (view != null) {
            view.setPressed(false);
        }

        isRecording = false;
    }

    public void showReleaseToCancelHint() {
        recordingHint.setText("松开手指取消发送");
        recordingHint.setBackgroundResource(R.drawable.ease_recording_text_hint_bg);
        micImage.setImageResource(R.mipmap.cancel);
    }

    public void showMoveUpToCancelHint() {
        recordingHint.setText("手指上滑取消发送");
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
        int i = audioMessageHelper.getCurrentRecordMaxAmplitude() * 6 / 0x7FFF;
        micImage.setImageDrawable(micImages[i]);
    }

    public void showTooShortHint() {
        recordingHint.setText("说话时间太短");
        recordingHint.setBackgroundColor(Color.TRANSPARENT);
        micImage.setImageResource(R.mipmap.voice_to_short);
    }

    public void onPause() {
        // 停止录音
        if (audioMessageHelper != null) {
            stopRecording(v);
            audioMessageHelper.completeRecord(true);
        }
    }

    public void onDestroy() {
        if (audioMessageHelper != null) {
            audioMessageHelper.destroyAudioRecorder();
        }
    }
}
