package ru.cityvoicer.golosun.api;

public class NetProfile {
    public Integer id;
    public String username_mobile;
    public Integer password_clear;
    public String balance_phiz;
    public Integer age;
    public String sex;
    public String push_token;
    public String ap_mac_address;

    public static final String EXPAND_ALL_PARAMS = "password_clear,balance_phiz,age,sex,push_token,ap_mac_address";
}
