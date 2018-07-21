package com.mp3.lieder.mp3_musik;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import com.mp3.lieder.mp3_musik.AbspielenService.MusikBinder;
import android.widget.MediaController.MediaPlayerControl;
import static android.Manifest.permission_group.STORAGE;
import com.mp3.lieder.mp3_musik.R;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    private Toolbar toolbar;

    ListView listViewTitel;
    private ArrayList<Lied> liedListe;
    private Button playbtn;
    private int lied_pos;



    private AbspielenService absService;
    private Intent absIntent;
    private boolean musikB=false;

    private MusikSteuerung steuerung;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        listViewTitel = findViewById(R.id.lieder_liste);
        liedListe= new ArrayList<Lied>();



        findeLieder();
        setzeKontroller();

        Collections.sort(liedListe, new Comparator<Lied>(){
            public int compare(Lied a, Lied b){
                return a.getTitel().compareTo(b.getTitel());
            }
        });
        LiedAdapter AnzAdap = new LiedAdapter(liedListe,this);
        listViewTitel.setAdapter(AnzAdap);

        playbtn=findViewById(R.id.Play);
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(absService.spielt()){
                    absService.pause();
                    playbtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.play));
                }else {
                    if(absService.getPos()==lied_pos){
                        absService.los();
                        playbtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.pause));
                    }else if(absService.getPos()!=lied_pos){
                        absService.waehleLied(lied_pos);
                        absService.spieleLied();
                        playbtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.pause));
                    }
                    playbtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.pause));
                    //absService.waehleLied(6);
                    //absService.spieleLied();

                }

            }
        });


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
        lied_pos=Integer.parseInt(view.getTag().toString());
        playbtn.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.pause));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.shuffle:
                absService.setzeShuffle();
                break;
            case R.id.stop:
                stopService(absIntent);
                absService=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setzeKontroller(){
        steuerung=new MusikSteuerung(this);
        steuerung.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spieleNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spielePrev();
            }
        });
        steuerung.setMediaPlayer(this);
        steuerung.setAnchorView(findViewById(R.id.lieder_liste));
        steuerung.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        unbindService(musikCon);
        stopService(absIntent);
        absService=null;
        super.onDestroy();
    }

    @Override
    public void start() {
    absService.los();
    }

    @Override
    public void pause() {
        absService.pause();
    }

    @Override
    public int getDuration() {
        if(absService!=null && musikB && absService.spielt())
        return absService.getDauer();
        else return 0;
    }


    @Override
    public int getCurrentPosition() {
        if ( absService != null && musikB && absService.spielt())
        return absService.getPos();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
    absService.bar(pos);
    }


    @Override
    public boolean isPlaying() {
        if(absService!=null && musikB)
        return absService.spielt();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    //play next
    private void spieleNext(){
        absService.spieleNext();
        steuerung.show(0);
    }

    //play previous
    private void spielePrev(){
        absService.spielePrev();
        steuerung.show(0);
    }


}
