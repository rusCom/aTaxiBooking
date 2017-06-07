package org.toptaxi.ataxibooking.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainActivity;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.tools.DOTResponse;


public class SplashActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static String TAG = "#########" + SplashActivity.class.getName();
    AsyncTask curTask;
    boolean isGooglePlayConnect, isLocationEnabled;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_splash);
        ((TextView)findViewById(R.id.tvSplashVersion)).setText(MainApplication.getInstance().getVersionName());

        isGooglePlayConnect = false;
        isLocationEnabled = false;

        if (!isNetworkAvailable(getApplicationContext())){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для работы программы необходимо подключение к интернет. Проверьте подключение и попробуйте еще раз.");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
            alertDialog.create();
            alertDialog.show();
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                checkPermission();
            }
            else {
                init();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (curTask != null){
            curTask.cancel(true);
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_LOGIN_PHONE){
            if (resultCode == RESULT_CANCELED)finish();
            else init();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        //Log.d(TAG, "isNetworkAvailable");
        int[] networkTypes = {ConnectivityManager.TYPE_MOBILE,
                ConnectivityManager.TYPE_WIFI};
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            for (int networkType : networkTypes) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null &&
                        activeNetworkInfo.getType() == networkType)
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public void init(){
        //Log.d(TAG, "init MainApplication.getInstance().getAccount().getToken() = " + MainApplication.getInstance().getAccount().getToken());

        if (MainApplication.getInstance().getAccount().getToken().equals("")){
            Intent loginIntent = new Intent(SplashActivity.this, LoginPhoneActivity.class);
            startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN_PHONE);
        }
        else {
            // подключаемся к сервису playMarket
            if (!isGooglePlayConnect){
                // Create an instance of GoogleAPIClient.
                if (mGoogleApiClient == null) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .addApi(LocationServices.API)
                            .addApi(Places.PLACE_DETECTION_API)
                            .addApi(Places.GEO_DATA_API)
                            .build();
                    mGoogleApiClient.connect();
                }

            }
            // Если подключение к плей сервсиу уже есть
            else {
                //startMainActivity();
                new GetPreferencesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }

    }

    private class GetPreferencesTask extends AsyncTask<Void, Void, DOTResponse>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            curTask = this;
            //((TextView)findViewById(R.id.tvSplashAction)).setText(getString(R.string.tvSplashActionConnect));
        }

        @Override
        protected DOTResponse doInBackground(Void... voids) {
            return MainApplication.getInstance().getnDot().preferences();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finish();
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (isCancelled()){
                finish();
            }
            if (result.getCode() == 200){
                try {
                    MainApplication.getInstance().parseData(new JSONObject(result.getBody()));
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            curTask = this;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return MainApplication.getInstance().getDOT().getData();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            finish();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (isCancelled()){
                finish();
            }
            if (result == Constants.DOT_REST_OK){
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                MainApplication.getInstance().showToastType(result);
                finish();
            }
        }
    }



    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else{
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
        else {init();}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_LOCATION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    init();
                }
            }
            else
            {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this, "Для продолжения работы необходимо дать разрешение на доступ к GPS данным", Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isGooglePlayConnect = true;
        MainApplication.getInstance().setGoogleApiClient(mGoogleApiClient);
        //Log.d(TAG, "onConnected");
        init();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для корректной работы приложения необходимо обновить Сервисы Google Play");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
                    startActivity(intent);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Для корректной работы приложения необходимо установить Сервисы Google Play");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.google.android.gms"));
                    startActivity(intent);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(connectionResult.getErrorCode() + " " + connectionResult.getErrorMessage());
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {finish();}
            });
            alertDialog.create();
            alertDialog.show();

        }

    }
}
