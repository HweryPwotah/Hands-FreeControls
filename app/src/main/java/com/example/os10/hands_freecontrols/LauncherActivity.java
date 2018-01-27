package com.example.os10.hands_freecontrols;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

/**
 * Launcher class. User will be directed to this activity on startup
 */


public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_launcher);
    }

    public void OpenNavigation(View view){
        //Accessibility Service initialization
        if(Preferences.initForA11yService(this) == null) return;

        TheAccessibilityService service= TheAccessibilityService.get();
        if (null != service) { //The application has been given permission
//            Toast.makeText(this, "Permission is granted", Toast.LENGTH_SHORT).show();
            //launch initialization
            service.onServiceConnected();
        }
        else { //The application has not been given permission.
            //Thus, The application is running for the first time
            Toast.makeText(this, "Please allow the program to have accessibility services.", Toast.LENGTH_SHORT).show();
            if (!openAccessibility()) {
                noAccessibilitySettingsAlert();
            }
        }
    }

    public void navigateToSettings(View view){
        Intent intent = new Intent(this, SettingsActivity.class);
        this.startActivity(intent);
    }

    public void navigateToAbout(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        this.startActivity(intent);
    }

    /**
     * Open the accessibility settings screen
     *
     * @return true on success
     */
    private boolean openAccessibility() {
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            startActivity(intent, null);
        }
        catch(ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    /* Keep references to the dialogs to properly dismiss them. See:
       http://stackoverflow.com/questions/2850573/activity-has-leaked-window-that-was-originally-added */
//    Dialog mHelpDialog;
    Dialog mNoA11ySettingsDialog;
    /**
     * Display message for no accessibility settings available
     */
    private void noAccessibilitySettingsAlert() {
        final Resources r= getResources();
        mNoA11ySettingsDialog= new AlertDialog.Builder(this)
                .setMessage(r.getText(R.string.launcher_no_accessibility_settings))
                .setPositiveButton(r.getText(R.string.launcher_done),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        })
                .create();
        mNoA11ySettingsDialog.setCancelable(false);
        mNoA11ySettingsDialog.setCanceledOnTouchOutside(false);
        mNoA11ySettingsDialog.show();
    }
}