package org.toptaxi.ataxibooking.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Preferences {
    private Boolean PayTypeBonus, CalcTypeTaximeter;
    private List<PayType> payTypes;


    public Preferences() {
        PayTypeBonus = false;
        CalcTypeTaximeter = false;
        payTypes = new ArrayList<>();
        payTypes.add(new PayType("type_cash"));
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        payTypes.clear();
        payTypes.add(new PayType("type_cash"));
        if (data.has("pay_type_corporate")) if (data.getBoolean("pay_type_corporate")) payTypes.add(new PayType("type_corporate"));
        if (data.has("pay_type_bonus"))     if (data.getBoolean("pay_type_bonus")) {payTypes.add(new PayType("type_bonus"));PayTypeBonus = true;}
        if (data.has("pay_type_card"))      if (data.getBoolean("pay_type_card")) payTypes.add(new PayType("type_card"));
        if (data.has("calc_type_taximeter"))CalcTypeTaximeter = data.getBoolean("calc_type_taximeter");
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
