package com.example.administrator.voicelockunlock.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.voicelockunlock.LockScreen.Lockscreen;
import com.example.administrator.voicelockunlock.LockScreen.LockscreenUtil;
import com.example.administrator.voicelockunlock.LockScreen.PermissionActivity;
import com.example.administrator.voicelockunlock.LockScreen.SharedPreferencesUtil;
import com.example.administrator.voicelockunlock.R;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class LockscreenViewService extends Service {

    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private View mLockscreenView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private RelativeLayout mBackgroundLayout = null;
    private RelativeLayout mBackgroundInLayout = null;
    private ImageView mBackgroundLockImageView = null;
    private RelativeLayout mForgroundLayout = null;
    private RelativeLayout mStatusBackgruondDummyView = null;
    private RelativeLayout mStatusForgruondDummyView = null;
    private boolean mIsLockEnable = false;
    private boolean mIsSoftkeyEnable = false;
    private int mDeviceWidth = 0;
    private int mDevideDeviceWidth = 0;
    private float mLastLayoutX = 0;
    public  int mServiceStartId = 0;
    private SendMassgeHandler mMainHandler = null;
    private ImageView imgSpeackVoice;

    private GifImageView gifImageView;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected TextView txtspeakReslutl;

    private class SendMassgeHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            changeBackGroundLockView(mLastLayoutX);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferencesUtil.init(mContext);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMainHandler = new SendMassgeHandler();
        if (isLockScreenAble()) {
            if (null != mWindowManager) {
                if (null != mLockscreenView) {
                    mWindowManager.removeView(mLockscreenView);
                }
                mWindowManager = null;
                mParams = null;
                mInflater = null;
                mLockscreenView = null;
            }
            initState();
            initView();
            attachLockScreenView();
        }
        return LockscreenViewService.START_STICKY;
    }

    @Override
    public void onDestroy() {
        dettachLockScreenView();
    }


    private void initState() {

        mIsLockEnable = LockscreenUtil.getInstance(mContext).isStandardKeyguardState();
        if (mIsLockEnable) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                    PixelFormat.TRANSLUCENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsLockEnable && mIsSoftkeyEnable) {
                mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            } else {
                mParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
            }
        } else {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if (null == mWindowManager) {
            mWindowManager = ((WindowManager) mContext.getSystemService(WINDOW_SERVICE));
        }
    }

    private void initView() {
        if (null == mInflater) {
            mInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (null == mLockscreenView) {
            mLockscreenView = mInflater.inflate(R.layout.view_locokscreen, null);


        }
    }

    private boolean isLockScreenAble() {
        boolean isLock = SharedPreferencesUtil.get(Lockscreen.ISLOCK);
        if (isLock) {
            isLock = true;
        } else {
            isLock = false;
        }
        return isLock;
    }


    private void attachLockScreenView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mContext)) {
                Intent permissionActivityIntent = new Intent(mContext, PermissionActivity.class);
                permissionActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(permissionActivityIntent);

                LockscreenUtil.getInstance(mContext).getPermissionCheckSubject()
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Action1<Boolean>() {
                                    @Override
                                    public void call(Boolean aBoolean) {
                                        addLockScreenView();
                                    }
                                }
                        );
            } else {
                addLockScreenView();
            }
        } else {
            addLockScreenView();
        }

    }

    private void addLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView && null != mParams) {
            mLockscreenView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mWindowManager.addView(mLockscreenView, mParams);
            settingLockView();
        }
    }


    private boolean dettachLockScreenView() {
        if (null != mWindowManager && null != mLockscreenView && isAttachedToWindow()) {
            mWindowManager.removeView(mLockscreenView);
            mLockscreenView = null;
            mWindowManager = null;
            stopSelf(mServiceStartId);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isAttachedToWindow() {
        return mLockscreenView.isAttachedToWindow();
    }

    private void settingLockView() {


        mBackgroundLayout = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_background_layout);
        mBackgroundInLayout = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_background_in_layout);
        mBackgroundLockImageView = (ImageView) mLockscreenView.findViewById(R.id.lockscreen_background_image);
        mForgroundLayout = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_forground_layout);
        txtspeakReslutl=(TextView)mLockscreenView.findViewById(R.id.speakReslutl);


        imgSpeackVoice=(ImageView)mLockscreenView.findViewById(R.id.imgSpeackVoice);
        imgSpeackVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                gifImageView=(GifImageView)mLockscreenView.findViewById(R.id.mygifImage);

                SpeechRecognitionListener h = new SpeechRecognitionListener();
                mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
                mSpeechRecognizer.setRecognitionListener(h);
                mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                if (mSpeechRecognizer.isRecognitionAvailable(mContext))
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mContext.getPackageName());
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);


            }
        });

        mStatusBackgruondDummyView = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_background_status_dummy);
        mStatusForgruondDummyView = (RelativeLayout) mLockscreenView.findViewById(R.id.lockscreen_forground_status_dummy);
        setBackGroundLockView();

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mDeviceWidth = displayMetrics.widthPixels;
        mDevideDeviceWidth = (mDeviceWidth / 2);
        mBackgroundLockImageView.setX((int) (((mDevideDeviceWidth) * -1)));

        //kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int val = LockscreenUtil.getInstance(mContext).getStatusBarHeight();
            RelativeLayout.LayoutParams forgroundParam = (RelativeLayout.LayoutParams) mStatusForgruondDummyView.getLayoutParams();
            forgroundParam.height = val;
            mStatusForgruondDummyView.setLayoutParams(forgroundParam);
            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            mStatusForgruondDummyView.startAnimation(alpha);
            RelativeLayout.LayoutParams backgroundParam = (RelativeLayout.LayoutParams) mStatusBackgruondDummyView.getLayoutParams();
            backgroundParam.height = val;
            mStatusBackgruondDummyView.setLayoutParams(backgroundParam);
        }
    }

    private void setBackGroundLockView() {
        if (mIsLockEnable) {
            mBackgroundInLayout.setBackgroundColor(getResources().getColor(R.color.lock_background_color));
            mBackgroundLockImageView.setVisibility(View.VISIBLE);

        } else {
            mBackgroundInLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mBackgroundLockImageView.setVisibility(View.GONE);
        }
    }


    private void changeBackGroundLockView(float forgroundX) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.unlock));
            }
        } else {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.unlock));
            }
        }
    }


    public void optimizeForground(float forgroundX) {
        if (forgroundX < mDevideDeviceWidth) {
            int startPostion = 0;
            for (startPostion = mDevideDeviceWidth; startPostion >= 0; startPostion--) {
                mForgroundLayout.setX(startPostion);
            }
        } else {
            TranslateAnimation animation = new TranslateAnimation(0, mDevideDeviceWidth, 0, 0);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mForgroundLayout.setX(mDevideDeviceWidth);
                    mForgroundLayout.setY(0);
                    dettachLockScreenView();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            mForgroundLayout.startAnimation(animation);
        }
    }

    class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {
            imgSpeackVoice.setVisibility(View.GONE);
            gifImageView.setVisibility(View.VISIBLE);
            txtspeakReslutl.setText("Speak Password Now");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            gifImageView.setVisibility(View.GONE);
            imgSpeackVoice.setVisibility(View.VISIBLE);

        }

        @Override
        public void onError(int i) {

            txtspeakReslutl.setText("Password Not Match");
        }

        @Override
        public void onResults(Bundle results) {

            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            //Toast.makeText(getApplicationContext(),matches.get(0),Toast.LENGTH_SHORT).show();
            MediaPlayer password_accepted = MediaPlayer.create(mContext, R.raw.password_accepted);
            MediaPlayer invalid_password = MediaPlayer.create(mContext, R.raw.invalid_password);

            SharedPreferences prefs = getSharedPreferences("PassData", MODE_PRIVATE);
            String Password = prefs.getString("Password", null);
            if (Password != null) {

                if(matches.get(0).matches(Password))
                {
                    optimizeForground(620);
                    password_accepted.start();
                }
                else
                {
                    txtspeakReslutl.setText("Invalid Voice Password"+ "\n " + "\""+ matches.get(0) +"\"");
                    invalid_password.start();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Default Password Is  \"hello\" ",Toast.LENGTH_SHORT).show();

                if(matches.get(0).matches("hello"))
                {
                    optimizeForground(620);
                    password_accepted.start();
                }
                else
                {
                    txtspeakReslutl.setText("Invalid Voice Password"+ "\n " + "\""+ matches.get(0) +"\"");
                    invalid_password.start();
                }
            }


        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

}
