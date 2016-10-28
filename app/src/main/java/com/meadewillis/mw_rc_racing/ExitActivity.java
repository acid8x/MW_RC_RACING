package com.meadewillis.mw_rc_racing;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ExitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        Button buttonYes = (Button) findViewById(R.id.buttonYes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                MainActivity.exitingState = true;
                finish();
            }
        });

        Button buttonNo = (Button) findViewById(R.id.buttonNo);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.exitingState = false;
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        MainActivity.exitingState = false;
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
