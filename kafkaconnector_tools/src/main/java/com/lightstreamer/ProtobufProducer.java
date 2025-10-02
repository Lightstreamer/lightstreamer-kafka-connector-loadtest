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

import java.util.Random;

import org.apache.kafka.common.serialization.Serializer;

import com.lightstreamer.proto.PriceInfo;

public class ProtobufProducer extends RateLimitedKafkaProducer<PriceInfo> {

    public ProtobufProducer(String kafka_bootstrap_string, String pid,
            String topicName) {
        super(kafka_bootstrap_string, pid, topicName, ProtoTestObjSerializer.class);

    }

    @Override
    PriceInfo makePayload(Random rnd, String key) {
        return com.lightstreamer.proto.PriceInfo.newBuilder()
                .setSymbol(key)
                .setLS(0.1f)
                .setLSSize(10)
                .setBid(0.1f)
                .setBidSize(10)
                .setAsk(0.1f)
                .setAskSize(10)
                .setCurrTime("1")
                .setHigh(0.1f)
                .setLow(0.1f)
                .setVol(10)
                .build();
    }

    public static class ProtoTestObjSerializer implements Serializer<com.lightstreamer.proto.PriceInfo> {

        @Override
        public byte[] serialize(String topic, com.lightstreamer.proto.PriceInfo data) {
            if (data == null) {
                return null;
            }
            return data.toByteArray();
        }
    }
}
