package com.example.android.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

public class PlayerEditorActivity extends Activity {

    private static int Id, Red, Green, Blue;
    private static String Name;

    private EditText editText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Id = bundle.getInt("ID");
            Name = bundle.getString("NAME");
            Red = bundle.getInt("RED");
            Green = bundle.getInt("GREEN");
            Blue = bundle.getInt("BLUE");
        }
        setContentView(R.layout.activity_player_editor);

        setResult(PlayerEditorActivity.RESULT_CANCELED);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setColorFilter(Color.rgb(Red, Green, Blue));
        editText = (EditText) findViewById(R.id.editText);
        editText.setText(Name);
        SeekBar seekBarRed = (SeekBar) findViewById(R.id.seekBar);
        seekBarRed.setProgress(Red);
        seekBarRed.setProgressTintList(ColorStateList.valueOf(Color.RED));
        seekBarRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Red = i;
                imageView.setColorFilter(Color.rgb(Red, Green, Blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar seekBarGreen = (SeekBar) findViewById(R.id.seekBar2);
        seekBarGreen.setProgress(Green);
        seekBarGreen.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        seekBarGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Green = i;
                imageView.setColorFilter(Color.rgb(Red, Green, Blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar seekBarBlue = (SeekBar) findViewById(R.id.seekBar3);
        seekBarBlue.setProgress(Blue);
        seekBarBlue.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        seekBarBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Blue = i;
                imageView.setColorFilter(Color.rgb(Red, Green, Blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Name = editText.getText().toString();
                Intent intent = new Intent();
                intent.putExtra("ID", Id);
                intent.putExtra("NAME", Name);
                intent.putExtra("RED", Red);
                intent.putExtra("GREEN", Green);
                intent.putExtra("BLUE", Blue);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
