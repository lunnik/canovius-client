package com.lionsquare.canoviusclient;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.lionsquare.canoviusclient.servicios.AutoSync;

public class ActivitySettings extends AppCompatActivity {
    Boolean autoSync=false;
    Boolean statusService = false;
    SharedPreferences pref;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         preferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        statusService  = preferences.getBoolean("noticias", false);
        /*setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
         pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();//se incia la muetsra de framento
        PreferencesFragment preferencesFragment = new PreferencesFragment();
        fragmentTransaction.replace(android.R.id.content, preferencesFragment);//con esto se meuestra en la pantalla
        //replace(metodo) remplza lo que esta ne la pantalla y se muetsra el fragmento
        fragmentTransaction.commit();//para que los cambios persista se usa el comit
    }


    public static class PreferencesFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_preferences);
        }
    }

    @Override
    protected void onPause() {

            statusService = preferences.getBoolean("noticias", false);
            SharedPreferences preferences = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            autoSync = pref.getBoolean("applicatioSync", autoSync);
            if (autoSync == true && statusService == false) {

                //statusService= true;//meterer en un shared prefrence
                editor.putBoolean("noticias", true);
                editor.commit();
                Intent intent = new Intent(ActivitySettings.this, AutoSync.class);
                startService(intent);
                Toast.makeText(this, "Autosincronización Activada", Toast.LENGTH_SHORT).show();
            } else if (autoSync == false) {
                Toast.makeText(this, "Autosincronización Desactivada", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ActivitySettings.this, AutoSync.class);
                stopService(intent);
                // statusService= false;
                editor.putBoolean("noticias", false);
                editor.commit();
            }

        super.onPause();
    }
}
