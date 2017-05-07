package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.tools.DOTResponse;

public class LoginActivity extends AppCompatActivity {
    private static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private EditText edActivityLoginPhone, edActivityLoginPassword;
    private TextInputLayout ilActivityLoginPhone, ilActivityLoginPassword;
    private LinearLayout llActivityLoginProgress;
    private TextView tvActivityLoginTimer;
    private Button btnActivityLoginGetPassword;
    private SharedPreferences sharedPreferences;
    private boolean IsFirstGetPassword = true, SMSRecieved = false;
    private SMSReceive smsReceive;
    int Timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edActivityLoginPhone    = (EditText)findViewById(R.id.edActivityLoginPhone);
        edActivityLoginPassword = (EditText)findViewById(R.id.edActivityLoginPassword);
        ilActivityLoginPhone    = (TextInputLayout)findViewById(R.id.ilActivityLoginPhone);
        ilActivityLoginPassword = (TextInputLayout)findViewById(R.id.ilActivityLoginPassword);
        llActivityLoginProgress = (LinearLayout)findViewById(R.id.llActivityLoginProgress);
        tvActivityLoginTimer    = (TextView)findViewById(R.id.tvActivityLoginTimer);
        btnActivityLoginGetPassword = (Button)findViewById(R.id.btnActivityLoginGetPassword);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edActivityLoginPhone.setText(sharedPreferences.getString("accountPhone", ""));

        Button okButton = (Button)findViewById(R.id.btnActivityLoginGetToken);
        okButton.setTextSize((float) (okButton.getTextSize() * 1.5));

        llActivityLoginProgress.setVisibility(View.GONE);

        edActivityLoginPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        checkSMSPermissions();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceive != null)unregisterReceiver(smsReceive);
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



    public void getToken(View view) {
        if (!validatePhone())return;
        if (!validatePassword())return;
        String[] params = {edActivityLoginPhone.getText().toString(), edActivityLoginPassword.getText().toString()};
        new GetTokenTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    private boolean validatePassword(){
        if (edActivityLoginPassword.getText().toString().trim().equals("")){
            ilActivityLoginPassword.setError(getString(R.string.errorPasswordNotEntered));
            edActivityLoginPassword.requestFocus();
            return false;
        }
        else {
            ilActivityLoginPassword.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validatePhone(){
        if (edActivityLoginPhone.getText().toString().trim().equals("")){
            ilActivityLoginPhone.setError(getString(R.string.errorPhoneNotEntered));
            edActivityLoginPhone.requestFocus();
            return false;
        }
        else {
            ilActivityLoginPhone.setErrorEnabled(false);
        }
        return true;
    }


    public void getPassword(View view) {
        if (validatePhone()){
            if (IsFirstGetPassword){
                new GetPasswordTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());

            }
            else {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Подтверждение")
                        .setMessage("Выслать смс повторно?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new GetPasswordTask(LoginActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
                            }

                        })
                        .setNegativeButton("Нет", null)
                        .show();

            }

        }
    }

    private class GetPasswordTask extends AsyncTask<String, Void, DOTResponse>{
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
            return MainApplication.getInstance().getnDot().profile_login(strings[0], "SMS");
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                if (smsReceive == null){
                    smsReceive = new SMSReceive();
                    IntentFilter intentFilter = new IntentFilter(SMS_ACTION);
                    registerReceiver(smsReceive, intentFilter);
                }
                new TimerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                IsFirstGetPassword = false;
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
        }
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



    private class TimerTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Timer = 90;
            SMSRecieved = false;
            llActivityLoginProgress.setVisibility(View.VISIBLE);
            btnActivityLoginGetPassword.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while ((Timer > 0) & (!SMSRecieved)){
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
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            llActivityLoginProgress.setVisibility(View.GONE);
            btnActivityLoginGetPassword.setVisibility(View.VISIBLE);
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
                    SMSRecieved = true;
                    String pass = body.substring(1, 5);
                    edActivityLoginPassword.setText(pass);
                    getToken(null);
                }
            }

        }
    }
}
