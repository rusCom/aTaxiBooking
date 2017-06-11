package org.toptaxi.ataxibooking;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.activities.HisOrdersActivity;
import org.toptaxi.ataxibooking.activities.SplashActivity;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.Driver;
import org.toptaxi.ataxibooking.dialogs.NewVersionDialog;
import org.toptaxi.ataxibooking.dialogs.UserAgreementDialog;
import org.toptaxi.ataxibooking.tools.DOTResponse;
import org.toptaxi.ataxibooking.tools.OnMainDataChangeListener;
import org.toptaxi.ataxibooking.tools.RadarView;
import org.toptaxi.ataxibooking.activities.AccountActivity;
import org.toptaxi.ataxibooking.activities.AddressActivity;
import org.toptaxi.ataxibooking.activities.ChooseHouseActivity;
import org.toptaxi.ataxibooking.activities.NewOrderActivity;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMainDataChangeListener, Drawer.OnDrawerItemClickListener, GoogleApiClient.ConnectionCallbacks {
    private static String TAG = "#########" + MainActivity.class.getName();


    private GoogleMap mMap;
    Geocoder mGeocoder;
    ImageButton btnSetPickup;
    EditText edMainActivityTitle;
    RoutePoint viewRoutePoint;
    RelativeLayout rlNewOrder, rlOrderSearchDriver;
    ImageView ivCentralPickUp;
    LinearLayout llRoutePoints;
    CardView llCarInfo;
    static RadarView mRadarView = null;
    FloatingActionButton fabCallDriver, fabCancelOrder, fabSearchAddress;
    String callIntentPhone;
    protected Drawer drawer;
    protected AccountHeader accountHeader;
    IProfile profile;
    long back_pressed;
    PrimaryDrawerItem menuBalanceItem, menuMapTypeItem;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d(TAG, "onCreate");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        mGeocoder = new Geocoder(this, Locale.getDefault());

        initialInterface();
        generateDrawer();

        // Проверяем на принятие пользовательского соглашение
        if (MainApplication.getInstance().getAccount().IsShowUserAgreement()){
            UserAgreementDialog userAgreementDialog = new UserAgreementDialog(this);
            userAgreementDialog.show();
        }
        // Проверим на наличие обновления
        else if (MainApplication.getInstance().getAccount().IsShowUpgradeVersion()){
            new NewVersionDialog(this).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (MainApplication.getInstance().getGoogleApiClient() == null){
            //Log.d(TAG, "google api cline = null");
            Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(splashIntent);
            finish();
        }
        else {
            MainApplication.getInstance().setOnMainDataChangeListener(this);
            MainApplication.getInstance().setMainActivity(this);
            generateView();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        MainApplication.getInstance().setMainActivity(null);
        MainApplication.getInstance().setOnMainDataChangeListener(null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.d(TAG, "onMapReady");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            }
        });

        if (MainApplication.getInstance().getLocation() != null)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getLocation().getLatitude(), MainApplication.getInstance().getLocation().getLongitude()), 17));

            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    //Log.d(TAG, "onCameraIdle " + mMap.getCameraPosition().toString());
                    (new GetAddressByGPS()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMap.getCameraPosition().target);
                    MainApplication.getInstance().setMapLocation(mMap.getCameraPosition().target);
                }
            });
        generateView();
    }

    public void initialInterface() {
        rlNewOrder = (RelativeLayout) findViewById(R.id.rlMainActivityNewOrder);
        rlOrderSearchDriver = (RelativeLayout) findViewById(R.id.rlMainActivityOrderSearchDriver);
        llRoutePoints = (LinearLayout) findViewById(R.id.llMainActivityRoutePoints);
        llCarInfo = (CardView) findViewById(R.id.llMainActivityCarInfo);

        btnSetPickup = (ImageButton) findViewById(R.id.btnSetPickUp);
        edMainActivityTitle = (EditText) findViewById(R.id.edTitle);
        //mRadarView = (RadarView) findViewById(R.id.radarView);
        ivCentralPickUp = (ImageView) findViewById(R.id.ivMainActivityCentralPickUp);

        (findViewById(R.id.btnSetPickUp)).setEnabled(false);
        ((TextView)findViewById(R.id.tvAddressLine)).setText(getString(R.string.captionSearchAddress));
        ((TextView)findViewById(R.id.tvAddressLocality)).clearComposingText();
        ((TextView)findViewById(R.id.btnSetPickUpCaption)).setText(getString(R.string.btnPickUpMainActivitySearch));

        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawer != null)drawer.openDrawer();
            }
        });

        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelOrder();
            }
        });



        edMainActivityTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_NEW_ORDER) {
                    Intent addressIntent = new Intent(MainActivity.this, AddressActivity.class);
                    startActivityForResult(addressIntent, Constants.ACTIVITY_ADDRESS);
                }
            }
        });

        edMainActivityTitle.setSingleLine(true);

        btnSetPickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //addRoutePoint(viewRoutePoint);
                viewRoutePoint.setMapLocation(mMap.getCameraPosition().target);
                viewRoutePoint.setNoteFromHistory();
                MainApplication.getInstance().getOrder().addRoutePoint(viewRoutePoint);
                Intent intent = new Intent(MainActivity.this, NewOrderActivity.class);
                startActivityForResult(intent, Constants.ACTIVITY_NEW_ORDER);
            }
        });

        Animation animationRotateCenter = AnimationUtils.loadAnimation(
                this, R.anim.rotate_center);

        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int width = (int) (displaymetrics.widthPixels * 0.8);
        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(width, width);
        l.addRule(RelativeLayout.CENTER_IN_PARENT);


        ImageView imgview = (ImageView) findViewById(R.id.ivRadar);
        imgview.setLayoutParams(l);
        imgview.startAnimation(animationRotateCenter);


        fabSearchAddress = (FloatingActionButton)findViewById(R.id.fabMainActivitySearchAddress);
        fabSearchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddressActivity.class), Constants.ACTIVITY_ADDRESS);
            }
        });

        fabCallDriver = (FloatingActionButton) findViewById(R.id.fabMainActivityCallDriver);
        fabCallDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callIntent(MainApplication.getInstance().getOrder().getDriver().getPhone());
            }
        });

        fabCancelOrder = (FloatingActionButton)findViewById(R.id.fabMainActivityCancelOrder);
        fabCancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelOrder();
            }
        });

        FloatingActionButton fabChoseAddressOnMapSetMapCurLocation = (FloatingActionButton) findViewById(R.id.fabChoseAddressOnMapSetMapCurLocation);
        fabChoseAddressOnMapSetMapCurLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_NEW_ORDER){
                    if (MainApplication.getInstance().getLocation() != null)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getLocation().getLatitude(), MainApplication.getInstance().getLocation().getLongitude()), 17));
                }
                else {
                    MainApplication.getInstance().getOrder().setMapDriverAnimate(false);
                    generateView();
                }

            }
        });

        FloatingActionButton fabChoseAddressOnMapZoomIn = (FloatingActionButton) findViewById(R.id.fabChoseAddressOnMapZoomIn);
        fabChoseAddressOnMapZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float MapZoom = mMap.getCameraPosition().zoom + 1;
                if (MapZoom > 18) {
                    MapZoom = 18;
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, MapZoom));
            }
        });
        FloatingActionButton fabChoseAddressOnMapZoomOut = (FloatingActionButton) findViewById(R.id.fabChoseAddressOnMapZoomOut);
        fabChoseAddressOnMapZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float MapZoom = mMap.getCameraPosition().zoom - 1;
                if (MapZoom < 1) {
                    MapZoom = 1;
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, MapZoom));
            }
        });
    }

    public void callIntent(String phone){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CALL_PHONE)){
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE},
                        Constants.MY_PERMISSIONS_CALL_PHONE);
                callIntentPhone = phone;
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE},
                        Constants.MY_PERMISSIONS_CALL_PHONE);
                callIntentPhone = phone;
            }
        }
        else {
            Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialIntent);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        }
        else {
            if (back_pressed + 2000 > System.currentTimeMillis()) super.onBackPressed();
            else
                Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода из приложения", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.MY_PERMISSIONS_CALL_PHONE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    callIntent(callIntentPhone);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.d(TAG, "onActivityResult requestCode = " + requestCode + ";resultCode = " + resultCode);
        if (requestCode == Constants.ACTIVITY_ADDRESS){
            if (resultCode == RESULT_OK){
                RoutePoint routePoint = data.getParcelableExtra(RoutePoint.class.getCanonicalName());
                addRoutePoint(routePoint);
            }
        }
        if (requestCode == Constants.ACTIVITY_CHOOSE_HOUSE){
            if (resultCode == RESULT_OK){
                RoutePoint routePoint = data.getParcelableExtra(RoutePoint.class.getCanonicalName());
                addRoutePoint(routePoint);
            }
        }
    }

    @Override
    public void OnMainDataChange() {
        generateView();
    }

    @Override
    public void OnNewGPSData() {
        if (mMap != null){
            if (MainApplication.getInstance().getLocation() != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getLocation().getLatitude(), MainApplication.getInstance().getLocation().getLongitude()), 17));
        }
    }

    @Override
    public void OnOrderDone() {
        MainApplication.getInstance().playSoundAssign();
        if (mMap != null){
            if (MainApplication.getInstance().getLocation() != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getLocation().getLatitude(), MainApplication.getInstance().getLocation().getLongitude()), 17));
        }
    }

    @Override
    public void OnAccountDataChange() {
        profile.withName(MainApplication.getInstance().getAccount().getName());
        profile.withEmail(MainApplication.getInstance().getAccount().getMail());
        accountHeader.updateProfile(profile);
    }

    public void generateView(){
        //Log.d(TAG, "generateView orderState = " + MainApplication.getInstance().getOrder().getStatus());
        rlNewOrder.setVisibility(View.GONE);
        rlOrderSearchDriver.setVisibility(View.GONE);
        ivCentralPickUp.setVisibility(View.GONE);
        llRoutePoints.setVisibility(View.GONE);
        llCarInfo.setVisibility(View.GONE);
        fabCallDriver.setVisibility(View.GONE);
        fabCancelOrder.setVisibility(View.GONE);
        fabSearchAddress.setVisibility(View.GONE);
        edMainActivityTitle.getText().clear();
        if (mMap != null)mMap.getUiSettings().setScrollGesturesEnabled(true);
        // Если статус заказа новый заказ
        if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_NEW_ORDER){
            edMainActivityTitle.setHint(getString(R.string.captionMainActivityNewOrder));
            ((TextView)findViewById(R.id.btnSetPickUpCaption)).setText(getString(R.string.btnPickUpMainActivity));
            rlNewOrder.setVisibility(View.VISIBLE);
            ivCentralPickUp.setVisibility(View.VISIBLE);
            fabSearchAddress.setVisibility(View.VISIBLE);
            if (mMap != null){
                mMap.clear();
                for (int itemID = 0; itemID < MainApplication.getInstance().getDrivers().getCount(); itemID++){
                    Driver driver = MainApplication.getInstance().getDrivers().getDriver(itemID);
                    if (driver != null){
                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(driver.getLatLng())
                                        .icon(BitmapDescriptorFactory.fromBitmap(driver.getBitmap()))
                                        .rotation(driver.getBearing())

                        );
                    }
                }
            }
        }
        // Если статус заказа - поиск автомобиля
        else if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_SEARCH_DRIVER){
            edMainActivityTitle.setHint(getString(R.string.captionMainActivitySearchDriver));
            rlOrderSearchDriver.setVisibility(View.VISIBLE);
            ivCentralPickUp.setVisibility(View.VISIBLE);
            llRoutePoints.setVisibility(View.VISIBLE);
            if (MainApplication.getInstance().getOrder().getCanDeny())fabCancelOrder.setVisibility(View.VISIBLE);
            setRouteDataView();
            if (mMap != null){
                if (MainApplication.getInstance().getOrder().getRoutePoint(0) != null)mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainApplication.getInstance().getOrder().getRoutePoint(0).getLatLng(), 16));
                //mMap.setTrafficEnabled(true);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.clear();
                for (int itemID = 0; itemID < MainApplication.getInstance().getDrivers().getCount(); itemID++){
                    LatLng driverPoint = MainApplication.getInstance().getDrivers().getDriver(itemID).getLatLng();
                    if (driverPoint != null){
                        mMap.addMarker(
                                new MarkerOptions()
                                    .position(driverPoint)
                                    .icon(BitmapDescriptorFactory.fromBitmap(MainApplication.getInstance().getDrivers().getDriver(itemID).getBitmap()))
                                    .rotation(MainApplication.getInstance().getDrivers().getDriver(itemID).getBearing())
                        );

                    }
                }
            }
        }
        // Если статус - водитель едет к клтенту
        else if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_DRIVE_TO_CLIENT){
            edMainActivityTitle.setHint(getString(R.string.captionMainActivityDriveToClient));
            llRoutePoints.setVisibility(View.VISIBLE);
            llCarInfo.setVisibility(View.VISIBLE);
            fabCallDriver.setVisibility(View.VISIBLE);
            if (MainApplication.getInstance().getOrder().getCanDeny())fabCancelOrder.setVisibility(View.VISIBLE);
            setRouteDataView();
            setDriverDataView();
            if (mMap != null){
                mMap.clear();
                setRoutePointOnMap();
                setCarPointOnMap();
                if (!MainApplication.getInstance().getOrder().isMapDriverAnimate()){
                    int size = this.getResources().getDisplayMetrics().widthPixels;
                    LatLngBounds.Builder latLngBuilder2 = new LatLngBounds.Builder();
                    latLngBuilder2.include(MainApplication.getInstance().getOrder().getRoutePoint(0).getLatLng());
                    latLngBuilder2.include(MainApplication.getInstance().getOrder().getDriverLocation());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder2.build(), size, size, 100));
                    MainApplication.getInstance().getOrder().setMapDriverAnimate(true);
                }
            }
        }
        // Статус - ожидание клиента
        else if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_DRIVER_AT_CLIENT){
            edMainActivityTitle.setHint(getString(R.string.captionMainActivityDriverAtClient));
            llRoutePoints.setVisibility(View.VISIBLE);
            llCarInfo.setVisibility(View.VISIBLE);
            fabCallDriver.setVisibility(View.VISIBLE);
            if (MainApplication.getInstance().getOrder().getCanDeny())fabCancelOrder.setVisibility(View.VISIBLE);
            setRouteDataView();
            setDriverDataView();
            if (mMap != null){
                mMap.clear();
                setRoutePointOnMap();
                setCarPointOnMap();
                if (!MainApplication.getInstance().getOrder().isMapDriverAnimate()){
                    if (MainApplication.getInstance().getOrder().getRoutePoint(0) != null)mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainApplication.getInstance().getOrder().getRoutePoint(0).getLatLng(), 18));
                    MainApplication.getInstance().getOrder().setMapDriverAnimate(true);
                }
            }
        }
        // Статус -  с клиентом
        else if (MainApplication.getInstance().getOrder().getStatus() == Constants.ORDER_STATE_CLIENT_IN_CAR){
            edMainActivityTitle.setHint(getString(R.string.captionMainActivityClientInCar));
            llRoutePoints.setVisibility(View.VISIBLE);
            llCarInfo.setVisibility(View.VISIBLE);
            fabCallDriver.setVisibility(View.VISIBLE);
            if (MainApplication.getInstance().getOrder().getCanDeny())fabCancelOrder.setVisibility(View.VISIBLE);
            setRouteDataView();
            setDriverDataView();
            if (mMap != null){
                mMap.clear();
                setRoutePointOnMap();
                setCarPointOnMap();
                if (!MainApplication.getInstance().getOrder().isMapDriverAnimate()){
                    int size = this.getResources().getDisplayMetrics().widthPixels;
                    LatLngBounds.Builder latLngBuilder2 = new LatLngBounds.Builder();
                    for (int itemID = 0; itemID < MainApplication.getInstance().getOrder().getRouteCount(); itemID++){
                        latLngBuilder2.include(MainApplication.getInstance().getOrder().getRoutePoint(itemID).getLatLng());
                    }
                    latLngBuilder2.include(MainApplication.getInstance().getOrder().getDriverLocation());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder2.build(), size, size, 100));
                    MainApplication.getInstance().getOrder().setMapDriverAnimate(true);
                }
            }
        }


    }

    public void setCarPointOnMap(){
        if (MainApplication.getInstance().getOrder().getDriverLocation() != null){
            mMap.addMarker(
                    new MarkerOptions()
                            .position(MainApplication.getInstance().getOrder().getDriverLocation())
                            .icon(BitmapDescriptorFactory.fromBitmap(MainApplication.getInstance().getOrder().getDriver().getBitmap()))
            );
        }
    }

    public void setRoutePointOnMap(){
        for (int itemID = 0; itemID < MainApplication.getInstance().getOrder().getRouteCount(); itemID++){
            RoutePoint routePoint = MainApplication.getInstance().getOrder().getRoutePoint(itemID);
            Bitmap bitmap;
            if (itemID == 0)bitmap = MainApplication.getBitmap(this, R.mipmap.ic_onboard_from);
            else if (itemID == (MainApplication.getInstance().getOrder().getRouteCount() - 1))bitmap = MainApplication.getBitmap(this, R.mipmap.ic_onboard_to);
            else bitmap = MainApplication.getBitmap(this, R.mipmap.ic_onboard_address);
            mMap.addMarker(new MarkerOptions().
                    position(routePoint.getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                );
        }
    }


    public void setDriverDataView(){
        ((TextView)findViewById(R.id.tvMainActivityCarName)).setText(MainApplication.getInstance().getOrder().getDriver().getCarName());
    }

    public void setRouteDataView(){
        RoutePoint firstRoutePoint = MainApplication.getInstance().getOrder().getRoutePoint(0);
        if (firstRoutePoint != null){
            ((TextView)findViewById(R.id.tvMainActivityPickupName)).setText(firstRoutePoint.getName());
            ((TextView)findViewById(R.id.tvMainActivityPickupDescription)).setText(firstRoutePoint.getDescription());
        }
        ((TextView)findViewById(R.id.tvMainActivityDestinationName)).setText("По факту ...");
        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            ((TextView)findViewById(R.id.tvMainActivityDestinationName)).setText("По факту ...");
            ((TextView)findViewById(R.id.tvMainActivityDestinationDescription)).setText("");
        }
        else {
            RoutePoint lastRoutePoint = MainApplication.getInstance().getOrder().getRoutePoint(MainApplication.getInstance().getOrder().getRouteCount() - 1);
            if (lastRoutePoint != null){
                ((TextView)findViewById(R.id.tvMainActivityDestinationName)).setText(lastRoutePoint.getName());
                ((TextView)findViewById(R.id.tvMainActivityDestinationDescription)).setText(lastRoutePoint.getDescription());
            }
        }
    }


    public void cancelOrder(){
        if (MainApplication.getInstance().getOrder().getStatus() != Constants.ORDER_STATE_NEW_ORDER){
            if (MainApplication.getInstance().getOrder().getCanDeny()){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Отменить заказ");
                alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new OrderDenyTask(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
                alertDialog.setNegativeButton("Нет" , null);
                alertDialog.create();
                alertDialog.show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "google api client connected");
        MainApplication.getInstance().setGoogleApiClient(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class OrderDenyTask extends AsyncTask<Void, Void, DOTResponse> {
        ProgressDialog progressDialog;
        Context mContext;

        OrderDenyTask(Context mContext) {
            this.mContext = mContext;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getResources().getString(R.string.dlgCheckData));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected DOTResponse doInBackground(Void... voids) {
            return MainApplication.getInstance().getnDot().orders_deny();
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                try {
                    MainApplication.getInstance().parseData(new JSONObject(result.getBody()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            /*
            if (result.getCode() == 200){
                if (!MainApplication.getInstance().getOrder().setCalcData(result.getBody())){
                    MainApplication.getInstance().showToast("Ошибка при расчете стоимости");
                    MainApplication.getInstance().getOrder().setCalcSucces(false);
                }
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
                MainApplication.getInstance().getOrder().setCalcSucces(false);
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
                MainApplication.getInstance().getOrder().setCalcSucces(false);
            }
            generateView();
            */
        }

    }

    public void generateDrawer(){
        profile = new ProfileDrawerItem()
                .withName(MainApplication.getInstance().getAccount().getNameForDrawer())
                .withEmail(MainApplication.getInstance().getAccount().getMail())
                .withIcon(ContextCompat.getDrawable(this, R.mipmap.contact_default));
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(profile)
                .withCompactStyle(true)
                .withTextColor(ContextCompat.getColor(this, R.color.account_header_text))
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                        startActivity(intent);
                        return false;
                    }
                })
                .build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withOnDrawerItemClickListener(this)
                .withAccountHeader(accountHeader)
                .build();
        if (MainApplication.getInstance().getPreferences().getPayTypeBonus()){
            menuBalanceItem = new PrimaryDrawerItem().withName(getString(R.string.menuBalanceItem)).withIcon(FontAwesome.Icon.faw_rub).withSelectable(false).withBadge(String.valueOf(MainApplication.getInstance().getAccount().getBalance())).withIdentifier(Constants.MENU_BALANCE);
            drawer.addItem(menuBalanceItem);
        }

        drawer.addItem(new PrimaryDrawerItem().withName(getString(R.string.menuProfileItem)).withIcon(FontAwesome.Icon.faw_address_card).withSelectable(false).withIdentifier(Constants.MENU_PROFILE));
        //drawer.addItem(new PrimaryDrawerItem().withName(getString(R.string.menuHistoryItem)).withIcon(FontAwesome.Icon.faw_history).withSelectable(false).withIdentifier(Constants.MENU_HISTORY));
        menuMapTypeItem = new PrimaryDrawerItem().withName(getString(R.string.menuMapTypeItem)).withIcon(FontAwesome.Icon.faw_map).withSelectable(false).withIdentifier(Constants.MENU_MAP_TYPE);
        drawer.addItem(menuMapTypeItem);
        drawer.addItem(new DividerDrawerItem());
        drawer.addItem(new PrimaryDrawerItem().withName("ver. " + MainApplication.getInstance().getVersionName()).withIcon(FontAwesome.Icon.faw_creative_commons).withEnabled(false).withSelectable(false));
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch ((int) drawerItem.getIdentifier()){
            case Constants.MENU_MAP_TYPE:{
                if (MainApplication.getInstance().getMapViewType() == 0){
                    MainApplication.getInstance().setMapViewType(1);
                    menuMapTypeItem.withSetSelected(true);
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                else {
                    MainApplication.getInstance().setMapViewType(0);
                    menuMapTypeItem.withSetSelected(false);
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                drawer.updateItem(menuMapTypeItem);
                break;
            }
            case Constants.MENU_PROFILE:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
            case Constants.MENU_HISTORY:
                startActivity(new Intent(MainActivity.this, HisOrdersActivity.class));
                break;

        }
        return false;
    }


    private class GetAddressByGPS extends AsyncTask<LatLng, Void, RoutePoint>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            (findViewById(R.id.btnSetPickUp)).setEnabled(false);
            ((TextView)findViewById(R.id.tvAddressLine)).setText(getString(R.string.captionSearchAddress));
            ((TextView)findViewById(R.id.tvAddressLocality)).clearComposingText();
            ((TextView)findViewById(R.id.btnSetPickUpCaption)).setText(getString(R.string.btnPickUpMainActivitySearch));
        }
        @Override
        protected RoutePoint doInBackground(LatLng... latLngs) {
            return PlacesAPI.getByLocation(latLngs[0].latitude, latLngs[0].longitude);
        }

        @Override
        protected void onPostExecute(RoutePoint routePoint) {
            super.onPostExecute(routePoint);
            if (routePoint != null){
                ((TextView)findViewById(R.id.tvAddressLine)).setText(routePoint.getName());
                ((TextView)findViewById(R.id.tvAddressLocality)).setText(routePoint.getDescription());
                ((TextView)findViewById(R.id.btnSetPickUpCaption)).setText(getString(R.string.btnPickUpMainActivity));
                viewRoutePoint = routePoint;
                if (routePoint.getPlaceType() != Constants.ROUTE_POINT_TYPE_UNKNOWN)(findViewById(R.id.btnSetPickUp)).setEnabled(true);
            }
            else {

            }

        }
    }

    public void addRoutePoint(RoutePoint routePoint){
        //Log.d(TAG, "addRoutePoint routePoint = " + routePoint.getName() + ";" + routePoint.getPlaceType());
        if (routePoint.getPlaceType() == Constants.ROUTE_POINT_TYPE_STREET){
            Intent intent = new Intent(MainActivity.this, ChooseHouseActivity.class);
            intent.putExtra(RoutePoint.class.getCanonicalName(), routePoint);
            startActivityForResult(intent, Constants.ACTIVITY_CHOOSE_HOUSE);
        }
        else {
            MainApplication.getInstance().getOrder().addRoutePoint(routePoint);
            Intent intent = new Intent(MainActivity.this, NewOrderActivity.class);
            startActivityForResult(intent, Constants.ACTIVITY_NEW_ORDER);
        }
    }


}
