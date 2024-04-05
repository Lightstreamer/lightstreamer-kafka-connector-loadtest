package com.lightstreamer;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestComplexObj {

    @JsonProperty
    public String id;

    @JsonProperty
    public String firstText;

    @JsonProperty
    public String secondText;

    @JsonProperty
    public String thirdText;

    @JsonProperty
    public String fourthText;

    @JsonProperty
    private int firstnumber;

    @JsonProperty
    private int secondNumber;

    @JsonProperty
    private int thirdNumber;

    @JsonProperty
    private int fourthNumber;

    @JsonProperty
    private List<String> hobbies;

    @JsonProperty
    public String timestamp;

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setFirstText(String firstText) {
        this.firstText = firstText;
    }

    public void setSecondText(String secondText) {
        this.secondText = secondText;
    }

    public void setThirdText(String thirdText) {
        this.thirdText = thirdText;
    }

    public void setFourthText(String fourthText) {
        this.fourthText = fourthText;
    }

    public void setFirstnumber(int firstnumber) {
        this.firstnumber = firstnumber;
    }

    public void setSecondNumber(int secondNumber) {
        this.secondNumber = secondNumber;
    }

    public void setThirdNumber(int thirdNumber) {
        this.thirdNumber = thirdNumber;
    }

    public void setFourthNumber(int fourthNumber) {
        this.fourthNumber = fourthNumber;
    }

    public TestComplexObj() {
    }

    public TestComplexObj(String id, String firstText, String secondText, String thirdText, String fourthText,
            int firstnumber, int secondNumber, int thirdNumber, int fourthNumber, List<String> hobbies,
            String timestamp) {
        this.id = id;
        this.firstText = firstText;
        this.secondText = secondText;
        this.thirdText = thirdText;
        this.fourthText = fourthText;
        this.firstnumber = firstnumber;
        this.secondNumber = secondNumber;
        this.thirdNumber = thirdNumber;
        this.fourthNumber = fourthNumber;
        this.hobbies = hobbies;
        this.timestamp = timestamp;
    }
}