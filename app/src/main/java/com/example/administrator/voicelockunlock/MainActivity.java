package com.example.administrator.voicelockunlock;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.administrator.voicelockunlock.LockScreen.Lockscreen;
import com.example.administrator.voicelockunlock.LockScreen.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity
{
    public SwitchCompat mSwitchd = null;
    public Context mContext = null;
    public TextView txtLockScreen,txtVersion;
    public String versionName="";
    public LinearLayout linVoicePass,linPassword;
    private int REQUEST_ID_MULTIPLE_PERMISSIONS = 23;
    private boolean PermissionResult=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        GetPermissionDetails();


        txtLockScreen=(TextView) findViewById(R.id.txtlockscreenlab);
        Typeface type = Typeface.createFromAsset(getAssets(),"myfont.ttf");
        txtLockScreen.setTypeface(type);

        txtVersion=(TextView)findViewById(R.id.txtVersion);
        try
        {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            if(versionName!="")
            {
                txtVersion.setText("Ver "+versionName);
            }

        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }


        SharedPreferencesUtil.init(mContext);

        mSwitchd = (SwitchCompat) this.findViewById(R.id.switch_locksetting);
        mSwitchd.setTextOn("yes");
        mSwitchd.setTextOff("no");
        boolean lockState = SharedPreferencesUtil.get(Lockscreen.ISLOCK);
        if (lockState)
        {
            mSwitchd.setChecked(true);

        } else {
            mSwitchd.setChecked(false);

        }

        mSwitchd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    if(PermissionResult==true)
                    {
                        SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, true);
                        Lockscreen.getInstance(mContext).startLockscreenService();
                    }

                }
                else {

                    SharedPreferencesUtil.setBoolean(Lockscreen.ISLOCK, false);
                    Lockscreen.getInstance(mContext).stopLockscreenService();
                }

            }
        });

        linVoicePass=(LinearLayout)findViewById(R.id.linerVoice);
        linVoicePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GetSpeechInputText();
                startActivity(new Intent(MainActivity.this,VoicePassword.class));
            }
        });

        linPassword=(LinearLayout)findViewById(R.id.linerPassword);
        linPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Comming Soon !!!",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void  GetPermissionDetails() {

        boolean result=CheckPermissionsGranted();

        if(result==true)
        {
            PermissionResult=true;
        }
        else
        {
            requestPermission();
        }

    }

    public boolean CheckPermissionsGranted() {

        //this code for multiple permission to check like location and phone state
        int permissionPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int RECORD_AUDIO = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        if(permissionPhoneState==0 && RECORD_AUDIO==0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void requestPermission() {

        //this code for multiple permission to check like location and phone state
        int permissionPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        int RECORD_AUDIO = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionPhoneState != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS){

            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                PermissionResult=true;
            }
            else
            {
                Toast.makeText(getApplicationContext(),"oops you dented permissions !!!",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
