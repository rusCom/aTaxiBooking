package org.toptaxi.ataxibooking.data;


public class Constants {
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    public static final int MY_PERMISSIONS_RECEIVE_SMS      = 101;
    public static final int MY_PERMISSIONS_CALL_PHONE       = 102;

    public static final int ACTIVITY_ADDRESS      = 200;
    public static final int ACTIVITY_CHOOSE_HOUSE = 201;
    public static final int ACTIVITY_NEW_ORDER    = 202;
    public static final int ACTIVITY_LOGIN        = 203;
    public static final int ACTIVITY_PLACE_PICKER = 204;
    public static final int ACTIVITY_PAY_TYPE     = 205;
    public static final int ACTIVITY_WISH         = 206;

    public static final int ROUTE_POINT_TYPE_UNKNOWN        = 0;
    public static final int ROUTE_POINT_TYPE_STREET         = 1;
    public static final int ROUTE_POINT_TYPE_HOUSE          = 2;
    public static final int ROUTE_POINT_TYPE_LOCALITY       = 3;
    public static final int ROUTE_POINT_TYPE_POINT          = 4;
    public static final int ROUTE_POINT_TYPE_AIRPORT        = 100;
    public static final int ROUTE_POINT_TYPE_STATION        = 101;

    public static final int ORDER_STATE_NEW_ORDER           = 0;
    public static final int ORDER_STATE_CALC_ORDER          = 1;
    public static final int ORDER_STATE_SEARCH_DRIVER       = 2;
    public static final int ORDER_STATE_DRIVE_TO_CLIENT     = 3;
    public static final int ORDER_STATE_DRIVER_AT_CLIENT    = 4;
    public static final int ORDER_STATE_CLIENT_IN_CAR       = 5;

    public static final int ORDER_SERVICE_TYPE_ECONOMY      = 0;
    public static final int ORDER_PAYMENT_TYPE_CASH         = 0;

    public static final int FAST_ROUTE_POINT_HISTORY       = 1;
    public static final int FAST_ROUTE_POINT_AIRPORT       = 2;
    public static final int FAST_ROUTE_POINT_STATION       = 3;

    public static final int MENU_BALANCE        = 1;
    public static final int MENU_HISTORY        = 2;
    public static final int MENU_MAP_TYPE       = 3;

    public static final int ROUTE_POINT_ADAPTER_VIEW_ORDER          = 1;
    public static final int ROUTE_POINT_ADAPTER_FAST_ROUTE_POINT    = 2;
    public static final int ROUTE_POINT_ADAPTER_HOUSES              = 3;


    public static final int DOT_HTTP_ERROR      = 101;
    public static final int DOT_REST_ERROR      = 102;
    public static final int DOT_REST_OK         = 103;
    public static final int DOT_PHONE_WRONG     = 104;
    public static final int DOT_PASSWORD_WRONG  = 105;
    public static final int DOT_IDENTIFICATION  = 106;
    public static final int DOT_DRIVER_WRONG    = 107;
    public static final int DOT_NEW_VERSION     = 108;
    public static final int DOT_BLOCKED         = 109;

    public static final int DATABASE_VERSION    = 2;

}
