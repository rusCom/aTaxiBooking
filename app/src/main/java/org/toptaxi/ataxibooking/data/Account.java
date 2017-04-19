package org.toptaxi.ataxibooking.data;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;

public class Account {
    protected static String TAG = "#########" + Account.class.getName();
    private String Token, Name, eMail, Phone;
    private Double Balance;
    private Boolean PayTypeCorporate;
    private Integer UserAgreementVersion = 0;

    public Account(String token) {
        Token = token;
        PayTypeCorporate = false;
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        UserAgreementVersion = sPref.getInt("user_agreement_version", 0);

    }

    public void setFromJSON(JSONObject data) throws JSONException {
        if (data.has("name"))this.Name = data.getString("name");
        if (data.has("email"))this.eMail = data.getString("email");
        if (data.has("phone"))this.Phone = data.getString("phone");
        if (data.has("balance"))this.Balance = data.getDouble("balance");
        if (data.has("pay_type_corporate"))this.PayTypeCorporate = data.getBoolean("pay_type_corporate");
    }

    public void setUserAgreementApply(){
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putInt("user_agreement_version", MainApplication.getInstance().getPreferences().getUserAgreementVersion());
        editor.apply();
    }

    public Boolean IsShowUserAgreement(){
        Boolean result = false;
        if (MainApplication.getInstance().getPreferences().getUserAgreementVersion() > 0){
            if (!MainApplication.getInstance().getPreferences().getUserAgreementLink().equals("")){
                if (MainApplication.getInstance().getPreferences().getUserAgreementVersion() > UserAgreementVersion){
                    result = true;
                }
            }
        }
        return result;
    }

    public String getNameForDrawer(){
        if (Name != null && !Name.equals(""))return Name;
        return Phone;
    }

    public Boolean getPayTypeCorporate() {
        return PayTypeCorporate;
    }

    public Double getBalance() {
        return Balance;
    }

    public String getName() {
        return Name;
    }

    public String getPhone() {
        return Phone;
    }

    public String getMail(){
        if (eMail != null)return eMail;
        return "";
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public void setAccountData(String Name, String eMail){
        this.Name = Name;
        this.eMail = eMail;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(MainApplication.getInstance());
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("accountToken", token);
        editor.apply();
        Token = token;
    }
}
