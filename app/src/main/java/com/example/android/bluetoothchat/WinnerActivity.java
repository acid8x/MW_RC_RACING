package com.example.android.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WinnerActivity extends Activity {

    private static String[] Name = new String[5];
    private static TextView[] tvWinner = new TextView[5];
    private static LinearLayout[] ll = new LinearLayout[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Name[0] = bundle.getString("P1");
            Name[1] = bundle.getString("P2");
            Name[2] = bundle.getString("P3");
            Name[3] = bundle.getString("P4");
            Name[4] = bundle.getString("P5");
        }
        setContentView(R.layout.activity_winner);

        setResult(Activity.RESULT_OK);

        int start = R.id.tvWinner1;
        int count = R.id.tvWinner2 - start;
        int startLL = R.id.ll1;
        int countLL = R.id.ll2 - startLL;

        for (int i = 0; i < 5; i++) {
            int id = start + (count * i);
            int idLL = startLL + (countLL * i);
            tvWinner[i] = (TextView) findViewById(id);
            ll[i] = (LinearLayout) findViewById(idLL);
            if (!Name[i].equals("")) {
                tvWinner[i].setText(Name[i]);
            } else {
                ll[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
                ll[i].setVisibility(View.INVISIBLE);
            }
        }

        Button buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}