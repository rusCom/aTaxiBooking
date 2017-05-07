package org.toptaxi.ataxibooking.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

import java.util.ArrayList;
import java.util.List;

public class WishActivity extends AppCompatActivity {
    Spinner spValueAddition;
    CardView cvValueAddition, cvCheck, cvConditioner, cvSmoke, cvNoSmoke, cvChildren;
    Switch swCheck, swConditioner, swSmoke, swNoSmoke, swChildren;

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

        cvValueAddition = (CardView)findViewById(R.id.cvWishListValueAddition);
        cvCheck         = (CardView)findViewById(R.id.cvWishListCheck);
        cvConditioner   = (CardView)findViewById(R.id.cvWishListConditioner);
        cvSmoke         = (CardView)findViewById(R.id.cvWishListSmoke);
        cvNoSmoke       = (CardView)findViewById(R.id.cvWishListNoSmoke);
        cvChildren      = (CardView)findViewById(R.id.cvWishListChildren);
        swCheck         = (Switch)findViewById(R.id.swWishListCheck);
        swConditioner   = (Switch)findViewById(R.id.swWishListConditioner);
        swSmoke         = (Switch)findViewById(R.id.swWishListSmoke);
        swNoSmoke       = (Switch)findViewById(R.id.swWishListNoSmoke);
        swChildren      = (Switch)findViewById(R.id.swWishListChildren);

