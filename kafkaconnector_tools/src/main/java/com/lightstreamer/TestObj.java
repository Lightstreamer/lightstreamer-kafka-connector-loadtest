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
