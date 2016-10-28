package com.meadewillis.mw_rc_racing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class RaceSelectionActivity extends Activity {

    static int mRaceType = 0, mLaps = 0, mGates = 0, mKills = 0;
    static String sLaps = "How many laps ? -> ", sGates = "How many gates ? -> ", sKills = "How many kills ? -> ";
    private LinearLayout llLaps, llGates, llKills;
    private TextView tvLaps, tvGates, tvKills;
    private RadioButton rb1, rb2, rb3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mLaps = bundle.getInt("LAPS");
            mGates = bundle.getInt("GATES");
            mKills = bundle.getInt("KILLS");
        }
        setContentView(R.layout.activity_race_selection);

        setResult(Activity.RESULT_CANCELED);

        createView();
    }

    private void createView() {
        String temp;
        llLaps = (LinearLayout) findViewById(R.id.llLaps);
        llGates = (LinearLayout) findViewById(R.id.llGates);
        llKills = (LinearLayout) findViewById(R.id.llKills);
        tvLaps = (TextView) findViewById(R.id.tvLaps);
        temp = sLaps + mLaps;
        tvLaps.setText(temp);
        tvGates = (TextView) findViewById(R.id.tvGates);
        temp = sGates + mGates;
        tvGates.setText(temp);
        tvKills = (TextView) findViewById(R.id.tvKills);
        temp = sKills + mKills;
        tvKills.setText(temp);
        rb1 = (RadioButton) findViewById(R.id.radioButton);
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb1.setChecked(true);
                rb2.setChecked(false);
                rb3.setChecked(false);
                llLaps.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                llGates.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                llKills.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                mRaceType = 1;
            }
        });
        rb2 = (RadioButton) findViewById(R.id.radioButton2);
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb1.setChecked(false);
                rb2.setChecked(true);
                rb3.setChecked(false);
                llLaps.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                llGates.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                llKills.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                mRaceType = 2;
            }
        });
        rb3 = (RadioButton) findViewById(R.id.radioButton3);
        rb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rb1.setChecked(false);
                rb2.setChecked(false);
                rb3.setChecked(true);
                llLaps.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                llGates.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
                llKills.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                mRaceType = 3;
            }
        });
        SeekBar sbLaps = (SeekBar) findViewById(R.id.seekLaps);
        sbLaps.setProgress((mLaps - 1));
        sbLaps.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLaps = progress + 1;
                String temp = sLaps + mLaps;
                tvLaps.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar sbGates = (SeekBar) findViewById(R.id.seekGates);
        sbGates.setProgress((mGates - 1));
        sbGates.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mGates = progress + 2;
                String temp = sGates + mGates;
                tvGates.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar sbKills = (SeekBar) findViewById(R.id.seekKills);
        sbKills.setProgress((mKills - 1));
        sbKills.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mKills = progress + 1;
                String temp = sKills + mKills;
                tvKills.setText(temp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("RACE_TYPE", mRaceType);
                intent.putExtra("LAPS", mLaps);
                intent.putExtra("GATES", mGates);
                intent.putExtra("KILLS", mKills);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
