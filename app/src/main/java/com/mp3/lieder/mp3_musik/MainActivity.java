package com.mp3.lieder.mp3_musik;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.mp3.lieder.mp3_musik.AbspielenService.MusikBinder;

import static android.Manifest.permission_group.STORAGE;

public class MainActivity extends AppCompatActivity {

    ListView listViewTitel;
    private ArrayList<Lied> liedListe;

    private AbspielenService absService;
    private Intent absIntent;
    private boolean musikB=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewTitel = findViewById(R.id.lieder_liste);
        liedListe= new ArrayList<Lied>();
        findeLieder();

        Collections.sort(liedListe, new Comparator<Lied>(){
            public int compare(Lied a, Lied b){
                return a.getTitel().compareTo(b.getTitel());
            }
        });
        LiedAdapter AnzAdap = new LiedAdapter(liedListe,this);
        listViewTitel.setAdapter(AnzAdap);

    }

    private ServiceConnection musikCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AbspielenService.MusikBinder bin = (AbspielenService.MusikBinder)service;
            absService=bin.kriegeService();
            absService.setzeListe(liedListe);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };



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

    @Override
    protected void onStart() {
        super.onStart();
        if(absIntent==null){
            absIntent = new Intent(this, AbspielenService.class);
            bindService(absIntent, musikCon, Context.BIND_AUTO_CREATE);
            startService(absIntent);
        }
    }
    public void gewLied(View view){
        absService.waehleLied(Integer.parseInt(view.getTag().toString()));
        absService.spieleLied();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shuffle:
                //shuffle
                break;
            case R.id.stop:
                stopService(absIntent);
                absService=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(absIntent);
        absService=null;
        super.onDestroy();
    }
}
