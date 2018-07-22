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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
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
    private Button playbtn, prevbtn;
    private int lied_pos;
    private boolean pausiert=false;



    private AbspielenService absService;
    private Intent absIntent;
    private boolean musikB=false;

    private static MusikSteuerung steuerung;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);



        listViewTitel = findViewById(R.id.lieder_liste);
        liedListe= new ArrayList<Lied>();



        Collections.sort(liedListe, new Comparator<Lied>(){
            public int compare(Lied a, Lied b){
                return a.getTitel().compareTo(b.getTitel());
            }
        });


        angecklicktesLied();
        findeLieder();
        LiedAdapter AnzAdap = new LiedAdapter(liedListe,this);
        listViewTitel.setAdapter(AnzAdap);
        setzeKontroller();

    }


    private void angecklicktesLied() {
        liedListe = new ArrayList<>();
        listViewTitel = (ListView) findViewById(R.id.lieder_liste);


        listViewTitel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                if (pausiert) pausiert = false;

                absService.waehleLied(position);
                absService.spieleLied();
                steuerung.show(0);


            }
        });
    }



    private ServiceConnection musikCon = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusikBinder bin=(MusikBinder) service;
            absService=bin.kriegeService();
            absService.setzeListe(liedListe);
            musikB=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        musikB=false;
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
        if(pausiert){
            setzeKontroller();
            pausiert=false;
        }
        lied_pos=Integer.parseInt(view.getTag().toString());
        steuerung.show(0);



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
        }
        );


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
    steuerung.show(0);
    }
    @Override
    protected void onPause(){
        super.onPause();
        pausiert=true;

    }
    @Override
    public void pause() {
        absService.pause();
        pausiert=true;


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


    @Override
    protected void onResume(){
        super.onResume();
        if(pausiert){

            pausiert=false;
        }
    }
    protected void onStop() {
        steuerung.hide();
        super.onStop();
    }

    //play next
    private void spieleNext(){
        absService.spieleNext();
        if(pausiert){

            pausiert=false;
        }

    }

    //play previous
    private void spielePrev(){
        absService.spielePrev();
        if(pausiert){

            pausiert=false;
        }

    }



}
