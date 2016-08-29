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

import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by toonsev on 8/29/2016.
 */
public class IMessageTest {
    @Test
    public void getTypeEqualsTypeTest(){
        assertEquals("type", new IMessage("type", new JsonObject()).getType());
    }
    @Test
    public void getDataEqualsDataTest(){
        JsonObject data = new JsonObject();
        data.addProperty("testkey", "testvalue");
        assertEquals(data, new IMessage("type", data).getData());
    }
}
