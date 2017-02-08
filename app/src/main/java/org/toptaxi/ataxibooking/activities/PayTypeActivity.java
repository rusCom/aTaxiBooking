package org.toptaxi.ataxibooking.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.data.PayType;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.adapters.PayTypesAdapter;

public class PayTypeActivity extends AppCompatActivity implements PayTypesAdapter.OnPayTypeClickListener {
    private static String TAG = "#########" + PayTypeActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_type);

        PayTypesAdapter payTypesAdapter = new PayTypesAdapter();
        payTypesAdapter.setOnPayTypeClickListener(this);

        RecyclerView rvPayTypes = (RecyclerView)findViewById(R.id.rvPayTypes);
        rvPayTypes.setLayoutManager(new LinearLayoutManager(this));
        rvPayTypes.setAdapter(payTypesAdapter);


        ((EditText)findViewById(R.id.edTitle)).setText("Выбор способа оплаты");
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
    }

    @Override
    public void PayTypeClick(PayType payType) {
        if (!MainApplication.getInstance().getOrder().getPayType().getType().equals(payType.getType())){
            MainApplication.getInstance().getOrder().setPayType(payType);
            setResult(RESULT_OK);
        }
        finish();
    }
}
