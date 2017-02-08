package org.toptaxi.ataxibooking.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;
import org.toptaxi.ataxibooking.tools.DateTimeTools;

import java.util.Calendar;

public class DateTimePickerDialog extends Dialog implements View.OnClickListener {
    protected static String TAG = "#########" + DateTimePickerDialog.class.getName();
    private TextView tvDateTimePickerDate;
    private NumberPicker pckDate, pckHour, pckMinute;

    public interface OnDateTimePickerDialogListener{
        void DateTimePickerDialogChose(Calendar date);
    }

    private OnDateTimePickerDialogListener onDateTimePickerDialogListener;

    private String[] Date = {"Сегодня", "Завтра", "Послезавтра"};
    private String[] Hour = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};
    private String[] Minute = {"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};
    public DateTimePickerDialog(Context context, OnDateTimePickerDialogListener onDateTimePickerDialogListener) {
        super(context);
        this.setContentView(R.layout.date_time_picker_dialog);
        this.onDateTimePickerDialogListener = onDateTimePickerDialogListener;
        setTitle("Выбор времени заказа");

        findViewById(R.id.btnDateTimePickerOk).setOnClickListener(this);
        findViewById(R.id.btnDateTimePickerCancel).setOnClickListener(this);

        pckDate = (NumberPicker)findViewById(R.id.pckDate);
        pckDate.setMinValue(0);
        pckDate.setMaxValue(Date.length - 1);
        pckDate.setValue(0);
        pckDate.setDisplayedValues(Date);


        pckHour = (NumberPicker)findViewById(R.id.pckHour);
        pckHour.setMinValue(0);
        pckHour.setMaxValue(Hour.length - 1);
        pckHour.setDisplayedValues(Hour);

        pckMinute = (NumberPicker)findViewById(R.id.pckMinute);
        pckMinute.setMinValue(0);
        pckMinute.setMaxValue(Minute.length - 1);
        pckMinute.setDisplayedValues(Minute);

        setDate(MainApplication.getInstance().getOrder().getWorkDate());

    }


    public void setDate(Calendar date){
        if (date != null){
            if (DateTimeTools.isTomorrow(date)){pckDate.setValue(1);}
            if (DateTimeTools.isAfterTomorrow(date)){pckDate.setValue(2);}
            pckHour.setValue(date.get(Calendar.HOUR_OF_DAY));
            switch (date.get(Calendar.MINUTE)){
                case 0:pckMinute.setValue(0);break;
                case 5:pckMinute.setValue(1);break;
                case 10:pckMinute.setValue(2);break;
                case 15:pckMinute.setValue(3);break;
                case 20:pckMinute.setValue(4);break;
                case 25:pckMinute.setValue(5);break;
                case 30:pckMinute.setValue(6);break;
                case 35:pckMinute.setValue(7);break;
                case 40:pckMinute.setValue(8);break;
                case 45:pckMinute.setValue(9);break;
                case 50:pckMinute.setValue(10);break;
                case 55:pckMinute.setValue(11);break;
            }
        }
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 45);
            pckHour.setValue(calendar.get(Calendar.HOUR_OF_DAY));
            if (calendar.get(Calendar.MINUTE) < 5){pckMinute.setValue(0);}
            else if (calendar.get(Calendar.MINUTE) < 10){pckMinute.setValue(1);}
            else if (calendar.get(Calendar.MINUTE) < 15){pckMinute.setValue(2);}
            else if (calendar.get(Calendar.MINUTE) < 20){pckMinute.setValue(3);}
            else if (calendar.get(Calendar.MINUTE) < 25){pckMinute.setValue(4);}
            else if (calendar.get(Calendar.MINUTE) < 30){pckMinute.setValue(5);}
            else if (calendar.get(Calendar.MINUTE) < 35){pckMinute.setValue(6);}
            else if (calendar.get(Calendar.MINUTE) < 40){pckMinute.setValue(7);}
            else if (calendar.get(Calendar.MINUTE) < 45){pckMinute.setValue(8);}
            else if (calendar.get(Calendar.MINUTE) < 50){pckMinute.setValue(9);}
            else if (calendar.get(Calendar.MINUTE) < 55){pckMinute.setValue(10);}
            else if (calendar.get(Calendar.MINUTE) < 60){pckMinute.setValue(11);}
        }

    }


    public Calendar getTime(){
        //Log.d(TAG, "getTime pckMinute = " + Minute[pckMinute.getValue()]);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, pckDate.getValue());
        calendar.set(Calendar.HOUR_OF_DAY, pckHour.getValue());
        calendar.set(Calendar.MINUTE, Integer.parseInt(Minute[pckMinute.getValue()]));
        //Log.d(TAG, "getTime calendar = " + calendar.toString());
        return calendar;


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnDateTimePickerOk:
                if (onDateTimePickerDialogListener != null){
                    onDateTimePickerDialogListener.DateTimePickerDialogChose(getTime());
                }
                dismiss();
                break;
            case R.id.btnDateTimePickerCancel:
                if (onDateTimePickerDialogListener != null){
                    onDateTimePickerDialogListener.DateTimePickerDialogChose(null);
                }
                dismiss();
                break;
        }
    }
}
