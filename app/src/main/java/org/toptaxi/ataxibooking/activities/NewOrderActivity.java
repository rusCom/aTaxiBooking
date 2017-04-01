package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.adapters.RoutePointsAdapter;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.dialogs.DateTimePickerDialog;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.DOTResponse;

import java.util.Calendar;

public class NewOrderActivity extends AppCompatActivity implements DateTimePickerDialog.OnDateTimePickerDialogListener, RoutePointsAdapter.OnRoutePointClickListener {
    boolean IsCalc = false;
    RoutePointsAdapter routePointsAdapter;
    RecyclerView rvRoutePoints;
    ImageButton btnAddOrder;
    Button btnWishList, btnPrior, btnPayType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        btnAddOrder = (ImageButton) findViewById(R.id.btnNewOrderActivityAddOrder);
        btnWishList = (Button) findViewById(R.id.btnNewOrderActivityWishList);
        btnPrior    = (Button) findViewById(R.id.btnNewOrderActivityTime);
        btnPayType  = (Button) findViewById(R.id.btnNewOrderActivityPaymentType);
        //btnAddOrder.setEnabled(false);


        rvRoutePoints = (RecyclerView)findViewById(R.id.rvNewOrderRoutePoints);
        rvRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        routePointsAdapter = new RoutePointsAdapter();
        routePointsAdapter.setOnRoutePointClickListener(this);
        routePointsAdapter.setRoutePoints(MainApplication.getInstance().getOrder().getRoutePoints(), Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER);
        routePointsAdapter.notifyDataSetChanged();
        rvRoutePoints.setAdapter(routePointsAdapter);

