package com.example.smarthomegesturecontrol.permissions;

import java.util.List;

public interface OnPermission {

    void hasPermission(List<String> granted, boolean all);

    void noPermission(List<String> denied, boolean quick);
}