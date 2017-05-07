package org.toptaxi.ataxibooking.data;


import android.os.Build;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.tools.DateTimeTools;
import org.toptaxi.ataxibooking.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Order {
    private static String TAG = "#########" + Order.class.getName();
    List<RoutePoint> routePoints;
    Integer Status = Constants.ORDER_STATE_NEW_ORDER, Distance, Duration, Price = 0, ID = 0,
            ServiceType = Constants.ORDER_SERVICE_TYPE_ECONOMY;
    String CalcID = "";
    private Calendar WorkDate;
    Driver driver;
    boolean IsMapDriverAnimate;
    private PayType payType;
    private Integer WishValueAddition = -1;
    private Boolean WishCheck = false, WishConditioner = false, WishSmoke = false, WishNoSmoke = false, WishChildren = false;
    private Boolean IsCanAdd = false, IsCalcSucces = false;


    public Order() {
        routePoints = new ArrayList<>();
        driver = new Driver();
        payType = new PayType("cash");
    }

    public String getServiceTypeName() {
        return MainApplication.getInstance().getResources().getStringArray(R.array.order_service_type)[ServiceType];
    }

    public Integer getWishValueAddition() {
        return WishValueAddition;
    }

    public void setWishValueAddition(Integer wishValueAddition) {
        WishValueAddition = wishValueAddition;
    }

    public Boolean getWishCheck() {
        return WishCheck;
    }

    public void setWishCheck(Boolean wishCheck) {
        WishCheck = wishCheck;
    }

    public Boolean getWishConditioner() {
        return WishConditioner;
    }

    public void setWishConditioner(Boolean wishConditioner) {
        WishConditioner = wishConditioner;
    }

    public Boolean getWishSmoke() {
        return WishSmoke;
    }

    public void setWishSmoke(Boolean wishSmoke) {
        WishSmoke = wishSmoke;
    }

    public Boolean getWishNoSmoke() {
        return WishNoSmoke;
    }

    public void setWishNoSmoke(Boolean wishNoSmoke) {
        WishNoSmoke = wishNoSmoke;
    }

    public Boolean getWishChildren() {
        return WishChildren;
    }

    public void setWishChildren(Boolean wishChildren) {
        WishChildren = wishChildren;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public Driver getDriver() {
        return driver;
    }

    public Boolean getCanAdd() {
        if (MainApplication.getInstance().getPreferences().getCalcTypeTaximeter())return true;
        return IsCalcSucces;
        /*
        if (IsCalcSucces){
            if (getRouteCount() == 1){
                MainApplication.getInstance().getPreferences().getCalcTypeTaximeter()))return true;
            }
            else return true;
        }
        else return false;
        */
    }

    public void setCalcSucces(Boolean calcSucces) {
        IsCalcSucces = calcSucces;
    }

    public void setFromJSON(JSONObject data) throws JSONException {
        //Log.d(TAG, "setFromJSON ID=" + this.ID + "("+data.getInt("id")+") status=" + this.Status + "("+ data.getInt("status") +")");
        if (data.has("uid")){
            if (data.getInt("uid") != this.ID){
                //Log.d(TAG, "setFromJSON changeID");
                setMapDriverAnimate(false);
                this.Status = Constants.ORDER_STATE_NEW_ORDER;
            }
            this.ID = data.getInt("uid");
        }
        if (data.has("status")){
            Integer status = Constants.ORDER_STATE_NEW_ORDER;
            switch (data.getString("status")){
                case "search_car":status = Constants.ORDER_STATE_SEARCH_DRIVER;break;
                case "drive_to_client":status = Constants.ORDER_STATE_DRIVE_TO_CLIENT;break;
                case "drive_at_client":status = Constants.ORDER_STATE_DRIVER_AT_CLIENT;break;
                case "driver_expect_client":status = Constants.ORDER_STATE_DRIVER_AT_CLIENT;break;
                case "paid_idle":status = Constants.ORDER_STATE_DRIVER_AT_CLIENT;break;
                case "client_in_car":status = Constants.ORDER_STATE_CLIENT_IN_CAR;break;
            }
            if (status != this.Status){
                //Log.d(TAG, "setFromJSON changeStatus");
                setMapDriverAnimate(false);
                if (status == Constants.ORDER_STATE_DRIVE_TO_CLIENT)MainApplication.getInstance().playSoundAssign();
                if (status == Constants.ORDER_STATE_DRIVER_AT_CLIENT)MainApplication.getInstance().playSoundDriveToClient();
                if (status == Constants.ORDER_STATE_CLIENT_IN_CAR)MainApplication.getInstance().playSoundAssign();

            }
            this.Status = status;
        }
        if (data.has("driver"))this.driver.setFromJSON(data.getJSONObject("driver"));
        routePoints.clear();
        if (data.has("route")){
            JSONArray route = data.getJSONArray("route");
            for (int itemID = 0; itemID < route.length(); itemID++){
                RoutePoint routePoint = new RoutePoint();
                routePoint.setFromJSON(route.getJSONObject(itemID));
                addRoutePoint(routePoint);
            }
        }
    }

    public PayType getPayType() {
        return payType;
    }

    public void setPayType(PayType payType) {
        this.payType = payType;
    }

    public String getCalcID() {
        return CalcID;
    }

    public LatLng getDriverLocation(){
        return driver.getLatLng();
    }



    public void addRoutePoint(RoutePoint routePoint){
        routePoints.add(routePoint);
        routePoint.addDatabaseHistory();
    }

    public int getRouteCount(){
        return routePoints.size();
    }

    public RoutePoint getRoutePoint(int position){
        if (getRouteCount() == 0)return null;
        if (position > getRouteCount())return null;
        return routePoints.get(position);
    }

    public void clear(){
        routePoints.clear();
        Status = Constants.ORDER_STATE_NEW_ORDER;
        Distance = 0;
        Duration = 0;
        Price = 0;
        IsMapDriverAnimate = false;
        payType.setType("cash");
        IsCalcSucces = false;
    }

    public boolean isMapDriverAnimate() {
        return IsMapDriverAnimate;
    }

    public void setMapDriverAnimate(boolean mapDriverAnimate) {
        IsMapDriverAnimate = mapDriverAnimate;
    }

    public boolean calcOrder() throws JSONException {
        if (calcDistance()){
            Status = Constants.ORDER_STATE_CALC_ORDER;
            //Log.d(TAG, "calcOrder = " + getCalcString());
            JSONObject calc = new JSONObject(MainApplication.getInstance().getDOT().getDataType("calc_order", getCalcString()));
            if (calc.getString("response").equals("ok")){
                Price = calc.getInt("cost");
                CalcID = calc.getString("calc_id");
            }
        }
        return true;
    }

    public Integer getPrice() {
        return Price;
    }

    public String getPriceString(){
        String result = "";
        String rub = " руб.";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            rub = String.valueOf(Html.fromHtml("&#x20bd", Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
        } else {
            rub = String.valueOf(Html.fromHtml("&#x20bd")); // or for older api
        }
        if (Price == 0){result = "По таксометру";}
        else {result = new DecimalFormat("##0").format(Price) + " " + rub;}

        if (MainApplication.getInstance().getPreferences().getViewDistance())result += "(" + getDistance() + ")";
        return result;
    }

    public Calendar getWorkDate() {
        return WorkDate;
    }

    public void setWorkDate(Calendar workDate) {
        WorkDate = workDate;
    }

    public String getPriorInfo(){
        String result = "На время";
        if (WorkDate != null){
            String hour = String.valueOf(WorkDate.get(Calendar.HOUR_OF_DAY));
            if (WorkDate.get(Calendar.HOUR_OF_DAY) < 10){hour = "0" + String.valueOf(WorkDate.get(Calendar.HOUR_OF_DAY));}
            String minute = String.valueOf(WorkDate.get(Calendar.MINUTE));
            if (WorkDate.get(Calendar.MINUTE) < 10){minute = "0" + String.valueOf(WorkDate.get(Calendar.MINUTE));}
            result = hour + ":" + minute;
            if (DateTimeTools.isTomorrow(WorkDate)){result = "Завтра на " + result;}
            else if (DateTimeTools.isAfterTomorrow(WorkDate)){result = "Послезавтра на " + result;}
            else if (!DateTimeTools.isCurDate(WorkDate)){
                result = WorkDate.get(Calendar.DAY_OF_MONTH) + " " + DateTimeTools.getSklonMonthName(WorkDate) + " на " + result;
            }
            else {result = "Сегодня на " + result;}

        }
        return result;
    }

    public String getWishsText(){
        String result = "";
        if (WishValueAddition > 0){result += "УВЕЛИЧЕНИЕ СТОИМОСТИ,";}
        if (WishCheck){result += "БСО,";}
        if (WishConditioner){result += "КОНДИЦИОНЕР,";}
        if (WishSmoke){result += "КУРЯЩИЙ САЛОН,";}
        if (WishNoSmoke){result += "НЕ КУРЯЩИЙ САЛОН,";}
        if (WishChildren){result += "С РЕБЕНКОМ,";}
        if (!result.equals("")){
            result = result.substring(0, result.length() - 1);
            //result = "(" + result + ")";
        }
        return result;

    }

    public Integer getStatus() {
        return Status;
    }

    public String getPriceDescription(){
        String result = "";
        switch (Status){
            case Constants.ORDER_STATE_NEW_ORDER:result = "Для расчета стоимости выберите куда Вам надо ехать.";break;
            case Constants.ORDER_STATE_CALC_ORDER:
                //Log.d(TAG, "getPriceDescription duration = " + Duration);
                result = "Дистанция: " + getDistance() + ". Ориентировочное время в пути: " + getDuration();
                break;
        }
        return result;
    }

    public Boolean setCalcData(String dataString){
        Boolean result = false;
        try {
            JSONObject data = new JSONObject(dataString);
            Price = data.getInt("price");
            CalcID = data.getString("uid");
            if (data.has("distance"))Distance = data.getInt("distance");
            if (data.has("duration"))Duration = data.getInt("duration");
            result = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setCalcSucces(result);
        return result;
    }

    public String getCalcJSON()  {
        JSONObject data = new JSONObject();
        try {
            if (WorkDate != null){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
                data.put("date", sdf.format(WorkDate.getTime()));
            }
            data.put("category", "economy");
            data.put("distance", Distance);
            data.put("duration", Duration);
            data.put("pay", payType.getType());
            JSONObject wish = new JSONObject();
            if (WishValueAddition > 0){wish.put("value_addition", String.valueOf(WishValueAddition));}
            if (WishCheck)wish.put("check", "true");
            if (WishConditioner)wish.put("conditioner", "true");
            if (WishSmoke)wish.put("smoke", "true");
            if (WishNoSmoke)wish.put("no_smoke", "true");
            if (WishChildren)wish.put("children", "true");
            data.put("wish", wish);
            JSONArray route = new JSONArray();
            for (int itemID = 0; itemID < routePoints.size(); itemID++){
                route.put(routePoints.get(itemID).getCalcJSON());
            }
            data.put("route", route);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getCalcJSON " + data.toString());

        return data.toString();
    }

    private String getCalcString(){
        String result = MainApplication.getInstance().getPackageName() + "|";
        result += MainApplication.getInstance().getAccount().getToken() + "|";
        result += Status + "|";
        if (WorkDate != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
            result += sdf.format(WorkDate.getTime()) + "|";
        }
        else {result += "|";}
        result += getRouteCount() + "|";
        result += Distance + "|";
        result += Duration + "|";

        if (MainApplication.getInstance().getLocation() != null){
            result += MainApplication.getInstance().getLocation().getLatitude() + "|";
            result += MainApplication.getInstance().getLocation().getLongitude() + "|";
        }
        else {result += "0|0|";}

        if (MainApplication.getInstance().getMapLocation() != null){
            result += MainApplication.getInstance().getMapLocation().latitude + "|";
            result += MainApplication.getInstance().getMapLocation().longitude + "|";
        }
        else {result += "0|0|";}


        result += "^";
        for (int itemID = 0; itemID < getRouteCount(); itemID++){
            result += getRoutePoint(itemID).getCalcString() + "#";
        }
        result += "^";
        return result;
    }

    private String getDuration(){
        String result = "";
        Integer hour = Duration / 3600;
        Integer min = (Duration - hour/3600)/60;
        Integer sek = Duration - (min * 60);
        min = min - (hour * 60);

        String s_min = String.valueOf(min);
        if (min < 10)s_min = "0" + String.valueOf(min);
        String s_sek = String.valueOf(sek);
        if (sek < 10)s_sek = "0" + String.valueOf(sek);

        if (hour > 0){
            if (hour < 10)result = "0" + String.valueOf(hour) + ":" + s_min + ":" + s_sek;
            else result = String.valueOf(hour) + ":" + s_min + ":" + s_sek;
        }
        else{
            result = s_min + ":" + s_sek;
        }

        //Log.d(TAG, "getDuration hour = " + hour + "; min = " + min + "; sek = " + sek);

        return result;
    }

    private String getDistance(){
        if (Distance == 0){return  "";}
        String result = "";
        if (Distance < 1000)result += new DecimalFormat("##0").format(Distance) + " м.";
        else {result += new DecimalFormat("##0.00").format(Distance/1000.0) + " км.";}
        return result;
    }

    public boolean calcDistance(){
        boolean isCalc = false;
        Distance = 0;
        Duration = 0;
        if (getRouteCount() > 1){
            String request = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + getRoutePoint(0).getLatitude() + "," + getRoutePoint(0).getLongitude() + "&destinations=";
            for (int itemID = 1; itemID < getRouteCount(); itemID++){
                request += getRoutePoint(itemID).getLatitude() + "," + getRoutePoint(itemID).getLongitude();
                if (itemID < (getRouteCount() - 1))request += "|";
            }
            //request += "&key=" + MainApplication.getInstance().getResources().getString(R.string.web_google_maps_key);
            request += "&language=" + Locale.getDefault();

            //Log.d(TAG, "calcDistance request = " + request);
            try {
                URL url = new URL(request);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String resultJson = buffer.toString();
                //Log.d(TAG, "calcDistance resultJson = " + resultJson);
                JSONObject jsonObject = new JSONObject(resultJson);
                if (jsonObject.getString("status").equals("OK")){
                    JSONArray elements = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
                    for (int itemID = 0; itemID < elements.length(); itemID++){
                        Distance += elements.getJSONObject(itemID).getJSONObject("distance").getInt("value");
                        Duration += elements.getJSONObject(itemID).getJSONObject("duration").getInt("value");
                    }
                }
                isCalc = true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return isCalc;
    }

}
