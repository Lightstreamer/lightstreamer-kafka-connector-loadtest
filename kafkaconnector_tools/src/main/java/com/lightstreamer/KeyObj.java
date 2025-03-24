package com.lightstreamer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.SecureRandom;
import java.util.Random;

public class KeyObj {

    @JsonProperty
    public String timestamp;

    @JsonProperty
    public String nameId;

    @JsonProperty
    public String sndValue;

    @JsonProperty
    public int intNum;

    @JsonProperty
    private String[] names;

    public KeyObj() {
    }

    public KeyObj(String timestamp, String name, String sndValue, int intNum, String[] stringids) {
        this.timestamp = timestamp;
        this.nameId = name;
        this.sndValue = sndValue;
        this.intNum = intNum;
        initNames(stringids);
    }

    private void initNames(String[] stringids) {
        names = new String[1024];
        Random rnd = new SecureRandom();
        for (int i = 0; i < names.length; i++) {
            names[i] = stringids[rnd.nextInt(stringids.length)];
        }
    }

}

