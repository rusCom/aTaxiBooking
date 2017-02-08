package org.toptaxi.ataxibooking.data;

import org.toptaxi.ataxibooking.MainApplication;
import org.toptaxi.ataxibooking.R;

public class PayType {
    private String Type;

    public PayType(String type) {
        Type = type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getType() {
        return Type;
    }

    public String getCaption(){
        switch (Type){
            case "type_cash":return "Наличные";
            case "type_corporate":return "Корпоративная поездка";
            case "type_bonus":return "Бонусы";
            case "type_card":return "Банковская карта";
        }
        return "";
    }

    public String getCardCaption(){
        switch (Type){
            case "type_cash":return "Наличный расчет";
            case "type_corporate":return "Поездка за счет организации";
            case "type_bonus":return "Оплата бонусами";
            case "type_card":return "Оплата по банковской карте";
        }
        return "";
    }

    public String getCardDescription(){
        switch (Type){
            case "type_cash":       return "Оплата наличными деньгами непосредственно водителю";
            case "type_corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return "Оплата поездки за корпоративный счет Вашей организации";
                                    else return "Если Ваша организация уже является нашим корпоративным клиентом, обратитесь к Вашему персональному менеджеру. Если Вы хатите заключить договор на Корпоративное обслуживание напишите нам. Мы обязательно с Вами свяжемся.";
            case "type_bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return "Поздравляем, Вы накопили достаточное количество бонусов для оплаты.";
                                    else return "Пользуйтесь нашими услугами, копите бонусы, оплачивайте бонусами.";
            case "type_card":       return "Оплата по банковской карте будет доступна в ближайше время, мы Вас обязательно оповестим";
        }
        return "";
    }

    public int getCardImage(){
        switch (Type){
            case "type_cash":       return R.mipmap.ic_conformation_pay_type_cash;
            case "type_corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return R.mipmap.ic_conformation_pay_type_corporate;
            else return R.mipmap.ic_conformation_pay_type_corporate_ne;
            case "type_bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return R.mipmap.ic_conformation_pay_type_bonus;
            else return R.mipmap.ic_conformation_pay_type_bonus_ne;
            case "type_card":       return R.mipmap.ic_conformation_pay_type_card_ne;
        }
        return R.mipmap.ic_conformation;
    }

    public int getButtonImage(){
        switch (Type){
            case "type_cash":       return R.mipmap.ic_conformation_pay_type_cash_button;
            case "type_corporate":  return R.mipmap.ic_conformation_pay_type_corporate_button;
            case "type_bonus":      return R.mipmap.ic_conformation_pay_type_bonus_button;
            case "type_card":       return R.mipmap.ic_conformation_pay_type_card_button;
        }
        return R.mipmap.ic_conformation;
    }

    public String clickReturnType(){
        switch (Type){
            case "type_cash":       return "return";
            case "type_corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return "return";
            else return "";
            case "type_bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return "return";
            else return "";
            case "type_card":       return "";
        }
        return "";
    }

}
