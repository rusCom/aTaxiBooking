package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.toptaxi.ataxibooking.adapters.RoutePointsAdapter;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChooseHouseActivity extends FragmentActivity implements RoutePointsAdapter.OnRoutePointClickListener {
    private static String TAG = "#########" + ChooseHouseActivity.class.getName();
    RecyclerView rvRoutePoints;
    RoutePointsAdapter routePointsAdapter;
    RoutePoint streetRoutePoint;
    AlertDialog dialog;
    EditText edNumber, edSplash;
    private Timer timer = new Timer();
    private final int DELAY = 500; //milliseconds of delay for timer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_house);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        streetRoutePoint = getIntent().getParcelableExtra(RoutePoint.class.getCanonicalName());
        ((EditText)findViewById(R.id.edTitle)).setHint(streetRoutePoint.getName());

        routePointsAdapter = new RoutePointsAdapter();
        routePointsAdapter.setOnRoutePointClickListener(this);
        rvRoutePoints = (RecyclerView)findViewById(R.id.rvChooseHouseActivityHouses);
        rvRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        rvRoutePoints.setAdapter(routePointsAdapter);

        edNumber = (EditText)findViewById(R.id.edChooseHouseActivityHouseNumber);
        edSplash = (EditText)findViewById(R.id.edChooseHouseActivityHouseSplash);

        edNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getHouses();

            }
        });

        edSplash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                getHouses();

            }
        });

        findViewById(R.id.btnTitleLeft).setBackgroundResource(R.drawable.ic_arrow_back);
        findViewById(R.id.btnTitleRight).setBackgroundResource(R.drawable.ic_check);

        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });



        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] params = new String[]{((EditText)findViewById(R.id.edChooseHouseActivityHouseNumber)).getText().toString(),
                        ((EditText)findViewById(R.id.edChooseHouseActivityHouseSplash)).getText().toString()
                };
                //new CheckHouseNumberTask(ChooseHouseActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        });
    }

    public void getHouses(){
        if (!edNumber.getText().toString().trim().equals("")){
            timer.cancel();
            timer = new Timer();
            timer.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            Log.d(TAG, "start search");
                            final List<RoutePoint> routePoints = PlacesAPI.getHouseSearch(streetRoutePoint, edNumber.getText().toString().trim(), edSplash.getText().toString().trim());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    routePointsAdapter.setRoutePoints(routePoints, Constants.ROUTE_POINT_ADAPTER_FAST_ROUTE_POINT);
                                    routePointsAdapter.notifyDataSetChanged();
                                    rvRoutePoints.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "stop search");
                                }
                            });
                        }
                    },
                    DELAY
            );

        }
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "onPause");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        super.onPause();
    }

    @Override
    public void RoutePointClick(RoutePoint routePoint, int position) {
        if (routePoint != null){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra(RoutePoint.class.getCanonicalName(), routePoint);
            finish();
        }
        else {

        }

    }


    private class CheckHouseNumberTask extends AsyncTask<String, Void, RoutePoint> {
        ProgressDialog progressDialog;
        Context mContext;

        CheckHouseNumberTask(Context mContext) {
            this.mContext = mContext;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Обработка данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }
        @Override
        protected RoutePoint doInBackground(String... params) {

            //PlacesAPI.getHouseSearch(streetRoutePoint, params[0], params[1]);

            return RoutePoint.checkHouseNumber(streetRoutePoint, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(RoutePoint routePoint) {
            progressDialog.dismiss();
            if (routePoint != null){
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra(RoutePoint.class.getCanonicalName(), routePoint);
                finish();
            }
            else {
                showDialog();

            }
        }
    }

    private class GetHousesTask extends AsyncTask<String, Void, ArrayList<RoutePoint>>{
        ProgressDialog progressDialog;
        Context mContext;

        GetHousesTask(Context mContext) {
            this.mContext = mContext;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Обработка данных ...");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }
        @Override
        protected ArrayList<RoutePoint> doInBackground(String... params) {
            return PlacesAPI.getHouseSearch(streetRoutePoint, params[0], params[1]);
        }

        @Override
        protected void onPostExecute(ArrayList<RoutePoint> routePoints) {
            progressDialog.dismiss();


        }
    }

    public void showDialog(){


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Этот номер дома не известен системе. Вы уверены, что ввели правильный адрес?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {sendResult();    }
        });
        alertDialog.setNegativeButton("Нет" , null);
        alertDialog.create();
        dialog = alertDialog.show();

    }

    public void sendResult(){
        // передает не тот код ответа
        dialog.cancel();
        streetRoutePoint.setPlaceType(Constants.ROUTE_POINT_TYPE_HOUSE);
        String houseNumber = ((EditText)findViewById(R.id.edChooseHouseActivityHouseNumber)).getText().toString();
        if (!((EditText)findViewById(R.id.edChooseHouseActivityHouseNumber)).getText().toString().equals("")){
            houseNumber += "/" + ((EditText)findViewById(R.id.edChooseHouseActivityHouseNumber)).getText().toString();
        }
        streetRoutePoint.setHouseNumber(houseNumber);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        intent.putExtra(RoutePoint.class.getCanonicalName(), streetRoutePoint);
        finish();
    }
}
