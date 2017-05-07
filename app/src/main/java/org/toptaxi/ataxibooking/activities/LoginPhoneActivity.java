package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.tools.DOTResponse;

public class LoginPhoneActivity extends AppCompatActivity {
    private EditText edActivityLoginPhone;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        edActivityLoginPhone    = (EditText)findViewById(R.id.edActivityLoginPhone);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        edActivityLoginPhone.setText(sharedPreferences.getString("accountPhone", ""));

        edActivityLoginPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        findViewById(R.id.btnActivityLoginGetPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new GetPasswordTask(LoginPhoneActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
                checkSMSPermissions();
            }
        });

        edActivityLoginPhone.setSingleLine(true);
        edActivityLoginPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //new GetPasswordTask(LoginPhoneActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
                    checkSMSPermissions();
                }

                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.ACTIVITY_LOGIN_TOKEN){
            if (resultCode == RESULT_OK){setResult(RESULT_OK);finish();}
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
        else {
            new GetPasswordTask(LoginPhoneActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new GetPasswordTask(LoginPhoneActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edActivityLoginPhone.getText().toString().trim());
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accountPhone", edActivityLoginPhone.getText().toString().trim());
        editor.apply();
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
            return MainApplication.getInstance().getnDot().profile_login(strings[0], "SMS");
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                Intent loginIntent = new Intent(LoginPhoneActivity.this, LoginGetTokenActivity.class);
                loginIntent.putExtra("phone", edActivityLoginPhone.getText().toString());
                startActivityForResult(loginIntent, Constants.ACTIVITY_LOGIN_TOKEN);
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error");
            }
        }
    }
}
