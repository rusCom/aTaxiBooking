package org.toptaxi.ataxibooking.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.data.Constants;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.DOTResponse;

public class AccountActivity extends AppCompatActivity {
    private static String TAG = "#########" + AccountActivity.class.getName();
    AlertDialog dialog;
    EditText edMail, edName, edPhone, edCode;
    LinearLayout llConfirmPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        edMail = (EditText)findViewById(R.id.edAccountActivityMail);
        edName = (EditText)findViewById(R.id.edAccountActivityName);
        edPhone = (EditText)findViewById(R.id.edAccountActivityPhone);
        edCode = (EditText)findViewById(R.id.edAccountActivityCode);

        findViewById(R.id.btnTitleRight).setBackgroundResource(R.drawable.ic_check);

        llConfirmPhone = (LinearLayout)findViewById(R.id.llAccountActivityConfirmPhone);

        edPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        edMail.setText(MainApplication.getInstance().getAccount().getMail());
        edName.setText(MainApplication.getInstance().getAccount().getName());
        edPhone.setText(MainApplication.getInstance().getAccount().getPhone());
        edCode.setText("");

        llConfirmPhone.setVisibility(View.GONE);

        findViewById(R.id.btnTitleLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.btnTitleRight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRightButtonClick(null);
            }
        });

    }

    public boolean isChangeData(){
        if (!MainApplication.getInstance().getAccount().getMail().equals(edMail.getText().toString()))return true;
        if (!MainApplication.getInstance().getAccount().getName().equals(edName.getText().toString()))return true;
        return false;
    }

    public boolean validateData(){
        if (!validateMail())return false;
        return true;
    }

    public boolean validateMail(){
        if (!edMail.getText().toString().equals("") && !isValidEmail(((EditText)findViewById(R.id.edAccountActivityMail)).getText())){
            ((TextInputLayout)findViewById(R.id.ilAccountActivityMail)).setError(getResources().getString(R.string.edAccountActivityMailError));
            findViewById(R.id.edAccountActivityMail).requestFocus();
            return false;
        }
        else {
            ((TextInputLayout)findViewById(R.id.ilAccountActivityMail)).setErrorEnabled(false);
        }
        return true;
    }

    public void btnAccountActivityConfirmPhoneClick(View view){
        String[] params = {edPhone.getText().toString(), edCode.getText().toString()};
        new CheckPasswordTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params); //edCode.getText().toString());

    }

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void onRightButtonClick(View v){
        if (dialog != null && dialog.isShowing())dialog.dismiss();
        if (validateData()){
            // Если был изменен номер телефона
            if (!MainApplication.getInstance().getAccount().getPhone().equals(edPhone.getText().toString().replace(" ", "").replace("-", "").trim())){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage(getResources().getString(R.string.dlgAccountActivityPhoneChange));
                alertDialog.setPositiveButton(getResources().getString(R.string.dlgAccountActivityPhoneChangeConfirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {confirmPhoneSuccessClick();}
                });
                alertDialog.setNegativeButton(getResources().getString(R.string.dlgNo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {confirmPhoneUnSuccessClick();}
                });
                alertDialog.create();
                dialog = alertDialog.show();
            }
            else if (isChangeData()){
                JSONObject data = new JSONObject();
                try {
                    data.put("name", edName.getText().toString());
                    data.put("email", edMail.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //String accountData = edName.getText().toString() + "|" + edMail.getText().toString() + "|";
                (new SaveAccountDataTask(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.toString());
            }

        }
    }

    public void confirmPhoneSuccessClick(){
        new GetPasswordTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edPhone.getText().toString());
    }

    public void confirmPhoneUnSuccessClick(){
        if (dialog != null && dialog.isShowing())dialog.dismiss();
        edPhone.setText(MainApplication.getInstance().getAccount().getPhone());
    }

    public void CloseActivity(){
        if (dialog != null && dialog.isShowing())dialog.dismiss();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isChangeData()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.dlgAccountActivitySaveData));
            alertDialog.setPositiveButton(getResources().getString(R.string.dlgYes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {onRightButtonClick(null);}
            });
            alertDialog.setNegativeButton(getResources().getString(R.string.dlgNo), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CloseActivity();
                }
            });
            alertDialog.create();
            dialog = alertDialog.show();
        }
        else {super.onBackPressed();}
    }

    private class CheckPasswordTask extends AsyncTask<String, Void, DOTResponse>{
        ProgressDialog progressDialog;

        CheckPasswordTask(Context mContext) {
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
            return MainApplication.getInstance().getnDot().profile_check_phone_code(strings[0], strings[1]);
            //return MainApplication.getInstance().getDOT().getDataType("check_new_phone_code", strings[0]);
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                llConfirmPhone.setVisibility(View.GONE);
                MainApplication.getInstance().getAccount().setPhone(edPhone.getText().toString());
                //edPhone.setText(MainApplication.getInstance().getAccount().getPhone());
                edCode.setText("");
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error " + String.valueOf(result.getCode()));
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
            return MainApplication.getInstance().getnDot().profile_check_phone(strings[0]);
            //return MainApplication.getInstance().getDOT().getDataType("check_new_phone", strings[0]);
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            if (result.getCode() == 200){
                llConfirmPhone.setVisibility(View.VISIBLE);
            }
            else if ((result.getCode() == 400) && (!result.getBody().equals("")))  {
                MainApplication.getInstance().showToast(result.getBody());
            }
            else {
                MainApplication.getInstance().showToast("HTTP Error " + String.valueOf(result.getCode()));
            }
        }
    }



    private class SaveAccountDataTask extends AsyncTask<String, Void, DOTResponse>{
        ProgressDialog progressDialog;

        SaveAccountDataTask(Context mContext) {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage(getResources().getString(R.string.pdSaveAccountDataTask));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected DOTResponse doInBackground(String... strings) {
            return MainApplication.getInstance().getnDot().profile_set(strings[0]);
            /*
            Boolean result = false;
            try {
                JSONObject resultData = new JSONObject(MainApplication.getInstance().getDOT().getDataType("set_account_data", strings[0]));
                if (resultData.getString("response").equals("ok")){
                    result = true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
            */
        }

        @Override
        protected void onPostExecute(DOTResponse result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())progressDialog.dismiss();
            showResultDialog(result.getCode() == 200);
        }
    }

    public void showResultDialog(Boolean result){
        if (result){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.dlgAccountActivitySaveDataSuccess));
            alertDialog.setPositiveButton(getResources().getString(R.string.dlgOk), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            alertDialog.create();
            alertDialog.show();
            MainApplication.getInstance().getDOT().getDataTypeTask("account", "");
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.dlgAccountActivitySaveDataUnSuccess));
            alertDialog.setPositiveButton(getResources().getString(R.string.dlgOk), null);
            alertDialog.create();
            alertDialog.show();
        }
    }
}
