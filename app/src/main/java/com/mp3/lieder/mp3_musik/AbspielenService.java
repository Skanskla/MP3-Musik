package com.mp3.lieder.mp3_musik;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

public class AbspielenService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList <Lied> lieder;
    private int posLieder;
    private final IBinder musB= new MusikBinder();

    public void onCreate(){
        super.onCreate();
        posLieder=0;
        player=new MediaPlayer();
        initPlayer();
    }
    public void initPlayer(){
        player.setWakeMode(getApplicationContext(), PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    public void setzeListe(ArrayList<Lied> dieLieder){
        lieder=dieLieder;
    }

    public class MusikBinder extends Binder{
        AbspielenService kriegeService(){
            return AbspielenService.this;
        }
    }
    public AbspielenService() {
    }

    public void spieleLied(){
        player.reset();
        Lied abgesLied = lieder.get(posLieder);
        long aktLied = abgesLied.getId();
        Uri track = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, aktLied);
        try{
            player.setDataSource(getApplicationContext(), track);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Fehler", e);
        }
        player.prepareAsync();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return musB;
    }
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    mp.start();
    }

    public void waehleLied(int liedIndex){
        posLieder=liedIndex;
    }



}
