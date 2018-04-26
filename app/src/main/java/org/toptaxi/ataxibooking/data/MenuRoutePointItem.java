package org.toptaxi.ataxibooking.data;


public class MenuRoutePointItem {
    int Action;
    String ActionName;

    public MenuRoutePointItem(int action, String actionName) {
        Action = action;
        ActionName = actionName;
    }

    public String getActionName() {
        return ActionName;
    }

    public int getAction() {
        return Action;
    }
}
