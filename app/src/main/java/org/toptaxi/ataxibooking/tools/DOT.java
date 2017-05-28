package org.toptaxi.ataxibooking.tools;

import android.content.Context;
import android.util.Log;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DOT {
    protected static String TAG = "#########" + org.toptaxi.ataxibooking.tools.DOT.class.getName();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Context mContext;
    private OkHttpClient httpClient;

    public DOT(Context mContext) {
        this.mContext = mContext;
        httpClient = new OkHttpClient();
    }

    public DOTResponse profile_login(String phone, String type){
        String method = "profile/login";
        String params = "key=" + mContext.getResources().getString(R.string.restToken);
        try {
            params += "&phone=" + URLEncoder.encode(phone, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params += "&type=" + type;
        return httpGet(method, params);
    }

    public DOTResponse profile_registration(String phone, String code){
        String method = "profile/registration";
        String params = "key=" + mContext.getResources().getString(R.string.restToken);
        try {
            params += "&phone=" + URLEncoder.encode(phone, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params += "&code=" + code;
        return httpGet(method, params);
    }

    public DOTResponse profile_check_phone(String phone){
        String method = "profile/check_phone";
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        try {
            params += "&phone=" + URLEncoder.encode(phone, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return httpGet(method, params);
    }

    public DOTResponse profile_check_phone_code(String phone, String code){
        String method = "profile/check_phone_code";
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        try {
            params += "&phone=" + URLEncoder.encode(phone, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params += "&code=" + code;
        return httpGet(method, params);
    }

    public DOTResponse profile_get(){
        return httpPost("profile/get", "token=" + MainApplication.getInstance().getAccount().getToken(), "");
    }

    public DOTResponse profile_set(String data){
        return httpPost("profile/set", "token=" + MainApplication.getInstance().getAccount().getToken(), data);
    }

    public DOTResponse data(){
        String method = "data";
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            params += "&lt=" + String.valueOf(MainApplication.getInstance().getLocation().getLatitude());
            params += "&ln=" + String.valueOf(MainApplication.getInstance().getLocation().getLongitude());
        }
        return httpGet(method, params);
    }

    public DOTResponse orders_history(String guid){
        String method = "orders/history";
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (!guid.equals(""))params += "&guid=" + guid;
        return httpGet(method, params);
    }

    public DOTResponse orders_calc(String calcJSON){
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            params += "&lt=" + String.valueOf(MainApplication.getInstance().getLocation().getLatitude());
            params += "&ln=" + String.valueOf(MainApplication.getInstance().getLocation().getLongitude());
        }
        return httpPost("orders/calc", params, calcJSON);
    }

    public DOTResponse orders_add(String note){
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            params += "&lt=" + String.valueOf(MainApplication.getInstance().getLocation().getLatitude());
            params += "&ln=" + String.valueOf(MainApplication.getInstance().getLocation().getLongitude());
        }
        if (!note.equals(""))params += "&note=" + note;
        return httpGet("orders/add", params);
    }

    public DOTResponse orders_deny(){
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            params += "&lt=" + String.valueOf(MainApplication.getInstance().getLocation().getLatitude());
            params += "&ln=" + String.valueOf(MainApplication.getInstance().getLocation().getLongitude());
        }

        return httpGet("orders/deny", params);
    }

    public DOTResponse preferences(){
        String method = "preferences";
        String params = "token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            params += "&lt=" + String.valueOf(MainApplication.getInstance().getLocation().getLatitude());
            params += "&ln=" + String.valueOf(MainApplication.getInstance().getLocation().getLongitude());
        }
        params += "&profile=true&data=true";
        //Log.d(TAG, "properties params=" + params);

        return httpGet(method, params);
    }

    private DOTResponse httpPost(String method, String params, String body){
        DOTResponse result = new DOTResponse(500);
        RequestBody requestBody = RequestBody.create(JSON, body);
        String main_url = "http://" + mContext.getResources().getString(R.string.mainRestIP) + ":" + mContext.getResources().getString(R.string.restPort) + "/" + method + "?" + params;
        String reserve_url = "http://" + mContext.getResources().getString(R.string.reserveRestIP) + ":" + mContext.getResources().getString(R.string.restPort) + "/" + method + "?" + params;

        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(main_url)
                    .post(requestBody)
                    .build();
            response = httpClient.newCall(request).execute();
            //Log.d(TAG, "httpGet main_ur success");
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d(TAG, "httpGet main_ur unsuccessful");
        }

        if (response == null){
            Request request = new Request.Builder()
                    .url(reserve_url)
                    .build();
            try {
                response = httpClient.newCall(request).execute();
                //Log.d(TAG, "httpGet reserve success");
            } catch (IOException e) {
                e.printStackTrace();
                //Log.d(TAG, "httpGet reserve unsuccessful");
            }
        }

        if (response != null){
            String responseBody = "";
            try {
                responseBody = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.Set(response.code(), responseBody);

        }

        return result;

    }

    private DOTResponse httpGet(String method, String params){
        DOTResponse result = new DOTResponse(500);

        String main_url = "http://" + mContext.getResources().getString(R.string.mainRestIP) + ":" + mContext.getResources().getString(R.string.restPort) + "/" + method + "?" + params;
        String reserve_url = "http://" + mContext.getResources().getString(R.string.reserveRestIP) + ":" + mContext.getResources().getString(R.string.restPort) + "/" + method + "?" + params;

        //Log.d(TAG, "httpGet main_url = " + main_url + ";reserve_url = " + reserve_url);


        //OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            Request request = new Request.Builder()
                    .url(main_url)
                    .build();
            response = httpClient.newCall(request).execute();
            //Log.d(TAG, "httpGet main_ur success");
        } catch (IOException e) {
            e.printStackTrace();
            //Log.d(TAG, "httpGet main_ur unsuccessful");
        }

        if (response == null){
            Request request = new Request.Builder()
                    .url(reserve_url)
                    .build();
            try {
                response = httpClient.newCall(request).execute();
                //Log.d(TAG, "httpGet reserve success");
            } catch (IOException e) {
                e.printStackTrace();
                //Log.d(TAG, "httpGet reserve unsuccessful");
            }
        }

        if (response != null){
            String responseBody = "";
            try {
                responseBody = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            result.Set(response.code(), responseBody);

        }

        return result;
    }
}
