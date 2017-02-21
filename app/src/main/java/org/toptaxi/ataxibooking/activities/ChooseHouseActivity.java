package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

import java.util.ArrayList;

public class ChooseHouseActivity extends FragmentActivity  {


    RoutePoint streetRoutePoint;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_house);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        streetRoutePoint = getIntent().getParcelableExtra(RoutePoint.class.getCanonicalName());
        ((EditText)findViewById(R.id.edTitle)).setHint(streetRoutePoint.getName());

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
                new CheckHouseNumberTask(ChooseHouseActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        });
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "onPause");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        super.onPause();
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