        findViewById(R.id.btnTitleLeft).setBackgroundResource(R.drawable.ic_arrow_back);
        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        (findViewById(R.id.edTitle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!IsCalc){
                    Intent intent = new Intent(NewOrderActivity.this, AddressActivity.class);
                    startActivityForResult(intent, Constants.ACTIVITY_ADDRESS);
                }
            }
        });



        (findViewById(R.id.btnNewOrderActivityTime)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOrderWorkDateClick();
            }
        });

        generateView();

    }

    public void setOrderWorkDateClick(){
        DateTimePickerDialog dialog = new DateTimePickerDialog(this, this);
        dialog.show();
    }

    public void btnWishClick(View view){
        startActivityForResult(new Intent(NewOrderActivity.this, WishActivity.class), Constants.ACTIVITY_WISH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_ADDRESS && resultCode == RESULT_OK){
            RoutePoint routePoint = data.getParcelableExtra(RoutePoint.class.getCanonicalName());
            MainApplication.getInstance().getOrder().addRoutePoint(routePoint);
            routePointsAdapter.setRoutePoints(MainApplication.getInstance().getOrder().getRoutePoints(), Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER);
            routePointsAdapter.notifyDataSetChanged();
            new CalcOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (requestCode == Constants.ACTIVITY_PLACE_PICKER && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this, data);
            if (place != null){
                RoutePoint routePoint = new RoutePoint();
                routePoint.setFromPlace(place);
                MainApplication.getInstance().getOrder().addRoutePoint(routePoint);
                routePointsAdapter.setRoutePoints(MainApplication.getInstance().getOrder().getRoutePoints(), Constants.ROUTE_POINT_ADAPTER_VIEW_ORDER);
                routePointsAdapter.notifyDataSetChanged();
                new CalcOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if (requestCode == Constants.ACTIVITY_PAY_TYPE && resultCode == RESULT_OK){
            new CalcOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onBackPressed() {
        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            MainApplication.getInstance().getOrder().clear();
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Внимание, все введенные данные пропадут.");
            alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainApplication.getInstance().getOrder().clear();
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
            alertDialog.setNegativeButton("Нет" , null);
            alertDialog.create();
            alertDialog.show();
        }
    }

    public void generateView(){
        /*
        ((TextView)findViewById(R.id.tvNewOrderPriceDescription)).setText(MainApplication.getInstance().getOrder().getPriceDescription());
        ((TextView)findViewById(R.id.tvNewOrderPrice)).setText(MainApplication.getInstance().getOrder().getPrice());
        */

        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            ((EditText)findViewById(R.id.edTitle)).setHint("Куда поедите");
            btnPrior.setEnabled(MainApplication.getInstance().getPreferences().getCalcTypeTaximeter());
            btnWishList.setEnabled(MainApplication.getInstance().getPreferences().getCalcTypeTaximeter());
            btnPayType.setEnabled(MainApplication.getInstance().getPreferences().getCalcTypeTaximeter());
            btnAddOrder.setEnabled(MainApplication.getInstance().getPreferences().getCalcTypeTaximeter());
        }
        else {
            ((EditText)findViewById(R.id.edTitle)).setHint("Добавить адрес");
            btnPrior.setEnabled(true);
            btnWishList.setEnabled(true);
            btnPayType.setEnabled(true);
            btnAddOrder.setEnabled(true);
        }

        ((Button)findViewById(R.id.btnNewOrderActivityTime)).setText(MainApplication.getInstance().getOrder().getPriorInfo());
        ((Button)findViewById(R.id.btnNewOrderActivityServiceType)).setText(MainApplication.getInstance().getOrder().getServiceTypeName());
        ((Button)findViewById(R.id.btnNewOrderActivityPaymentType)).setText(MainApplication.getInstance().getOrder().getPayType().getCaption());
        ((Button)findViewById(R.id.btnNewOrderActivityPaymentType)).setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, MainApplication.getInstance().getOrder().getPayType().getCardImage()) , null, null);
        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            findViewById(R.id.tvNewOrderActivityCost).setVisibility(View.GONE);
            findViewById(R.id.ivNewOrderActivityPriceDivider).setVisibility(View.GONE);
        }
        else if (!MainApplication.getInstance().getPreferences().getCalcTypeTaximeter()) {
            findViewById(R.id.tvNewOrderActivityCost).setVisibility(View.VISIBLE);
            findViewById(R.id.ivNewOrderActivityPriceDivider).setVisibility(View.VISIBLE);
            ((TextView)findViewById(R.id.tvNewOrderActivityCost)).setText(MainApplication.getInstance().getOrder().getPriceString());
        }

        if (MainApplication.getInstance().getPreferences().IsWishList())btnWishList.setVisibility(View.VISIBLE);
        else btnWishList.setVisibility(View.GONE);

        if (MainApplication.getInstance().getPreferences().IsPrior())btnPrior.setVisibility(View.VISIBLE);
        else btnPrior.setVisibility(View.GONE);

        if (MainApplication.getInstance().getPreferences().getPayTypes().size() == 1)btnPayType.setVisibility(View.GONE);
        else {btnPayType.setVisibility(View.VISIBLE);}



    }

    @Override
    public void DateTimePickerDialogChose(Calendar date) {
        MainApplication.getInstance().getOrder().setWorkDate(date);
        new CalcOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnNewOrderActivityAddOrderClick(View view){
        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            if (MainApplication.getInstance().getPreferences().getCalcTypeTaximeter())new AddOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getString(R.string.dlgNewOrderActivityNotHaveTaximeter));
                alertDialog.setPositiveButton(getString(R.string.dlgOk), null);
                alertDialog.create();
                alertDialog.show();
            }

        }
        else new AddOrderTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void btnPayTypeClick(View view){
        if (MainApplication.getInstance().getOrder().getRouteCount() == 1){
            if (MainApplication.getInstance().getPreferences().getCalcTypeTaximeter())showPayTypeDialog();
            else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getString(R.string.dlgNewOrderActivityNotHaveTaximeter));
                alertDialog.setPositiveButton(getString(R.string.dlgOk), null);
                alertDialog.create();
                alertDialog.show();
            }

        }
        else showPayTypeDialog();
    }

    public void showPayTypeDialog(){
        if (MainApplication.getInstance().getPreferences().IsHavePaymentTypes()){
            startActivityForResult(new Intent(NewOrderActivity.this, PayTypeActivity.class), Constants.ACTIVITY_PAY_TYPE);
        }
        else {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getString(R.string.dlgNewOrderActivityNotHavePaymentTypes));
            alertDialog.setPositiveButton(getString(R.string.dlgOk), null);
            alertDialog.create();
            alertDialog.show();
        }
    }

    @Override
    public void RoutePointClick(RoutePoint routePoint, int position) {
        if (routePoint == null){
            if (!IsCalc){
                Intent intent = new Intent(NewOrderActivity.this, AddressActivity.class);
                startActivityForResult(intent, Constants.ACTIVITY_ADDRESS);
            }
        }
    }

    private class AddOrderTask extends AsyncTask<Void, Void, String>{
        ProgressDialog progressDialog;
        AddOrderTask(Context mContext){
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getResources().getString(R.string.dlgCheckData));
            progressDialog.setCancelable(false);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            return MainApplication.getInstance().getDOT().sendData("add_order", MainApplication.getInstance().getOrder().getCalcID());
        }

        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);
            if (progressDialog.isShowing())progressDialog.dismiss();
            try {
                JSONObject result = new JSONObject(results);
                if (result.getString("response").equals("ok")){finish();}
                else {
                    MainApplication.getInstance().showToast(result.getString("value"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                MainApplication.getInstance().showToast(getResources().getString(R.string.errorRest));
            }
        }
    }

    private class CalcOrderTask extends AsyncTask<Void, Void, DOTResponse> {
        ProgressDialog progressDialog;
        Context mContext;

        CalcOrderTask(Context mContext) {
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
            MainApplication.getInstance().getOrder().calcDistance();
            return MainApplication.getInstance().getnDot().calc(MainApplication.getInstance().getOrder().getCalcJSON());
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                if (!MainApplication.getInstance().getOrder().setCalcData(result.getBody())){
                    MainApplication.getInstance().showToast("Ошибка при расчете стоимости");
                }
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
            generateView();
        }

    }
}
