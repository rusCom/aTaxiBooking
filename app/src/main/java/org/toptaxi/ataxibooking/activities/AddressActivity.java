package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.adapters.RoutePointsAdapter;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.data.RoutePoint;
import org.toptaxi.ataxibooking.fragments.FastRoutePointFragment;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AddressActivity extends AppCompatActivity implements RoutePointsAdapter.OnRoutePointClickListener {
    private static String TAG = "#########" + AddressActivity.class.getName();
    RecyclerView rvRoutePoints;
    RoutePointsAdapter routePointsAdapter;
    RoutePoint routePoint;
    EditText edRoutePointSearch;
    RelativeLayout rlFastRoutePoint;
    private Timer timer = new Timer();
    private final int DELAY = 1000; //milliseconds of delay for timer
    ProgressBar progressBar;
    Button btnMap;
    GetRoutePointsTask getRoutePointsTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        edRoutePointSearch = (EditText)findViewById(R.id.edTitle);
        edRoutePointSearch.setFocusableInTouchMode(true);
        edRoutePointSearch.setFocusable(true);
        edRoutePointSearch.setSingleLine(true);

        progressBar = (ProgressBar)findViewById(R.id.pbTitle);
        btnMap      = (Button)findViewById(R.id.btnAddressActivityMap);

        if (MainApplication.getInstance().getOrder().getRouteCount() == 0)btnMap.setVisibility(View.GONE);
        else btnMap.setVisibility(View.VISIBLE);

        routePointsAdapter = new RoutePointsAdapter();
        routePointsAdapter.setOnRoutePointClickListener(this);
        rvRoutePoints = (RecyclerView)findViewById(R.id.rvAddressActivityRoutePoints);
        rvRoutePoints.setLayoutManager(new LinearLayoutManager(this));
        rvRoutePoints.setAdapter(routePointsAdapter);



        edRoutePointSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                rvRoutePoints.setVisibility(View.GONE);
                if (edRoutePointSearch.getText().toString().equals("")){
                    rlFastRoutePoint.setVisibility(View.VISIBLE);
                    if (MainApplication.getInstance().getOrder().getRouteCount() > 0)btnMap.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    rlFastRoutePoint.setVisibility(View.GONE);
                    btnMap.setVisibility(View.GONE);
                    if (edRoutePointSearch.getText().toString().length() > 3) {
                        if (getRoutePointsTask != null){
                            getRoutePointsTask.cancel(true);
                        }
                        new GetRoutePointsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edRoutePointSearch.getText().toString());
                        //getRoutePointsTask.
                        /*
                        progressBar.setVisibility(View.VISIBLE);
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "start search");
                                        final List<RoutePoint> routePoints = PlacesAPI.getBySearch(edRoutePointSearch.getText().toString());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                routePointsAdapter.setRoutePoints(routePoints, Constants.ROUTE_POINT_ADAPTER_FAST_ROUTE_POINT);
                                                routePointsAdapter.notifyDataSetChanged();
                                                rvRoutePoints.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);

                                                Log.d(TAG, "stop search");
                                            }
                                        });
                                    }
                                },
                                DELAY
                        );
                        */
                    }
                }
            }
        });

        findViewById(R.id.btnTitleLeft).setBackgroundResource(R.drawable.ic_arrow_back);

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
                edRoutePointSearch.getText().clear();
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(AddressActivity.this, ChooseOnMapActivity.class);
                startActivityForResult(mapIntent, Constants.ACTIVITY_CHOOSE_MAP);
            }
        });

        initViewPager();

    }

    private class GetRoutePointsTask extends AsyncTask<String, Void, List<RoutePoint>>{
        @Override
        protected void onPreExecute() {
            //Log.d(TAG, "GetRoutePointsTask onPreExecute");
            super.onPreExecute();
            getRoutePointsTask = this;
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<RoutePoint> doInBackground(String... params) {
            //Log.d(TAG, "GetRoutePointsTask doInBackground " + params[0]);
            return PlacesAPI.getBySearch(params[0]);
        }

        @Override
        protected void onPostExecute(List<RoutePoint> routePoints) {
            //Log.d(TAG, "GetRoutePointsTask onPostExecute");
            super.onPostExecute(routePoints);
            routePointsAdapter.setRoutePoints(routePoints, Constants.ROUTE_POINT_ADAPTER_FAST_ROUTE_POINT);
            routePointsAdapter.notifyDataSetChanged();
            rvRoutePoints.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onCancelled() {
            //Log.d(TAG, "GetRoutePointsTask onCancelled");
            super.onCancelled();
            progressBar.setVisibility(View.GONE);

        }
    }



    @Override
    public void RoutePointClick(RoutePoint routePoint, int position) {
        this.routePoint = routePoint;
        new UpdateRoutePointFromPlaceID(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class UpdateRoutePointFromPlaceID extends AsyncTask<Void, Void, Void>{
        ProgressDialog progressDialog;
        Context mContext;

        UpdateRoutePointFromPlaceID(Context mContext) {
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
        protected Void doInBackground(Void... voids) {
            routePoint.updateFromPlaceID();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())progressDialog.dismiss();
            onRoutePointClickListener(routePoint);
        }
    }

    public void onRoutePointClickListener(RoutePoint routePoint){
        if (routePoint.getPlaceType() == Constants.ROUTE_POINT_TYPE_STREET){
            Intent intent = new Intent(AddressActivity.this, ChooseHouseActivity.class);
            intent.putExtra(RoutePoint.class.getCanonicalName(), routePoint);
            startActivityForResult(intent, Constants.ACTIVITY_CHOOSE_HOUSE);
        }
        else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            intent.putExtra(RoutePoint.class.getCanonicalName(), routePoint);
            finish();
        }
    }

    public void initViewPager(){
        rlFastRoutePoint = (RelativeLayout)findViewById(R.id.rlAddressActivityFastRoutePoint);
        ViewPager viewPager = (ViewPager)findViewById(R.id.vpAddressActivityFastRoutePoint);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(FastRoutePointFragment.newInstance(Constants.FAST_ROUTE_POINT_HISTORY), "Недавние");
        adapter.addFragment(FastRoutePointFragment.newInstance(Constants.FAST_ROUTE_POINT_AIRPORT), "Аэропорты");
        adapter.addFragment(FastRoutePointFragment.newInstance(Constants.FAST_ROUTE_POINT_STATION), "Вокзалы");
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabsAddressActivityFastRoutePoint);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult requestCode = " + requestCode + ";resultCode = " + resultCode);

        if (requestCode == Constants.ACTIVITY_CHOOSE_HOUSE){
            if (resultCode == RESULT_OK){
                RoutePoint rroutePoint = data.getParcelableExtra(RoutePoint.class.getCanonicalName());
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra(RoutePoint.class.getCanonicalName(), rroutePoint);
                finish();
            }
        }
        if (requestCode == Constants.ACTIVITY_CHOOSE_MAP){
            if (resultCode == RESULT_OK){
                RoutePoint rroutePoint = data.getParcelableExtra(RoutePoint.class.getCanonicalName());
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                intent.putExtra(RoutePoint.class.getCanonicalName(), rroutePoint);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        //Log.d(TAG, "onPause");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null){
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        super.onPause();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
