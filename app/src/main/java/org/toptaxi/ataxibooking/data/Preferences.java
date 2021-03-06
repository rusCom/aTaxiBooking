package org.toptaxi.ataxibooking.data;

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.tools.PlacesAPI;

import java.util.ArrayList;
import java.util.List;

public class Preferences {
    private static String TAG = "#########" + Preferences.class.getName();
    private Boolean PayTypeBonus, CalcTypeTaximeter, PaTypePromoCode, PriorOrder, ViewDistance = false, Share;
    private List<PayType> payTypes;
    private Integer WishValueAddition = -1, WishValueAdditionStep = 20, WishCheck = -1, WishConditioner = -1, WishSmoke = -1, WishNoSmoke = -1, WishChildren = -1, PriorTime = 45;
    private Integer UserAgreementVersion = 0, AndroidAppVersion = 0;
    private String UserAgreementLink = "", ShareText;
    private Location location;


    public Preferences() {
        PayTypeBonus        = false;
        CalcTypeTaximeter   = false;
        PaTypePromoCode     = false;
        PriorOrder          = false;
        ViewDistance        = false;
        Share               = false;
        ShareText           = "";
        PriorTime           = 45;
        location            = null;
        payTypes = new ArrayList<>();
        payTypes.add(new PayType("cash"));
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        //Log.d(TAG, "setFromJSON data = " + data.toString());
        payTypes.clear();
        JSONObject PayTypes = data.getJSONObject("pay_types");


        if (PayTypes.has("cash"))      if (PayTypes.getBoolean("cash")) payTypes.add(new PayType("cash"));
        if (PayTypes.has("corporate")) if (PayTypes.getBoolean("corporate")) payTypes.add(new PayType("corporate"));
        if (PayTypes.has("bonus"))     if (PayTypes.getBoolean("bonus")) {payTypes.add(new PayType("bonus"));PayTypeBonus = true;}
        if (PayTypes.has("promo_code"))if (PayTypes.getBoolean("promo_code")) {PaTypePromoCode = true;}
        if (PayTypes.has("card"))      if (PayTypes.getBoolean("card")) payTypes.add(new PayType("card"));


        if (data.has("calc_type_taximeter"))CalcTypeTaximeter = data.getBoolean("calc_type_taximeter");
        if (data.has("prior"))              PriorOrder = data.getBoolean("prior");
        if (data.has("prior_time"))         PriorTime = data.getInt("prior_time");
        if (data.has("view_distance"))      ViewDistance = data.getBoolean("view_distance");
        // Пользовательское соглашение
        if (data.has("user_agreement")){
            JSONObject UserAgreement = data.getJSONObject("user_agreement");
            UserAgreementVersion = UserAgreement.getInt("version");
            UserAgreementLink = UserAgreement.getString("link");
        }
        // Версия приложения
        if (data.has("android_app_version"))AndroidAppVersion = data.getInt("android_app_version");
        // LastLocation
        if (data.has("last_location")){
            JSONObject LastLocation = data.getJSONObject("last_location");
            location = new Location("LastLocation");
            location.setLatitude(LastLocation.getDouble("lt"));
            location.setLongitude(LastLocation.getDouble("ln"));
            //Log.d(TAG, "LastLocation = " + location.toString());
        }
        if (data.has("geo")){
            JSONObject geo = data.getJSONObject("geo");
            MainApplication.getInstance().getnDot().setGEO(geo.getString("ip"), geo.getInt("port"));
            new Thread(){
                @Override
                public void run() {
                    PlacesAPI.SetPopular(getLocation());
                }
            }.start();
        }

        if (data.has("share")){this.Share = data.getBoolean("share");}
        if (data.has("share_text")){this.ShareText = data.getString("share_text");}

        // Доп Услуги по заказам
        JSONObject wishTaxi = data.getJSONObject("wish").getJSONObject("taxi");
        if (wishTaxi.has("value_addition")){WishValueAddition      = wishTaxi.getInt("value_addition");}
        if (wishTaxi.has("addition_step")) {WishValueAdditionStep  = wishTaxi.getInt("addition_step");}
        if (wishTaxi.has("check"))         {WishCheck              = wishTaxi.getInt("check");}
        if (wishTaxi.has("conditioner"))   {WishConditioner        = wishTaxi.getInt("conditioner");}
        if (wishTaxi.has("smoke"))         {WishSmoke              = wishTaxi.getInt("smoke");}
        if (wishTaxi.has("no_smoke"))      {WishNoSmoke            = wishTaxi.getInt("no_smoke");}
        if (wishTaxi.has("children"))      {WishChildren           = wishTaxi.getInt("children");}
    }

    public Location getLocation() {
        return location;
    }

    public Boolean IsWishList(){
        Boolean result = false;
        if (getWishValueAddition() > 0) result = true;
        if (getWishCheck() >=0)         result = true;
        if (getWishConditioner() >=0)   result = true;
        if (getWishSmoke() >=0)         result = true;
        if (getWishNoSmoke() >=0)       result = true;
        if (getWishChildren() >=0)      result = true;
        return result;
    }

    Boolean getViewDistance() {
        return ViewDistance;
    }

    Integer getUserAgreementVersion() {
        return UserAgreementVersion;
    }

    public Boolean IsShare(){
        return (Share) && (!ShareText.equals(""));
    }

    public String getShareText() {
        return ShareText;
    }

    Integer getAndroidAppVersion() {
        return AndroidAppVersion;
    }

    public String getUserAgreementLink() {
        return UserAgreementLink;
    }

    public Integer getPriorTime() {
        return PriorTime;
    }

    public Boolean IsPrior(){return  PriorOrder;}

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
