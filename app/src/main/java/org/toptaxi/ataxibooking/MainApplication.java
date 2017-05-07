package org.toptaxi.ataxibooking;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.DOT;
import org.toptaxi.ataxibooking.data.Preferences;
import org.toptaxi.ataxibooking.tools.DOTResponse;
import org.toptaxi.ataxibooking.tools.OnMainDataChangeListener;
import org.toptaxi.ataxibooking.data.Account;
import org.toptaxi.ataxibooking.data.Drivers;
import org.toptaxi.ataxibooking.data.Order;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.tools.DBHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainApplication extends Application implements LocationListener {
    protected static String TAG = "#########" + MainApplication.class.getName();
    protected static MainApplication mainApplication;

    public static MainApplication getInstance(){return mainApplication;}

    private OnMainDataChangeListener onMainDataChangeListener;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation, lLocation;
    private LatLng mapLocation;
    private Order mOrder;
    private DOT mDOT;
    private Account mAccount;
    private LocationRequest mLocationRequest;
    private Preferences mPreferences;
    Thread getDataThread;
    private Drivers mDrivers;
    boolean IsLastHaveOrderData = false, IsGetDataThread = true;
    final Handler uiHandler = new Handler(Looper.getMainLooper());
    private MainActivity mainActivity;
    MediaPlayer mpOrderStateAssign, mpOrderStateDriveToClient;
    SQLiteDatabase dataBase;
    int mapViewType = 0;
    private org.toptaxi.ataxibooking.tools.DOT nDot;

    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mpOrderStateAssign = MediaPlayer.create(this, R.raw.assigned);
        mpOrderStateAssign.setLooping(false);

        mpOrderStateDriveToClient = MediaPlayer.create(this, R.raw.horn1);
        mpOrderStateDriveToClient.setLooping(false);

    }

    public SQLiteDatabase getDataBase() {
        if (dataBase == null){
            DBHelper dbHelper = new DBHelper();
            dataBase = dbHelper.getWritableDatabase();
        }
        return dataBase;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate");
        IsGetDataThread = false;
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                mGoogleApiClient.disconnect();
    }

    public int getMapViewType() {
        return mapViewType;
    }

    public void setMapViewType(int mapViewType) {
        this.mapViewType = mapViewType;
    }

    public void playSoundAssign(){mpOrderStateAssign.start();}

    public void playSoundDriveToClient(){mpOrderStateDriveToClient.start();}

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setMapLocation(LatLng mapLocation) {this.mapLocation = mapLocation;}

    public LatLng getMapLocation() {
        return mapLocation;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            //Log.d(TAG, "setGoogleApiClient");
            lLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //Log.d(TAG, "LastLocation = " + lLocation.toString());

        }
    }

    public Preferences getPreferences() {
        if (mPreferences == null)mPreferences = new Preferences();
        return mPreferences;
    }

    public Drivers getDrivers() {
        if (mDrivers == null){
            mDrivers = new Drivers();
        }
        return mDrivers;
    }

    public DOT getDOT() {
        if (mDOT == null){
            mDOT = new DOT(this);
        }
        return mDOT;
    }

    public org.toptaxi.ataxibooking.tools.DOT getnDot() {
        if (nDot == null){
            nDot = new org.toptaxi.ataxibooking.tools.DOT(this);
        }
        return nDot;
    }

    public Account getAccount() {
        if (mAccount == null){
            SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
            mAccount = new Account(sPref.getString("accountToken", ""));
        }
        return mAccount;
    }

    public Location getLocation() {
        if (mLocation != null) return mLocation;
        if (lLocation != null) return lLocation;
        if (getPreferences().getLocation() != null)return getPreferences().getLocation();
        return null;
    }

    public String getLocationData(){
        String Data = "";
        if (mLocation != null){
            Data += mLocation.getLatitude() + ";";
            Data += mLocation.getLongitude() + ";";
            Data += mLocation.getAccuracy() + ";";
            Data += mLocation.getSpeed() + ";";
            Data += mLocation.getBearing() + ";";
        }
        return Data;
    }



    public Order getOrder() {
        if (mOrder == null){mOrder = new Order();}
        return mOrder;
    }

    public void parseData(JSONObject dataJSON) throws JSONException {
        //Log.d(TAG, "parseData data = " + dataJSON.toString());
        if (dataJSON.has("preferences")){
            getPreferences().setFromJSON(dataJSON.getJSONObject("preferences"));

        }
        if (dataJSON.has("profile")){
            getAccount().setFromJSON(dataJSON.getJSONObject("profile"));
            if (onMainDataChangeListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onMainDataChangeListener.OnAccountDataChange();
                    }
                });
            }
        }
        if (dataJSON.has("order")){getOrder().setFromJSON(dataJSON.getJSONObject("order"));IsLastHaveOrderData = true;}
        else if (IsLastHaveOrderData){
            IsLastHaveOrderData = false;
            getOrder().clear();
            if (onMainDataChangeListener != null) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onMainDataChangeListener.OnOrderDone();
                    }
                });
            }
        }
        if (dataJSON.has("drivers")){getDrivers().setFromJSON(dataJSON.getJSONArray("drivers"));}

        if (onMainDataChangeListener != null){
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    onMainDataChangeListener.OnMainDataChange();
                }
            });

        }

        //if (dataJSON.has("preferences")){getMainPreferences().setFromJSON(dataJSON.getJSONArray("preferences").getJSONObject(0));}

    }

    public static String getRubSymbol(){
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = String.valueOf(Html.fromHtml("&#x20bd", Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
        } else {
            result = String.valueOf(Html.fromHtml("&#x20bd")); // or for older api
        }
        if (result.trim().equals(""))result = "руб.";
        return result;
    }

    public void setOnMainDataChangeListener(OnMainDataChangeListener onMainDataChangeListener) {
        this.onMainDataChangeListener = onMainDataChangeListener;
        if (onMainDataChangeListener != null){
            IsGetDataThread = true;
            if (getDataThread == null){
                GetDataThread mr = new GetDataThread();
                getDataThread = new Thread(mr);
                getDataThread.start();
            }
            else {
                //Log.d(TAG, "setOnMainDataChangeListener getDataThread = " + getDataThread.getState());
                if (getDataThread.getState() == Thread.State.TERMINATED){
                    getDataThread.interrupt();
                    GetDataThread mr = new GetDataThread();
                    getDataThread = new Thread(mr);
                    getDataThread.start();
                }

                //
            }

            if (mGoogleApiClient != null){
                mGoogleApiClient.connect();
            }
        }
        else {
            getDataThread.interrupt();
            IsGetDataThread = false;
            if (mGoogleApiClient != null)
                if (mGoogleApiClient.isConnected())
                    mGoogleApiClient.disconnect();
        }

    }

    private class GetDataThread implements Runnable{
        @Override
        public void run() {
            Integer timer = 5;
            while (IsGetDataThread){
                if (onMainDataChangeListener != null){
                    //Log.d(TAG, "GetDataThread run");
                    //MainApplication.getInstance().getDOT().getData();
                    DOTResponse dotResponse = MainApplication.getInstance().getnDot().data();
                    if (dotResponse.getCode() == 200){
                        try {
                            MainApplication.getInstance().parseData(new JSONObject(dotResponse.getBody()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                }
                else {Log.d(TAG, "GetDataThread run onMainDataChangeListener = null");}
                try {
                    TimeUnit.SECONDS.sleep(timer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //Log.d(TAG, "GetDataThread stop");
        }
    }

    public String getVersionName() {

        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (final PackageManager.NameNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Could not get version from manifest.");
        }
        if (versionName == null) {
            versionName = "unknown";
        }
        return versionName;
    }

    public String getVersionCode() {

        String versionName = null;
        try {
            Integer Code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = Code.toString();
        }
        catch (final PackageManager.NameNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Could not get version from manifest.");
        }
        if (versionName == null) {
            versionName = "unknown";
        }
        return versionName;
    }

    public void showToastType(int toastType){
        String message = "";
        switch (toastType){
            case Constants.DOT_HTTP_ERROR:message = getString(R.string.errorConnection);break;
            case Constants.DOT_REST_ERROR:message = getString(R.string.errorRest);break;
            case Constants.DOT_PHONE_WRONG:message = getString(R.string.errorPhoneWrong);break;
            case Constants.DOT_PASSWORD_WRONG:message = getString(R.string.errorPasswordWrong);break;
            case Constants.DOT_IDENTIFICATION:message = getString(R.string.errorIdentification);break;
            case Constants.DOT_DRIVER_WRONG:message = getString(R.string.errorDriverWrong);break;
        }
        if (!message.equals(""))showToast(message);
    }

    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    public void callIntent(String phone) {
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(dialIntent);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "onLocationChanged location = " + location.toString());
        if (mLocation == null){
            //Log.d(TAG, "onLocationChanged mLocation = null");
            mLocation = location;
            if (onMainDataChangeListener != null){
                onMainDataChangeListener.OnNewGPSData();
            }

            new Thread(){
                @Override
                public void run() {
                    //Log.d(TAG, "getFastRoutePointAirportAndStation start");
                    try {
                        RoutePoint.getFastRoutePointAirportAndStation(lLocation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }
        mLocation = location;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }
}
