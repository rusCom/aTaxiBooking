package org.toptaxi.ataxibooking.data;

import org.json.JSONObject;

public class Wish {
    private String Type;
    private Boolean IsChecked;
    private Integer Cost;

    public Wish() {
    }

    public void setFromJSON(JSONObject data){

    }

    public void setType(String type) {
        Type = type;
    }

    public String getType() {
        return Type;
    }


}
