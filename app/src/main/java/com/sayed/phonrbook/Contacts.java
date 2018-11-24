package com.sayed.phonrbook;

import java.util.ArrayList;

public class Contacts {

    String name;
    ArrayList<String> phoneNumber;

    public Contacts(String name, ArrayList<String> phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return "Contacts{" +
                "name='" + name + '\'' +
                ", phoneNumber=" + phoneNumber +
                '}';
    }
}
