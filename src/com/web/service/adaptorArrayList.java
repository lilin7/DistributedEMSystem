package com.web.service;

import java.util.ArrayList;

public class adaptorArrayList {
    public ArrayList<String> unmarshal (String[] list){
        ArrayList<String> returnMessage = new ArrayList<String>();
        for(int i = 0; i<list.length;i++){
            returnMessage.add(list[i]);
        }
        return returnMessage;
    }

    public String[] marshal (ArrayList<String> returnMessage){
        String[] list = new String[returnMessage.size()];
        for(int j = 0;j<returnMessage.size();j++){
            list[j] = returnMessage.get(j);
        }
        return list;
    }
}
