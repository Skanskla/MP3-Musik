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
import java.util.Random;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

public class AbspielenService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList <Lied> lieder;
    private int posLieder;
    private final IBinder musB= new MusikBinder();
    private boolean shuffle=false;
    private Random rdm;

    public void onCreate(){

        super.onCreate();
        posLieder=0;
        player=new MediaPlayer();
        initPlayer();
        rdm=new Random();
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

    public void spieleLied(){
        player.reset();
        Lied abgesLied = lieder.get(posLieder);
        long aktLied = abgesLied.getId();
        Uri track = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, aktLied);
        try{
            player.setDataSource(getApplicationContext(), track);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
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
        if(player.getCurrentPosition()>0){
            mp.reset();
            spieleNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    mp.start();
    }

    public void waehleLied(int liedIndex){
        posLieder=liedIndex;
    }

    public int getPos(){
        return player.getCurrentPosition();
    }

    public int getDauer(){
        return player.getDuration();
    }

    public boolean spielt(){
        return player.isPlaying();
    }

    public void pause(){
        player.pause();
    }

    public void bar(int pos){
        player.seekTo(pos);
    }

    public void los(){
        player.start();
    }

    public void spielePrev(){
        posLieder--;
        if(posLieder<0) posLieder=lieder.size()-1;
        spieleLied();
    }
    public void spieleNext(){

        if(shuffle){
            int neuLied = posLieder;
            while(neuLied==posLieder){
                neuLied=rdm.nextInt(lieder.size());
            }
            posLieder=neuLied;
        }
        else{
            posLieder++;
            if(posLieder>=lieder.size()) posLieder=0;
        }
        spieleLied();
    }
    public void setzeShuffle(){
        shuffle = !shuffle;
    }
}
