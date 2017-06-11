package org.toptaxi.ataxibooking.data;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DOT {
    protected static String TAG = "#########" + DOT.class.getName();
    private Context mContext;
    private String AppToken, restIP, restPort;

    public DOT(Context context) {
        mContext = context;
        AppToken = context.getPackageName();
        restIP = context.getString(R.string.mainRestIP);
        restPort = context.getString(R.string.restPort);
    }

    public String sendData(String Type, String Data){
        String result = "{\"response\":\"httpError\", \"value\":\"" + MainApplication.getInstance().getResources().getString(R.string.errorConnection) + "\"}";;
        try {
            String URL = getRest() + "send=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
            if (MainApplication.getInstance().getLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
                URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
            }
            if (Type != null)
                if (!Type.equals(""))
                    URL += "&type=" + URLEncoder.encode(Type, "UTF-8");

            if (Data != null)
                if (!Data.equals(""))
                    URL += "&data=" + URLEncoder.encode(Data, "UTF-8");
            result = httpGet(URL);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getDataType(String Type, String Data){
        String result = "{\"response\":\"httpError\", \"value\":\"" + MainApplication.getInstance().getResources().getString(R.string.errorConnection) + "\"}";;
        try {
            String URL = getRest() + "gettype=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
            if (MainApplication.getInstance().getLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
                URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
            }
            if (Type != null)
                if (!Type.equals(""))
                    URL += "&type=" + URLEncoder.encode(Type, "UTF-8");

            if (Data != null)
                if (!Data.equals(""))
                    URL += "&data=" + URLEncoder.encode(Data, "UTF-8");
            result = httpGet(URL);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void getDrivers(){
        String result = "{\"response\":\"httpError\", \"value\":\"" + MainApplication.getInstance().getResources().getString(R.string.errorConnection) + "\"}";;
        try {
            String URL = getRest() + "gettype=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
            if (MainApplication.getInstance().getMapLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getMapLocation().latitude;
                URL += "&longitude=" + MainApplication.getInstance().getMapLocation().longitude;
            }
            else if (MainApplication.getInstance().getLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
                URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
            }
            URL += "&type=free_drivers";
            JSONObject response = new JSONObject(httpGet(URL));
            //Log.d(TAG, "getData response = " + response.toString());
            if (response.getString("response").equals("ok")){
                MainApplication.getInstance().parseData(response);
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public int getData(){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getRest() + "get=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
            if (MainApplication.getInstance().getLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
                URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
            }
            //Log.d(TAG, "getData URL = " + URL);
            JSONObject response = new JSONObject(httpGet(URL));
            //Log.d(TAG, "getData response = " + response.toString());
            if (response.getString("response").equals("ok")){
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().parseData(response);
            }
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }
        return result;
    }

    public void sendDataResult(String type, String data){
        try {
            String URL = getRest() + "send=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
            if (MainApplication.getInstance().getLocation() != null){
                URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
                URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
            }
            if (type != null)
                if (!type.equals(""))
                    URL += "&type=" + URLEncoder.encode(type, "UTF-8");
            if (data != null)
                if (!data.equals(""))
                    URL += "&data=" + URLEncoder.encode(data, "UTF-8");
            new SendDataResultTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private static class SendDataResultTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Log.d(TAG, "SendDataResultTask onPreExecute");
            if (MainApplication.getInstance().getMainActivity() != null){
                progressDialog = new ProgressDialog(MainApplication.getInstance().getMainActivity());
                progressDialog.setMessage("Передача данных ...");
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //Log.d(TAG, "SendDataResultTask doInBackground");
            try {
                return httpGet(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "httpError";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d(TAG, "SendDataResultTask onPostExecute = " + s);
            try{
                progressDialog.dismiss();
            }
            catch (Exception e){}
            if (s.equals("httpError")){
                MainApplication.getInstance().showToastType(Constants.DOT_HTTP_ERROR);

            }
            else {
                try {
                    JSONObject data = new JSONObject(s);
                    if (data.has("response")){
                        if (data.getString("response").equals("ok")){
                            MainApplication.getInstance().getDOT().getDataTask();
                        }
                        else {
                            MainApplication.getInstance().showToast(data.getString("value"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void getDataTask(){
        String URL = getRest() + "get=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
            URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
        }
        //Log.d(TAG, "getDataParseTask URl = " + URL);
        GetDataParseTypeTask sendDataTask = new GetDataParseTypeTask();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            sendDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        else
            sendDataTask.execute(URL);
    }

    public void getDataTypeTask(String Type, String Data) {
        String URL = getRest() + "gettype=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken();
        if (MainApplication.getInstance().getLocation() != null){
            URL += "&latitude=" + MainApplication.getInstance().getLocation().getLatitude();
            URL += "&longitude=" + MainApplication.getInstance().getLocation().getLongitude();
        }
        //Log.d(TAG, "getDataParseTask URl = " + URL);
        if (Type != null)
            if (!Type.equals(""))
                try {
                    URL += "&type=" + URLEncoder.encode(Type, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

        if (Data != null)
            if (!Data.equals(""))
                try {
                    URL += "&data=" + URLEncoder.encode(Data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
        GetDataParseTypeTask sendDataTask = new GetDataParseTypeTask();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
            sendDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URL);
        else
            sendDataTask.execute(URL);
    }

    private static class GetDataParseTypeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return httpGet(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (!s.equals("")){
                try {

                    MainApplication.getInstance().parseData(new JSONObject(s));
                    //Log.d(TAG, "GetDataParseTypeTask onPostExecute s = " + s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getPreferences(){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String rest = "http://" + restIP + ":" + restPort + "/";
            String URL = rest + "preferences=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken() + "&version=" + MainApplication.getInstance().getVersionCode();
            URL += "&location=" + MainApplication.getInstance().getLocationData();
            //Log.d(TAG, "getPreferences URL = " + URL.toString());
            JSONObject response = new JSONObject(httpGet(URL));
            //Log.d(TAG, "getPreferences response = " + response.toString());
            if (response.getString("response").equals("ok")){
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().parseData(response);
            }
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }

        //Log.d(TAG, "getPreferences result = " + result);

        // Если основной IP не пашет
        if (result == Constants.DOT_HTTP_ERROR){
            //Log.d(TAG, "getPreferences try reserveIP");
            try {
                String rest = "http://" + mContext.getString(R.string.reserveRestIP) + ":" + restPort + "/";
                String URL = rest + "preferences=" + AppToken + "&token=" + MainApplication.getInstance().getAccount().getToken() + "&version=" + MainApplication.getInstance().getVersionCode();
                //Log.d(TAG, "getPreferences URL = " + URL.toString());
                JSONObject response = new JSONObject(httpGet(URL));
                //Log.d(TAG, "getPreferences response = " + response.toString());
                if (response.getString("response").equals("ok")){
                    result = Constants.DOT_REST_OK;
                    MainApplication.getInstance().parseData(response);
                }
                if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
                if (response.getString("response").equals("identification_error"))result = Constants.DOT_IDENTIFICATION;
                if (response.getString("response").equals("new_version"))result = Constants.DOT_NEW_VERSION;
                restIP = mContext.getString(R.string.reserveRestIP);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
                result = Constants.DOT_REST_ERROR;
            }


        }
        return result;
    }


    public int getPassword(String phone){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getRest() + "getpass=" + AppToken + "&phone=" + URLEncoder.encode(phone, "UTF-8");
            Log.d(TAG, "getPassword URL = " + URL);
            JSONObject response = new JSONObject(httpGet(URL));
            Log.d(TAG, "getPassword response = " + response);
            if (response.getString("response").equals("ok"))result = Constants.DOT_REST_OK;
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            if (response.getString("response").equals("phone_wrong"))result = Constants.DOT_PHONE_WRONG;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }
        return result;
    }

    public int getToken(String phone, String password){
        int result = Constants.DOT_HTTP_ERROR;
        try {
            String URL = getRest() + "gettoken=" + AppToken + "&phone=" + URLEncoder.encode(phone, "UTF-8") + "&psw=" + URLEncoder.encode(password, "UTF-8");
            JSONObject response = new JSONObject(httpGet(URL));
            if (response.getString("response").equals("rest_error"))result = Constants.DOT_REST_ERROR;
            else if (response.getString("response").equals("phone_wrong"))result = Constants.DOT_PHONE_WRONG;
            else if (response.getString("response").equals("password_wrong"))result = Constants.DOT_PASSWORD_WRONG;
            else {
                result = Constants.DOT_REST_OK;
                MainApplication.getInstance().getAccount().setToken(response.getString("response"));
            }
            Log.d(TAG, "getToken response = " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            result = Constants.DOT_REST_ERROR;
        }

        return result;
    }

    private String getRest(){
        return "http://" + restIP + ":" + restPort + "/";
    }


    public static String httpGet(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        response.body().close();
        return result;
    }


}
