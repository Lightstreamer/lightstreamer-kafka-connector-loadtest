/*
 * Copyright (C) 2024 Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.lightstreamer;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TestVeryComplexObj {

    @JsonProperty
    public String id;

    @JsonProperty
    public String changes;

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

    public void setChanges(String changes) {
        this.changes = changes;
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

    public TestVeryComplexObj() {
    }

    public TestVeryComplexObj(String id, String changes, String firstText, String secondText, String thirdText,
            String fourthText, int firstnumber, int secondNumber, int thirdNumber, int fourthNumber,
            List<String> hobbies, String timestamp) {
        this.id = id;
        this.changes = changes;
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
