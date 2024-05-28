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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lightstreamer.client.ClientListener;

public class MyClientListener implements ClientListener {

    private static final Logger logger = LogManager.getLogger(MyClientListener.class);

    @Override
    public void onListenEnd() {
        logger.info("Stops listening to client events");
    }

    @Override
    public void onListenStart() {
        logger.info("Start listening to client events");
    }

    @Override
    public void onPropertyChange(String property) {
        logger.info("Client property changed: " + property);
    }

    @Override
    public void onServerError(int code, String message) {
        logger.info("Server error: " + code + ": " + message);
    }

    @Override
    public void onStatusChange(String newStatus) {
        logger.info("Connection status changed to " + newStatus);
    }

}
