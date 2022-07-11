package com.viniciusbozzi.project.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.viniciusbozzi.project.R;

/**
 DESENVOLVIDO POR VINICIUS BOZZI
 */

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressBar = findViewById(R.id.progressBarSplash);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnimator.setDuration(3000);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                abrirAutenticacao();
            }
        }, 3000);

    }

    private void abrirAutenticacao(){
        Intent i = new Intent(SplashActivity.this, AutenticacaoActivity.class);
        startActivity(i);
        finish();
    }
}
