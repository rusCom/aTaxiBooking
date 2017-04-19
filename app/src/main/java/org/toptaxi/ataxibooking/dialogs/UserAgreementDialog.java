package org.toptaxi.ataxibooking.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

public class UserAgreementDialog extends Dialog implements View.OnClickListener {
    protected static String TAG = "#########" + UserAgreementDialog.class.getName();

    public UserAgreementDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_user_agreement);
        this.setCanceledOnTouchOutside(false);
        String Link = "<a href=" + MainApplication.getInstance().getPreferences().getUserAgreementLink() + ">адресу</a>";
        ((TextView)findViewById(R.id.tvUserAgreementLink)).setText(Html.fromHtml("С полным текстом Вы можете ознакомиться по " + Link + " в сети Интернет."));
        ((TextView)findViewById(R.id.tvUserAgreementLink)).setMovementMethod(LinkMovementMethod.getInstance());
        findViewById(R.id.btnUserAgreementApply).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {

    }


    @Override
    public void onClick(View view) {
        MainApplication.getInstance().getAccount().setUserAgreementApply();
        dismiss();
    }
}
