package com.mp3.lieder.mp3_musik;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mp3.lieder.mp3_musik.MainActivity;
import java.util.ArrayList;

public class Interpreten extends AppCompatActivity {

    ListView listViewInterpreten;
    private ArrayList<Lied> liedListe;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interpreten);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

       findeLieder();

        listViewInterpreten=(ListView)findViewById(R.id.listview_interpreten);

        InterpretenAdapter AnzAdap = new InterpretenAdapter(liedListe,this);
        listViewInterpreten.setAdapter(AnzAdap);

    }

    public void findeLieder() {
        liedListe = new ArrayList<Lied>();

        ContentResolver musicResolver = this.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor LiedCursor = musicResolver.query(musicUri, null, null, null, null);

        if (LiedCursor != null && LiedCursor.moveToFirst()) {
            int titelcol = LiedCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idcol = LiedCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int interpretcol = LiedCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            do {

                long konId = LiedCursor.getLong(idcol);
                String konTitel = LiedCursor.getString(titelcol);
                String konInterpret = LiedCursor.getString(interpretcol);

                liedListe.add(new Lied(konId, konTitel, konInterpret));

            }
            while (LiedCursor.moveToNext());
        }
    }

    public void filterLiederzuInterpret(){


        Toast.makeText(this, liedListe.get(0).getInterpret(),Toast.LENGTH_LONG);
    }

}
