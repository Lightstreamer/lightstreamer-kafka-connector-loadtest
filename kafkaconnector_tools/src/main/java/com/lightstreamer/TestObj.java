package com.lightstreamer;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestObj {

    @JsonProperty
    public String timestamp;

    @JsonProperty
    public String fstValue;

    @JsonProperty
    public String sndValue;

    @JsonProperty
    public int intNum;

    public TestObj() {
    }

    public TestObj(String timestamp, String fstValue, String sndValue, int intNum) {
        this.timestamp = timestamp;
        this.fstValue = fstValue;
        this.sndValue = sndValue;
        this.intNum = intNum;
    }

}
