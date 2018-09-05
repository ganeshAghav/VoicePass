package com.example.administrator.voicelockunlock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class VoicePassword extends Activity {

    private GifImageView gifImageView;
    private TextView voiceResult,txtLabletop;
    private ImageView btnNext,btnSave,imgVoiceSpeak;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    private String NewPassword="";
    private String ConformPassword="";
    private  MediaPlayer invalid_password;
    private MediaPlayer password_accepted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_password);

        gifImageView=(GifImageView)findViewById(R.id.voicegifImage);
        imgVoiceSpeak=(ImageView)findViewById(R.id.VoiceSpeackVoice);
        voiceResult=(TextView) findViewById(R.id.txtvoiceresult);
        btnNext=(ImageView) findViewById(R.id.imgbtnnext);
        txtLabletop=(TextView)findViewById(R.id.txtlabel);
        btnSave=(ImageView)findViewById(R.id.imgbtnsave);

        invalid_password = MediaPlayer.create(VoicePassword.this, R.raw.invalid_password);
        password_accepted = MediaPlayer.create(VoicePassword.this, R.raw.password_accepted);

        txtLabletop.setText("Create New Voice Password Now");
        btnNext.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);

        imgVoiceSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SpekRecognitionListener h = new SpekRecognitionListener();
                mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(VoicePassword.this);
                mSpeechRecognizer.setRecognitionListener(h);
                mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                if (mSpeechRecognizer.isRecognitionAvailable(VoicePassword.this))
                    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, VoicePassword.this.getPackageName());
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(NewPassword!="")
                {
                    txtLabletop.setText("Speak Current Voice Password To \n Change Your Voice Password");
                    btnNext.setVisibility(View.GONE);
                    btnSave.setVisibility(View.VISIBLE);

                }
                else
                {
                    txtLabletop.setText("Create New Voice Password Now");
                    btnNext.setVisibility(View.VISIBLE);
                    btnSave.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),"Please Speak Something First To Record",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(NewPassword!="" && ConformPassword!="")
                {
                    if(ConformPassword.matches(NewPassword))
                    {
                        password_accepted.start();
                        SaveNewPassword();
                    }
                    else
                    {
                        invalid_password.start();
                        StartTextAnimation();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please Conform Your Voice Password !!!",Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    class SpekRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {
            imgVoiceSpeak.setVisibility(View.GONE);
            gifImageView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"Please Speak Something To Record",Toast.LENGTH_SHORT).show();
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
            imgVoiceSpeak.setVisibility(View.VISIBLE);

        }

        @Override
        public void onError(int i) {

            Toast.makeText(getApplicationContext(),"Voice Not Found",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {

            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            voiceResult.setText("Your Voice Password \n"+ matches.get(0));

            if(NewPassword!="")
            {
                ConformPassword=matches.get(0);

                if(ConformPassword.matches(NewPassword))
                {
                    password_accepted.start();
                    SaveNewPassword();
                }
                else
                {
                    txtLabletop.setText("Voice Password Does Not Match");
                    StartTextAnimation();
                    invalid_password.start();
                }
            }
            else
            {
                NewPassword=matches.get(0);
            }

        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }

    public void StartTextAnimation(){

        Animation animBlink;
        animBlink = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bouncing);
        animBlink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        txtLabletop.startAnimation(animBlink);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                txtLabletop.clearAnimation();
                txtLabletop.animate().cancel();
            }
        }, 3000);
    }

    public void SaveNewPassword(){

        SharedPreferences.Editor editor = getSharedPreferences("PassData", MODE_PRIVATE).edit();
        editor.putString("Password", ConformPassword);
        editor.apply();

        startActivity(new Intent(VoicePassword.this,MainActivity.class));
    }


}
