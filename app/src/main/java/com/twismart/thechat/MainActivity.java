package com.twismart.thechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainAcivity";
    private static final int PERMISSION_GPS=2;

    private PreferencesProfile preferencesProfile;
    private PreferencesFind preferencesFind;

    private LocationManager locationManager = null;
    private MyLocationListener myLocationListener;

    private NetworkInteractor networkInteractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferencesProfile = new PreferencesProfile(this);
        preferencesFind = new PreferencesFind(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(viewPager);

        preferencesProfile.setLogged(true);

        networkInteractor = new NetworkInteractor(this);
        networkInteractor.writeStatus(Constantes.Status.ONLINE.name());
        networkInteractor.writeTokenIdFirebase(FirebaseInstanceId.getInstance().getToken(), null);

        if(hasGPS()){
            gestionarPermiso(android.Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_GPS);
        }
    }


    public boolean hasGPS(){
        return (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION));
    }

    public void gestionarPermiso(String manifestPermission, int PERMISSION_REQUEST){
        if (ContextCompat.checkSelfPermission(this, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, manifestPermission)){
                ActivityCompat.requestPermissions(this, new String[]{manifestPermission, manifestPermission}, PERMISSION_REQUEST);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{manifestPermission, manifestPermission}, PERMISSION_REQUEST);
            }
        }
        else {
            activarGPS();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode){
            case PERMISSION_GPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    activarGPS();
                }
        }
    }

    public void activarGPS(){
        try {
            Log.d(TAG,"activarGPS");
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            myLocationListener = new MyLocationListener();

            Criteria criteria = new Criteria();

            locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria, true), 3000, 0, myLocationListener);
        } catch (SecurityException e) {
            Log.e(TAG,"PERMISSION_NOT_GRANTED");
        } catch (Exception e) {
            Log.e(TAG,"catch activarGPS " + e.getMessage());
        }
    }

    public void desactivarGPS(){
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(myLocationListener);
                locationManager = null;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "PERMISSION_NOT_GRANTED");
        } catch (Exception e) {
            Log.e(TAG,"catch desactivarGPS " + e.getMessage());
        }
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            networkInteractor.writeLocationInPerfil(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "onLocationChanged");
            desactivarGPS();
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onProviderDisabled(String provider) {
        }
    }

    public void onDestroy(){
        super.onDestroy();
        networkInteractor.writeStatus(Constantes.Status.OFFLINE.name());
        if(locationManager != null){
            desactivarGPS();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            final ProgressDialog mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.login_message_progress_load));
            mProgressDialog.show();
            networkInteractor.writeStatus(Constantes.Status.OFFLINE.name());
            networkInteractor.writeTokenIdFirebase(null, new NetworkInteractor.IWriteTokenIdFirebase() {
                @Override
                public void onSucces() {
                    preferencesProfile.clear();
                    preferencesFind.clear();
                    mProgressDialog.cancel();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String error) {

                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
