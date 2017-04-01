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
            case "cash":return "Наличные";
            case "corporate":return "Корпоративная поездка";
            case "bonus":return "Бонусы";
            case "card":return "Банковская карта";
        }
        return "";
    }

    public String getCardCaption(){
        switch (Type){
            case "cash":return "Наличный расчет";
            case "corporate":return "Поездка за счет организации";
            case "bonus":return "Оплата бонусами";
            case "card":return "Оплата по банковской карте";
        }
        return "";
    }

    public String getCardDescription(){
        switch (Type){
            case "cash":       return "Оплата наличными деньгами непосредственно водителю";
            case "corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return "Оплата поездки за корпоративный счет Вашей организации";
                                    else return "Если Ваша организация уже является нашим корпоративным клиентом, обратитесь к Вашему персональному менеджеру. Если Вы хатите заключить договор на Корпоративное обслуживание напишите нам. Мы обязательно с Вами свяжемся.";
            case "bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return "Поздравляем, Вы накопили достаточное количество бонусов для оплаты.";
                                    else return "Пользуйтесь нашими услугами, копите бонусы, оплачивайте бонусами.";
            case "card":       return "Оплата по банковской карте будет доступна в ближайше время, мы Вас обязательно оповестим";
        }
        return "";
    }

    public int getCardImage(){
        switch (Type){
            case "cash":       return R.mipmap.ic_conformation_pay_type_cash;
            case "corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return R.mipmap.ic_conformation_pay_type_corporate;
            else return R.mipmap.ic_conformation_pay_type_corporate_ne;
            case "bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return R.mipmap.ic_conformation_pay_type_bonus;
            else return R.mipmap.ic_conformation_pay_type_bonus_ne;
            case "card":       return R.mipmap.ic_conformation_pay_type_card_ne;
        }
        return R.mipmap.ic_conformation;
    }

    public int getButtonImage(){
        switch (Type){
            case "cash":       return R.mipmap.ic_conformation_pay_type_cash_button;
            case "corporate":  return R.mipmap.ic_conformation_pay_type_corporate_button;
            case "bonus":      return R.mipmap.ic_conformation_pay_type_bonus_button;
            case "card":       return R.mipmap.ic_conformation_pay_type_card_button;
        }
        return R.mipmap.ic_conformation;
    }

    public String clickReturnType(){
        switch (Type){
            case "cash":       return "return";
            case "corporate":  if (MainApplication.getInstance().getAccount().getPayTypeCorporate())return "return";
            else return "";
            case "bonus":      if (MainApplication.getInstance().getAccount().getBalance() > MainApplication.getInstance().getOrder().getPrice())return "return";
            else return "";
            case "card":       return "";
        }
        return "";
    }

}
