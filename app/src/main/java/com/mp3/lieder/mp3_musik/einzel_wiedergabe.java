package com.mp3.lieder.mp3_musik;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class einzel_wiedergabe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_einzel_wiedergabe);
        //Initialisierung der vertikalen VolumeBar mit custom Seekbar
        final VerticalSeekBar vertical_volume_bar = findViewById (R.id.volume_seekbar);
        vertical_volume_bar.setMax(10);

    }
}
