package org.toptaxi.ataxibooking.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.toptaxi.ataxibooking.MainActivity;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

public class ChooseOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    RoutePoint viewRoutePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_on_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapMapActivity);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnTitleLeft).setBackgroundResource(R.drawable.ic_arrow_back);
        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ((EditText)findViewById(R.id.edTitle)).setSingleLine(true);
        ((EditText)findViewById(R.id.edTitle)).setHint("Куда поедите?");

        FloatingActionButton fabChoseAddressOnMapSetMapCurLocation = (FloatingActionButton) findViewById(R.id.fabChoseAddressOnMapSetMapCurLocation);
        fabChoseAddressOnMapSetMapCurLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainApplication.getInstance().getLocation().getLatitude(), MainApplication.getInstance().getLocation().getLongitude()), 17));


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

        findViewById(R.id.btnSetPickUpMapActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra(RoutePoint.class.getCanonicalName(), viewRoutePoint);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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
            }
        });
    }

    private class GetAddressByGPS extends AsyncTask<LatLng, Void, RoutePoint>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            (findViewById(R.id.btnSetPickUpMapActivity)).setEnabled(false);
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
                if (routePoint.getPlaceType() != Constants.ROUTE_POINT_TYPE_UNKNOWN)(findViewById(R.id.btnSetPickUpMapActivity)).setEnabled(true);
            }

        }
    }
}
