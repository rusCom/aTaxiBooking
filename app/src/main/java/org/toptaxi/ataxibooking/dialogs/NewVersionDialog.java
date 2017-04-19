package org.toptaxi.ataxibooking.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

public class NewVersionDialog extends Dialog implements View.OnClickListener {

    public NewVersionDialog(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_new_version);
        this.setCanceledOnTouchOutside(false);
        findViewById(R.id.btnNewVersionOk).setOnClickListener(this);
        findViewById(R.id.btnNewVersionCancel).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnNewVersionOk){
            final String appPackageName = MainApplication.getInstance().getPackageName();
            try {
                MainApplication.getInstance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (android.content.ActivityNotFoundException anfe) {
                MainApplication.getInstance().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
        dismiss();
    }
}
