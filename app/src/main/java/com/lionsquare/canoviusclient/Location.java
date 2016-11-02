package com.lionsquare.canoviusclient;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lionsquare.canoviusclient.DB.SQLite;
import com.lionsquare.canoviusclient.servicios.AutoSync;

import java.util.List;

public class Location extends AppCompatActivity {
    private GoogleMap googleMap;
    private ImageView im1, im2;
    private SQLite db;
    double lat;
    double lon;
    private CoordinatorLayout coordinatorLayout;
    LocationManager locationManager;
    LocationListener locationListener;
    TextView txt_lat,txt_lon,txt_distancia;
    String  emei, model,direccionText,currentDateandTime,distancia;
    private AdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        txt_distancia= (TextView)findViewById(R.id.distancia);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        txt_lat=(TextView)findViewById(R.id.lat);
        txt_lon=(TextView)findViewById(R.id.lon);
        db = new SQLite(getApplicationContext());
        Snackbar();
        int codigoGooglePlay = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);//numero unico de google play
        if (codigoGooglePlay != ConnectionResult.SUCCESS) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(codigoGooglePlay, this, 6);
            if (dialog != null) {
                dialog.show();
            } else {
                Toast.makeText(Location.this,
                        "Error al verificar Google Play Servivces",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }

        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-5060471640712079/5727954745");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout layout =(LinearLayout)findViewById(R.id.maps_banner);
        layout.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-5060471640712079/1158154344");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout layout1 =(LinearLayout)findViewById(R.id.maps_banner2);
        layout1.addView(adView);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        adView.loadAd(adRequest1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("TITLE");
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


        FragmentManager myFragmentManager = getSupportFragmentManager();
        SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
        googleMap = mySupportMapFragment.getMap();

        if (googleMap != null) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);// tipo de mapa que se av mostrar
            googleMap.setMyLocationEnabled(false);// muestra el boton de localizacion
        }



        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//obtnemos lat y log
        android.location.Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (loc != null) {
            showLocation(loc.getLatitude(), loc.getLongitude());
             lat= loc.getLatitude();
             lon= loc.getLongitude();
            distancia = String.valueOf(loc.getAccuracy());

            Geocoder geocoder = new Geocoder(Location.this);
            List<Address> direcciones = null;
            try {
                direcciones = geocoder.getFromLocation(loc.getLatitude() , loc.getLongitude(),2);
            } catch (Exception e) {
                //Log.d("Error", "Error en geocoder:" + e.toString());
            }
            if(direcciones != null && direcciones.size() > 0 ){

                // Creamos el objeto address
                Address direccion = direcciones.get(0);

                // Creamos el string a partir del elemento direccion
                direccionText = String.format("%s, %s, %s",
                        direccion.getMaxAddressLineIndex() > 0 ? direccion.getAddressLine(0) : "",
                        direccion.getLocality(),
                        direccion.getCountryName());
            }

        }

        FloatingActionButton imgbtn = (FloatingActionButton)findViewById(R.id.fab); //your button
        imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                        LatLng(lat, lon), 15));
            }
        });

        LocationListener locationListener = new LocationListener() {
            //puede mover la camara del mapa cuando cabie de ubicacion
            //y se actuliza la ubicacion si se meueve
            public void onLocationChanged(android.location.Location location) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

                //mueve el foco en la pantalla cuando se muesta el mapa
                googleMap.moveCamera(center);//mueve la panatala al la marca
                showLocation(location.getLatitude(), location.getLongitude());

                txt_lat.setText(Double.toString(location.getLatitude()));
                txt_lon.setText(Double.toString(location.getLongitude()));
                txt_distancia.setText(distancia+" "+"metros");

            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        Criteria criteria = new Criteria();//permiete definir criterios de ubicacion para encontra de manera
        // mas precisa el dispositovo
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// que tal alto es el criterio de ubicacion
        criteria.setAltitudeRequired(true);//la ubicaion es mas precisa aun
        criteria.setCostAllowed(true);//le dice a la app que no debe usar el 3g pro el consumo de datos
        criteria.setSpeedRequired(true);//velocidad de en la que se debe encontrar
        String provider = locationManager.getBestProvider(criteria, true);//obteer ek mejro provedor disponible
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 1000, 70, locationListener);//se muetsra una  nueva hubicacion
        }


        im1 = (ImageView) findViewById(R.id.imageViewDrawer1);
        im2 = (ImageView) findViewById(R.id.imageViewDrawer2);

        SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                im1.setImageResource(R.drawable.up);
                im2.setImageResource(R.drawable.up);
            }
        });
        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                im1.setImageResource(R.drawable.down);
                im2.setImageResource(R.drawable.down);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, ActivitySettings.class);
            startActivity(i);
            return true;
        }

        if(id ==R.id.cerrar){
            db.deleteUsers();
            SharedPreferences settings = this.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
            settings.edit().clear().commit();
            //detener auto sincronizacion
            Intent intent = new Intent(Location.this, AutoSync.class);
            stopService(intent);
            finish();
            Intent i = new Intent(Location.this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.zoom_entrada, R.anim.zoom_salida);
        }
        if (id == R.id.salir) {
            finish();
            return true;
        }



        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onPause() {
       // locationManager.removeUpdates(locationListener);
        super.onPause();
    }

    private void Snackbar(){

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Aprende mas sobre la Auto-sincronizacion-", 150000)
                .setAction("Ajustes", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getApplicationContext(), ActivitySettings.class);
                        startActivity(i);

                    }
                });

        snackbar.show();
    }

    private void showLocation(double lat, double lng) {
        googleMap.addMarker(new MarkerOptions()//SIMBOLO DE GOTA INVERTIDA (MARCADOR )
                .position(new LatLng(lat, lng))
                .snippet(direccionText)
                .title("Mi ubicación..."));
    }
}
