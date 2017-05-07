package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.tools.DOTResponse;

public class LoginGetTokenActivity extends AppCompatActivity {
    private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private EditText edActivityLoginPassword;
    private boolean SMSReceived = false;
    private SMSReceive smsReceive;
    private TextView tvActivityLoginTimer;
    private LinearLayout llActivityLoginProgress, llActivityLoginGetPassword;
    ProgressBar pbActivityLogin;
    int Timer;
    String Phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_get_token);
        Phone = getIntent().getStringExtra("phone");
        ((TextView)findViewById(R.id.tvActivityLoginToken)).setText("Код был выслан на " + Phone);
        edActivityLoginPassword = (EditText)findViewById(R.id.edActivityLoginPassword);
        llActivityLoginProgress = (LinearLayout)findViewById(R.id.llActivityLoginProgress);
        llActivityLoginGetPassword = (LinearLayout)findViewById(R.id.llActivityLoginGetPassword);
        tvActivityLoginTimer    = (TextView)findViewById(R.id.tvActivityLoginTimer);
        pbActivityLogin         = (ProgressBar)findViewById(R.id.pbActivityLogin);

        llActivityLoginProgress.setVisibility(View.GONE);
        llActivityLoginGetPassword.setVisibility(View.GONE);

        //checkSMSPermissions();
        findViewById(R.id.btnActivityLoginGetToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getToken(view);
            }
        });

        edActivityLoginPassword.setSingleLine(true);
        edActivityLoginPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    getToken(null);
                }

                return false;
            }
        });

        startReceived();

        findViewById(R.id.btnActivityLoginGetPasswordSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] params = {Phone, "SMS"};
                new GetPasswordTask(LoginGetTokenActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        });

        findViewById(R.id.btnActivityLoginGetPasswordCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] params = {Phone, "CALL"};
                new GetPasswordTask(LoginGetTokenActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceive != null)unregisterReceiver(smsReceive);
    }

    public void startReceived(){
        if (smsReceive == null){
            smsReceive = new SMSReceive();
            IntentFilter intentFilter = new IntentFilter(SMS_ACTION);
            registerReceiver(smsReceive, intentFilter);
        }
        new TimerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    public void getToken(View view) {
        String[] params = {Phone, edActivityLoginPassword.getText().toString()};
        new GetTokenTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    private class GetTokenTask extends AsyncTask<String, Void, DOTResponse>{
        ProgressDialog progressDialog;

        GetTokenTask(Context mContext) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getResources().getString(R.string.pdCheckData));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected DOTResponse doInBackground(String... params) {
            return MainApplication.getInstance().getnDot().profile_registration(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                try {
                    JSONObject response = new JSONObject(result.getBody());
                    MainApplication.getInstance().getAccount().setToken(response.getString("token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult(RESULT_OK);
                finish();
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
        }
    }

    private class GetPasswordTask extends AsyncTask<String, Void, DOTResponse> {
        ProgressDialog progressDialog;

        GetPasswordTask(Context mContext) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getResources().getString(R.string.pdCheckData));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected DOTResponse doInBackground(String... strings) {
            return MainApplication.getInstance().getnDot().profile_login(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                startReceived();
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
        }
    }

    private class TimerTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Timer = 90;
            SMSReceived = false;
            llActivityLoginProgress.setVisibility(View.VISIBLE);
            llActivityLoginGetPassword.setVisibility(View.GONE);
            pbActivityLogin.setMax(90);
            //btnActivityLoginGetPassword.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while ((Timer > 0) & (!SMSReceived)){
                publishProgress();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Timer = Timer - 1;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            String message = getString(R.string.smsWait) + " " + String.valueOf(Timer) + " " + getString(R.string.reductionSek);
            tvActivityLoginTimer.setText(message);
            pbActivityLogin.setProgress(90-Timer);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            llActivityLoginProgress.setVisibility(View.GONE);
            llActivityLoginGetPassword.setVisibility(View.VISIBLE);
            //btnActivityLoginGetPassword.setVisibility(View.VISIBLE);
        }
    }



    public void checkSMSPermissions(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECEIVE_SMS)){
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS},
                        Constants.MY_PERMISSIONS_RECEIVE_SMS);
            }
            else{
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS},
                        Constants.MY_PERMISSIONS_RECEIVE_SMS);
            }
        }
    }

    private class SMSReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && SMS_ACTION.compareToIgnoreCase(intent.getAction()) == 0) {

                Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
                assert pduArray != null;
                SmsMessage[] messages = new SmsMessage[pduArray.length];
                for (int i = 0; i < pduArray.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
                }
                StringBuilder bodyText = new StringBuilder();
                for (SmsMessage message : messages) {
                    bodyText.append(message.getMessageBody());
                }
                String body = bodyText.toString();
                if (body.charAt(0) == ':'){
                    SMSReceived = true;
                    String pass = body.substring(1, 5);
                    edActivityLoginPassword.setText(pass);
                    getToken(null);
                }
            }

        }
    }
}
