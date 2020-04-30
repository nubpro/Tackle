package com.chai.tackle.Model;

public class Role {

    final static String roleList[][] = {
            {"N/A"},
            {"Normal Member"},
            {"Support Agent"},
            {"Manager"},
    };

    public static String toText(int roleCode) {
        return roleList[roleCode][0];
    }
}
