package com.mp3.lieder.mp3_musik;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

//diese Klasse dient zur Anzeige von Informationen in der ListView
public class LiedAdapter extends BaseAdapter {
    private ArrayList <Lied> lieder;
    private LayoutInflater liedinf;

    public LiedAdapter(ArrayList <Lied> meineLieder, Context k){
        lieder=meineLieder;
        liedinf=LayoutInflater.from(k);
    }

    @Override
    public int getCount() {
        return lieder.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
// die folgende Methode zeigt alle Lieder in der Listview an. Wird in Tab1_Titel aufgerufen
    // hier werden nur Titel angezeigt
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout lay = (LinearLayout) liedinf.inflate(R.layout.lied, parent,false);



        TextView titelV = lay.findViewById(R.id.sec_titel);
        TextView interpretV= lay.findViewById(R.id.sec_interpret);

        Lied akt = lieder.get(position);


        titelV.setText(akt.getTitel());
        interpretV.setText(akt.getInterpret());

        lay.setTag(position);

        return lay;
    }
}
