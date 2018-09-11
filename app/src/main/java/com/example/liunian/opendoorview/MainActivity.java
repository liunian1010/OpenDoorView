package com.example.liunian.opendoorview;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    OpenDoorView openDoorView;
    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openDoorView = findViewById(R.id.openDoorView);
        countDownTimer = new CountDownTimer(7625, 125) {
            @Override
            public void onTick(long millisUntilFinished) {
                openDoorView.setProgress(openDoorView.getProgress() + 1);
            }

            @Override
            public void onFinish() {
            }
        };
        countDownTimer.start();
        openDoorView.startAnimator();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
