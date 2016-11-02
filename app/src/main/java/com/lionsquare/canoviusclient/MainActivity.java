package com.lionsquare.canoviusclient;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.lionsquare.canoviusclient.DB.SQLite;
import com.lionsquare.canoviusclient.Network.Network;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //keytool -list -v -keystore /Users/archivaldo/AndroidStudioProjects/Proyectos/llaves/lionsquare.jks
    Button login;
    private ProgressDialog pDialog;
    int primeraVez = 0;
    EditText user, pass;
    String id;
    private SQLite db;
    HashMap<String, String> cursor;
    Boolean sessionActivity;
    String lat, lon, model, direccionText, distancia;
    JSONParser jsonParser = new JSONParser();
    LocationManager locManager;
    LocationListener locListener;
    android.location.Location localizacion;
    DialogAviso dialogAviso;
    android.support.v7.app.AlertDialog.Builder builder;
    private static final String LOGIN_URL = "http://canovius.16mb.com/canovius/login/sync_seguridad.php";
    private static final String LOGIN_SYNC = "http://canovius.16mb.com/canovius/login/sincro.php";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_ID = "id";
    private static final int REQUEST_EXTERNAL_STORAGE_RESULT = 0;
    String ID_ANDROID;
    private AdView adView;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

        } else {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(MainActivity.this, "Canovius  necesita permisos", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_STORAGE_RESULT);
        }

        user = (EditText) findViewById(R.id.user);
        SharedPreferences preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        String correo = preferences.getString("user", "");
        primeraVez = preferences.getInt("primera", 0);
        user.setText(correo);
        db = new SQLite(getApplicationContext());

        pass = (EditText) findViewById(R.id.pass);
        login = (Button) findViewById(R.id.login);


        login.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("user", user.getText().toString());
                        editor.commit();
                        if (Network.networkAvailable(MainActivity.this)) {
                            new AttemptLogin().execute();

                        } else {
                            alert();
                        }

                    }
                });

        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-5060471640712079/4251221545");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout layout = (LinearLayout) findViewById(R.id.login_banner);
        layout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if (primeraVez == 0) {
            dialogAviso = new DialogAviso();
            dialogAviso.show(getFragmentManager(), "miDialog");

        }

        ID_ANDROID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //  toolbar.setBackground(ContextCompat.getDrawable(this, R.drawable.divice));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // toolbar.setBackground(getResources().getDrawable(R.mipmap.ic_launcher));
        } else {

        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cursor = db.getUserDetails();

        if (!cursor.isEmpty()) {
            pDialog = new ProgressDialog(this);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage(getString(R.string.msg_progress_wait));
            pDialog.setCancelable(false);
            id = cursor.get("idUser");
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor edit = sp.edit();
            edit.commit();
            Intent intent = new Intent(MainActivity.this, Location.class);
            intent.putExtra(TAG_ID, id);
            startActivity(intent);
            overridePendingTransition(R.anim.zoom_entrada, R.anim.zoom_salida);
            finish();
        }

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        localizacion = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        if (localizacion != null) {

        } else {

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        if (id == R.id.gps) {

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.lionsquare.canisovismanager")));
            return true;
        }
        if (id == R.id.salir) {

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            alertGPS();
        }


        super.onStart();


    }

    @Override
    protected void onPause() {

        //locManager.removeUpdates(locListener);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }


    class AttemptLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Comprobando sus datos ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;
            String username = user.getText().toString();
            String password = pass.getText().toString();
            try {
                List params = new ArrayList();
                params.add(new BasicNameValuePair("username", username));
                params.add(new BasicNameValuePair("password", password));


                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
                        params);

                id = json.getString(TAG_ID);
                sessionActivity = true;

                success = json.getInt(TAG_SUCCESS);

                if (localizacion != null) {
                    lat = String.valueOf(localizacion.getLatitude());
                    lon = String.valueOf(localizacion.getLongitude());
                    distancia = String.valueOf(localizacion.getAccuracy());
                }


                //sincro
                if (success == 1) {
                    //syncro
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String currentDateandTime = sdf.format(new Date());
                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String emei = mngr.getDeviceId();
                    if (emei == null) {
                        emei = ID_ANDROID;
                    }
                    model = Build.MODEL;
                    List paramsLoc = new ArrayList();
                    paramsLoc.add(new BasicNameValuePair("emei", emei));
                    paramsLoc.add(new BasicNameValuePair("id", id));
                    paramsLoc.add(new BasicNameValuePair("lat", lat));
                    paramsLoc.add(new BasicNameValuePair("lon", lon));
                    paramsLoc.add(new BasicNameValuePair("model", model));
                    paramsLoc.add(new BasicNameValuePair("fecha", currentDateandTime));
                    JSONObject jsonId = jsonParser.makeHttpRequest(LOGIN_SYNC, "POST", paramsLoc);
                    db.addUser(username, id, password, emei, sessionActivity);

                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    List<Address> direcciones = null;
                    try {
                        direcciones = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 2);
                    } catch (Exception e) {
                        // Log.d("Error", "Error en geocoder:"+e.toString());
                    }
                    if (direcciones != null && direcciones.size() > 0) {

                        // Creamos el objeto address
                        Address direccion = direcciones.get(0);

                        // Creamos el string a partir del elemento direccion
                        direccionText = String.format("%s, %s, %s",
                                direccion.getMaxAddressLineIndex() > 0 ? direccion.getAddressLine(0) : "",
                                direccion.getLocality(),
                                direccion.getCountryName());
                    }

                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putString("username", username);
                    edit.commit();

                    Intent i = new Intent(MainActivity.this, Location.class);
                    //mostrarNotificacion();
                    finish();
                    startActivity(i);
                    return json.getString(TAG_MESSAGE);
                } else {

                    return json.getString(TAG_MESSAGE);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                String err = (e.getMessage() == null) ? "" : e.getMessage();
                return "No se puede acceder al servidor; intentelo nuevamente";
            }

            return null;

        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(MainActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void mostrarNotificacion() {
        int notificationId = 1;
        long[] pattern = new long[]{0, 500, 1000};
        NotificationCompat.Builder notificationCompat = new NotificationCompat.Builder(MainActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.string_titulo_notificacion))
                .setContentText(getText(R.string.string_mensaje_notificacion));

        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(MainActivity.this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//sonido en la notificacion
        notificationCompat.setSound(defaultSound);
        notificationCompat.setVibrate(pattern);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationCompat.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationCompat.setAutoCancel(true);
        notificationManager.notify(notificationId, notificationCompat.build());

    }


    private void alert() {

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(R.string.msg_progress_alert);
        alertDialog.setMessage(getString(R.string.msg_sin_intenet));
        alertDialog.setButton(getString(R.string.msg_si), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // aquí puedes añadir funciones
            }
        });
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.show();
    }


    private void alertGPS() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder
                .setMessage(R.string.gps_msg_deshabilitar)
                .setCancelable(false)
                .setPositiveButton(R.string.gps_msg_habilitar,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        });
        alertDialogBuilder.setNegativeButton(R.string.msg_no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @SuppressLint("ValidFragment")
    public class DialogAviso extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_aviso, null);
            builder.setView(view);

            Button btn_ok = (Button) view.findViewById(R.id.cuenta);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences preferences = getSharedPreferences("login", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("primera", 1);
                    editor.commit();
                    dismiss();

                }
            });
            Button btnt_sin_registro = (Button) view.findViewById(R.id.registroSin);
            btnt_sin_registro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.lionsquare.canisovismanager")));
                    dismiss();
                }
            });


            return builder.create();


        }


    }


}
