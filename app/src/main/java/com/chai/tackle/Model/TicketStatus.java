package com.chai.tackle.Model;

public class TicketStatus {

   final static String statusList[][] = {
            {"N/A", "N/A"},
            {"0", "Open"},
            {"P", "Pending"},
            {"S", "Solved"},
            {"C", "Closed"},
    };

    public static String toShortText(int statusCode) {
        return statusList[statusCode][0];
    }

    public static String toLongText(int statusCode) {
        return statusList[statusCode][1];
    }

}
