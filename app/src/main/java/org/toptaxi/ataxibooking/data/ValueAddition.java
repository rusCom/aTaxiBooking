package org.toptaxi.ataxibooking.data;


import org.toptaxi.ataxibooking.MainApplication;

public class ValueAddition {
    private Integer Value;

    public ValueAddition(Integer value) {
        Value = value;
    }

    @Override
    public String toString() {
        if (Value == 0)return "Нет";
        else return String.valueOf(Value) + " " + MainApplication.getRubSymbol();
    }
}
