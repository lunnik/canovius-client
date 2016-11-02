package com.lionsquare.canoviusclient.servicios;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.lionsquare.canoviusclient.DB.SQLite;
import com.lionsquare.canoviusclient.JSONParser;
import com.lionsquare.canoviusclient.MainActivity;
import com.lionsquare.canoviusclient.Network.Network;
import com.lionsquare.canoviusclient.R;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AutoSync extends Service {
    private int timeSync=3600000;
    Boolean autoSync=false;
    private SQLite db;
    HashMap<String ,String> cursor;
    AsyncTaskHora asyncTaskHora;
    String lat, lon,message;
    LocationManager locManager;
    JSONParser jsonParser = new JSONParser();
    android.location.Location localizacion;
    LocationListener locListener;
    private static final String AUTO_SYNC = "http://canovius.16mb.com/canovius/login/auto_sync.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int time = 0;
        String  times = "0";
        times= pref.getString("autosincronización", times);
        autoSync= pref.getBoolean("applicatioSync", autoSync);
        time = Integer.parseInt(times);
        if(autoSync=true){
            if(time==1){ timeSync=3600000;}
            if(time==3){ timeSync=10800000;}
            if(time==6){ timeSync=21600000;}
            if(time==12){ timeSync=43200000;}
            if(time==24){ timeSync=86400000;}
        }

        //Toast.makeText(this, "Autosincronización Iniciada", Toast.LENGTH_SHORT).show();
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        localizacion = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        db = new SQLite(getApplicationContext());
        cursor=db.getUserDetails();
        if (localizacion != null) {

            lat = String.valueOf(localizacion.getLatitude());
            lon = String.valueOf(localizacion.getLongitude());

        } else {
            Toast.makeText(getApplication(), "Debes activar el gps ", Toast.LENGTH_LONG).show();
        }

         locListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLocationChanged(android.location.Location localizacion) {
                lat = String.valueOf(localizacion.getLatitude());
                lon = String.valueOf(localizacion.getLongitude());
            }
        };

        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locListener);

        asyncTaskHora = new AsyncTaskHora();
        locManager.removeUpdates(locListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(Network.networkAvailable(getApplication())){

            asyncTaskHora.execute();

        }else{

            Toast.makeText(getApplication(), "Sin acceso a internet", Toast.LENGTH_LONG).show();
        }

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "Auto-sincronizacion Detenia", Toast.LENGTH_SHORT).show();
        asyncTaskHora.cancel(true);
    }

    private class AsyncTaskHora extends AsyncTask<String, String, String>{

        private DateFormat dateFormat;
        private String hora;
        private boolean mostrando;

        @Override
        protected String doInBackground(String... params) {
            while (mostrando){
                hora = dateFormat.format(new Date());
                int success;

                cursor=db.getUserDetails();
                cursor.get("emei");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String currentDateandTime = sdf.format(new Date());
                try{
                    publishProgress(hora);

                    List locationData = new ArrayList();
                    locationData.add(new BasicNameValuePair("lat", lat));
                    locationData.add(new BasicNameValuePair("lon", lon));
                    locationData.add(new BasicNameValuePair("emei", cursor.get("emei")));
                    locationData.add(new BasicNameValuePair("fecha", currentDateandTime));
                    JSONObject json = jsonParser.makeHttpRequest(AUTO_SYNC, "POST",
                            locationData);
                    success=json.getInt(TAG_SUCCESS);

                    message=json.getString(TAG_MESSAGE);

                    Thread.sleep(timeSync);
                    //return message;// prueba
                    //10000 = a diez segundos 3600000 un hora
                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (Exception e) {
                    String err = (e.getMessage()==null) ? "" : e.getMessage();
                    //mostrarNotificacion();
                    return null;
                }
            }

            return  message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dateFormat = new SimpleDateFormat("HH:mm:ss");
            mostrando = true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            locManager.removeUpdates(locListener);
            //Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mostrando = false;
        }
    }


    private void mostrarNotificacion(){
        int notificationId = 1;
        long[] pattern = new long[]{0,500,1000};
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.sin_acceso))
                .setContentText(getText(R.string.sin_servidor));

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getApplicationContext());
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//sonido en la notificacion
        notificationCompat.setSound(defaultSound);
        notificationCompat.setVibrate(pattern);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationCompat.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationCompat.setAutoCancel(true);
        notificationManager.notify(notificationId, notificationCompat.build());

    }
}
