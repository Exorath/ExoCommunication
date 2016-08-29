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

package com.exorath.communication.impl;

import com.exorath.communication.api.Message;
import com.google.gson.JsonObject;

/**
 * Created by toonsev on 8/29/2016.
 */
public class IMessage implements Message{
    private String type;
    private JsonObject data;

    public IMessage(String type, JsonObject data){
        this.type = type;
        this.data = data;
    }
    @Override
    public String getType() {
        return type;
    }

    @Override
    public JsonObject getData() {
        return data;
    }
}