        // Генериуем общий вид
        if (MainApplication.getInstance().getPreferences().getWishValueAddition() > 0){
            spValueAddition = (Spinner)findViewById(R.id.spWishActivityValueAddition);
            Integer count = MainApplication.getInstance().getPreferences().getWishValueAddition()/20;
            List<String> valueAdditions = new ArrayList<>();
            for (int itemID = 0; itemID < count + 1; itemID++){
                valueAdditions.add(String.valueOf(itemID*20) + " " + MainApplication.getRubSymbol());
            }
            ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, valueAdditions);
            spValueAddition.setAdapter(spinnerArrayAdapter);
        }
        else cvValueAddition.setVisibility(View.GONE);

        if (MainApplication.getInstance().getPreferences().getWishCheck() < 0)cvCheck.setVisibility(View.GONE);
        else if (MainApplication.getInstance().getPreferences().getWishCheck() > 0)swCheck.setText(String.valueOf(MainApplication.getInstance().getPreferences().getWishCheck()) + " " + MainApplication.getRubSymbol());

        if (MainApplication.getInstance().getPreferences().getWishConditioner() < 0)cvConditioner.setVisibility(View.GONE);
        else if (MainApplication.getInstance().getPreferences().getWishConditioner() > 0)swConditioner.setText(String.valueOf(MainApplication.getInstance().getPreferences().getWishConditioner()) + " " + MainApplication.getRubSymbol());

        if (MainApplication.getInstance().getPreferences().getWishSmoke() < 0)cvSmoke.setVisibility(View.GONE);
        else if (MainApplication.getInstance().getPreferences().getWishSmoke() > 0)swSmoke.setText(String.valueOf(MainApplication.getInstance().getPreferences().getWishSmoke()) + " " + MainApplication.getRubSymbol());

        if (MainApplication.getInstance().getPreferences().getWishNoSmoke() < 0)cvNoSmoke.setVisibility(View.GONE);
        else if (MainApplication.getInstance().getPreferences().getWishNoSmoke() > 0)swNoSmoke.setText(String.valueOf(MainApplication.getInstance().getPreferences().getWishNoSmoke()) + " " + MainApplication.getRubSymbol());

        if (MainApplication.getInstance().getPreferences().getWishChildren() < 0)cvChildren.setVisibility(View.GONE);
        else if (MainApplication.getInstance().getPreferences().getWishChildren() > 0)swChildren.setText(String.valueOf(MainApplication.getInstance().getPreferences().getWishChildren()) + " " + MainApplication.getRubSymbol());


        if (MainApplication.getInstance().getOrder().getWishValueAddition() > 0){
            Integer position = MainApplication.getInstance().getOrder().getWishValueAddition()/20;
            spValueAddition.setSelection(position);
        }

        swCheck.setChecked(MainApplication.getInstance().getOrder().getWishCheck());
        swConditioner.setChecked(MainApplication.getInstance().getOrder().getWishConditioner());
        swSmoke.setChecked(MainApplication.getInstance().getOrder().getWishSmoke());
        swNoSmoke.setChecked(MainApplication.getInstance().getOrder().getWishNoSmoke());
        swChildren.setChecked(MainApplication.getInstance().getOrder().getWishChildren());

        setIcons();

        spValueAddition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setIcons();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setIcons();

            }
        });

        swCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIcons();
            }
        });
        swConditioner.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIcons();
            }
        });
        swSmoke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIcons();
                if (b)swNoSmoke.setChecked(false);
            }
        });
        swNoSmoke.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIcons();
                if(b)swSmoke.setChecked(false);
            }
        });
        swChildren.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setIcons();
                if (b)swNoSmoke.setChecked(true);
            }
        });

        //if (swCheck.isChecked())f

        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        setWish();
    }

    public void setWish(){
        Boolean IsEdit = false;
        Integer newValue = spValueAddition.getSelectedItemPosition()*20;
        if (!MainApplication.getInstance().getOrder().getWishValueAddition().equals(newValue)){
            MainApplication.getInstance().getOrder().setWishValueAddition(newValue);
            IsEdit = true;
        }


        if ((MainApplication.getInstance().getOrder().getWishCheck() != swCheck.isChecked()) && (MainApplication.getInstance().getPreferences().getWishCheck() > 0))IsEdit = true;
        MainApplication.getInstance().getOrder().setWishCheck(swCheck.isChecked());

        if ((MainApplication.getInstance().getOrder().getWishConditioner() != swConditioner.isChecked()) && (MainApplication.getInstance().getPreferences().getWishConditioner() > 0))IsEdit = true;
        MainApplication.getInstance().getOrder().setWishConditioner(swConditioner.isChecked());

        if ((MainApplication.getInstance().getOrder().getWishSmoke() != swSmoke.isChecked()) && (MainApplication.getInstance().getPreferences().getWishSmoke() > 0))IsEdit = true;
        MainApplication.getInstance().getOrder().setWishSmoke(swSmoke.isChecked());

        if ((MainApplication.getInstance().getOrder().getWishNoSmoke() != swNoSmoke.isChecked()) && (MainApplication.getInstance().getPreferences().getWishNoSmoke() > 0))IsEdit = true;
        MainApplication.getInstance().getOrder().setWishNoSmoke(swNoSmoke.isChecked());

        if ((MainApplication.getInstance().getOrder().getWishChildren() != swChildren.isChecked()) && (MainApplication.getInstance().getPreferences().getWishChildren() > 0))IsEdit = true;
        MainApplication.getInstance().getOrder().setWishChildren(swChildren.isChecked());


        if (IsEdit)setResult(RESULT_OK);
        else setResult(RESULT_CANCELED);
        setResult(RESULT_OK);
        finish();
    }

    public void setIcons(){
        if (spValueAddition.getSelectedItemPosition() == 0)((ImageView)findViewById(R.id.ivWishActivityValueAddition)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_value_addition_ne));
        else ((ImageView)findViewById(R.id.ivWishActivityValueAddition)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_value_addition));
        if (swCheck.isChecked())((ImageView)findViewById(R.id.ivWishActivityCheck)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_check));
        else ((ImageView)findViewById(R.id.ivWishActivityCheck)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_check_ne));
        if (swConditioner.isChecked())((ImageView)findViewById(R.id.ivWishActivityConditioner)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_conditioner));
        else ((ImageView)findViewById(R.id.ivWishActivityConditioner)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_conditioner_ne));
        if (swSmoke.isChecked())((ImageView)findViewById(R.id.ivWishActivitySmoke)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_smoke));
        else ((ImageView)findViewById(R.id.ivWishActivitySmoke)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_smoke_ne));
        if (swNoSmoke.isChecked())((ImageView)findViewById(R.id.ivWishActivityNoSmoke)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_no_smoke));
        else ((ImageView)findViewById(R.id.ivWishActivityNoSmoke)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_no_smoke_ne));
        if (swChildren.isChecked())((ImageView)findViewById(R.id.ivWishActivityChildren)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_children));
        else ((ImageView)findViewById(R.id.ivWishActivityChildren)).setImageBitmap(MainApplication.getBitmap(this, R.mipmap.ic_conformation_wish_children_ne));
    }
}
