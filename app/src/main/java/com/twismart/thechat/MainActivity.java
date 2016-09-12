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
import android.view.View;
import android.widget.Toast;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity implements AdListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_GPS = 2;

    private PreferencesProfile preferencesProfile;
    private PreferencesFind preferencesFind;

    private LocationManager locationManager = null;
    private MyLocationListener myLocationListener;

    private NetworkInteractor networkInteractor;

    private com.amazon.device.ads.AdLayout amazonAdView;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.amazon.device.ads.AdRegistration.setAppKey(Constantes.ADS_AMAZON);
        MobileAds.initialize(getApplicationContext(), Constantes.ID_ADMOB);

        showBanners();

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
            managePermission(android.Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_GPS);
        }
    }

    private void showBanners() {
        try {
            amazonAdView = (com.amazon.device.ads.AdLayout) findViewById(R.id.amazonAd);
            amazonAdView.setListener(this);
            amazonAdView.loadAd();

            mAdView = (AdView) findViewById(R.id.admobAd);
        }
        catch (Exception e) {
            Log.d("Error ", "en registrarAdsAmazon");
        }
    }

    //listener od ads amazon
    @Override
    public void onAdLoaded(Ad ad, AdProperties adProperties) {
        Log.d(TAG, "onAdLoadddddd");
    }

    @Override
    public void onAdFailedToLoad(Ad ad, AdError adError) {
        Log.e(TAG, "onAdFailedToloaddd " + adError.getMessage());
        amazonAdView.destroy();
        amazonAdView.setVisibility(View.GONE);

        mAdView.setVisibility(View.VISIBLE);
        mAdView.loadAd(Constantes.getAdRequest());
        mAdView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "adgoogle error " + errorCode);
                mAdView.setVisibility(View.GONE);
            }
        });
    }
    @Override
    public void onAdExpanded(Ad ad) {
    }
    @Override
    public void onAdCollapsed(Ad ad) {
    }
    @Override
    public void onAdDismissed(Ad ad) {
    }


    public boolean hasGPS(){
        return (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION));
    }

    public void managePermission(String manifestPermission, int PERMISSION_REQUEST){
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
        try {
            amazonAdView.destroy();
        } catch (Exception e) {
            Log.e(TAG, "Error en amazon.onDestroy");
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
            mProgressDialog.setMessage(getString(R.string.main_message_progress_signout));
            mProgressDialog.show();
            networkInteractor.writeStatus(Constantes.Status.OFFLINE.name());
            networkInteractor.writeTokenIdFirebase(null, new NetworkInteractor.IWriteTokenIdFirebase() {
                @Override
                public void onSucces() {
                    preferencesProfile.clear();
                    preferencesFind.clear();
                    LocalDataBase dataBase = new LocalDataBase(getApplicationContext());
                    dataBase.clear();
                    mProgressDialog.cancel();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    mProgressDialog.cancel();
                    try {
                        Toast.makeText(getBaseContext(), R.string.main_text_not_logout, Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e){
                        Log.e(TAG, "catch onFailure writeTokenIdFirebase " + e.getMessage());
                    }
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
