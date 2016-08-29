/*
 * Copyright 2016 Exorath
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.exorath.communication.api;

import com.exorath.communication.impl.IMessage;
import com.google.gson.JsonObject;

/**
 * Created by toonsev on 8/29/2016.
 */
public interface Message {
    String TYPE_KEY = "type";
    String DATA_KEY = "data";

    /**
     * Gets the type identifier of this message.
     *
     * @return the type identifier of this message
     */
    String getType();

    /**
     * Gets the data of this message.
     *
     * @return the data of this message
     */
    JsonObject getData();

    static Message create(String type, JsonObject data) {
        return new IMessage(type, data);
    }

    static Message fromJson(JsonObject object) {
        String type = object.has(TYPE_KEY) ? object.get(TYPE_KEY).getAsString() : null;
        JsonObject data = object.has(DATA_KEY) && object.get(DATA_KEY).isJsonObject() ? object.get(DATA_KEY).getAsJsonObject() : null;
        return new IMessage(type, data);
    }

    static JsonObject toJson(Message message) {
        JsonObject json = new JsonObject();
        if (message.getType() != null)
            json.addProperty(TYPE_KEY, message.getType());
        if(message.getData() != null)
        json.add(DATA_KEY, message.getData());
        return json;
    }
}
