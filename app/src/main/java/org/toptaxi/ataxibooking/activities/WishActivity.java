package org.toptaxi.ataxibooking.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.toptaxi.ataxibooking.data.ValueAddition;
import org.toptaxi.ataxibooking.R;

public class WishActivity extends AppCompatActivity {
    Spinner spValueAddition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish);
        findViewById(R.id.btnTitleLeft).setBackgroundResource(R.drawable.ic_arrow_back);
        findViewById(R.id.btnTitleRight).setBackgroundResource(R.drawable.ic_check);

        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        ((EditText)findViewById(R.id.edTitle)).setSingleLine(true);
        ((EditText)findViewById(R.id.edTitle)).setHint("Выберите пожелания к заказу");

        spValueAddition = (Spinner)findViewById(R.id.spWishActivityValueAddition);
        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new ValueAddition[] {
                new ValueAddition( 0 ),
                new ValueAddition( 20 ),
                new ValueAddition( 40),
                new ValueAddition( 60 ),
                new ValueAddition( 80 ),
                new ValueAddition( 100 )
        });
        spValueAddition.setAdapter(spinnerArrayAdapter);

    }
}
