package org.toptaxi.ataxibooking.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Preferences {
    private static String TAG = "#########" + Preferences.class.getName();
    private Boolean PayTypeBonus, CalcTypeTaximeter;
    private List<PayType> payTypes;
    private Integer WishValueAddition = -1, WishCheck = -1, WishConditioner = -1, WishSmoke = -1, WishNoSmoke = -1, WishChildren = -1;


    public Preferences() {
        PayTypeBonus = false;
        CalcTypeTaximeter = false;
        payTypes = new ArrayList<>();
        payTypes.add(new PayType("type_cash"));
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        //Log.d(TAG, "setFromJSON data = " + data.toString());
        payTypes.clear();
        payTypes.add(new PayType("type_cash"));
        if (data.has("pay_type_corporate")) if (data.getBoolean("pay_type_corporate")) payTypes.add(new PayType("type_corporate"));
        if (data.has("pay_type_bonus"))     if (data.getBoolean("pay_type_bonus")) {payTypes.add(new PayType("type_bonus"));PayTypeBonus = true;}
        if (data.has("pay_type_card"))      if (data.getBoolean("pay_type_card")) payTypes.add(new PayType("type_card"));
        if (data.has("calc_type_taximeter"))CalcTypeTaximeter = data.getBoolean("calc_type_taximeter");
        // Доп Услуги по заказам
        if (data.has("wish_value_addition")){WishValueAddition  = data.getInt("wish_value_addition");}
        if (data.has("wish_check"))         {WishCheck          = data.getInt("wish_check");}
        if (data.has("wish_conditioner"))   {WishConditioner    = data.getInt("wish_conditioner");}
        if (data.has("wish_smoke"))         {WishSmoke          = data.getInt("wish_smoke");}
        if (data.has("wish_no_smoke"))      {WishNoSmoke        = data.getInt("wish_no_smoke");}
        if (data.has("wish_children"))      {WishChildren       = data.getInt("wish_children");}
    }

    public Boolean IsWishList(){
        Boolean result = false;
        if (getWishValueAddition() >=0) result = true;
        if (getWishCheck() >=0)         result = true;
        if (getWishConditioner() >=0)   result = true;
        if (getWishSmoke() >=0)         result = true;
        if (getWishNoSmoke() >=0)       result = true;
        if (getWishChildren() >=0)      result = true;
        return result;
    }

    public Integer getWishValueAddition() {
        return WishValueAddition;
    }

    public Integer getWishCheck() {
        return WishCheck;
    }

    public Integer getWishConditioner() {
        return WishConditioner;
    }

    public Integer getWishSmoke() {
        return WishSmoke;
    }

    public Integer getWishNoSmoke() {
        return WishNoSmoke;
    }

    public Integer getWishChildren() {
        return WishChildren;
    }

    public Boolean getPayTypeBonus() {
        return PayTypeBonus;
    }


    public Boolean getCalcTypeTaximeter() {
        return CalcTypeTaximeter;
    }

    public Boolean IsHavePaymentTypes(){
        return  !(payTypes.size() == 1);
    }

    public List<PayType> getPayTypes() {
        return payTypes;
    }
}
