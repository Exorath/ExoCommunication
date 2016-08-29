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
import com.exorath.communication.impl.ICommunicationAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.embedded.RedisServer;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by toonsev on 8/29/2016.
 */
public class ICommunicationAPITest {
    private String type;
    private JsonObject data;
    private Message mockedMessage;

    private JedisPool jedisPool;
    private RedisServer server;

    private ICommunicationAPI communicationAPI;

    @Before
    public void setup() throws IOException {
        server = new RedisServer(6379);
        server.start();
        jedisPool = new JedisPool("localhost");
        communicationAPI = new ICommunicationAPI(jedisPool);

        type = "sampletype";
        data = new JsonObject();
        data.addProperty("samplekey", "samepleval");

        mockedMessage = mock(Message.class);
        when(mockedMessage.getType()).thenReturn(type);
        when(mockedMessage.getData()).thenReturn(data);
    }


    @Test(timeout = 3000)
    public void getResourceObservableNotNullTest() {
        assertNotNull(communicationAPI.getResource());
    }


    @Test(timeout = 3000)
    public void getResourceCompletesTest() {
        TestSubscriber<Jedis> testSubscriber = new TestSubscriber<>();
        communicationAPI.getResource().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
    }

    @Test(timeout = 3000)
    public void getResourceEmitsOneJedis() {
        TestSubscriber<Jedis> testSubscriber = new TestSubscriber<>();
        communicationAPI.getResource().subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValueCount(1);
    }

    @Test(timeout = 3000)
    public void getResourceSetTest() {
        communicationAPI.getResource().toBlocking().first().set("testkey", "testvalue");
        assertEquals("testvalue", new Jedis("localhost").get("testkey"));
    }


    @Test(timeout = 5000)
    public void messageObservableDoesNotTerminateWhenUnsubscribedTest() throws InterruptedException {
        TestSubscriber subscriber = new TestSubscriber();
        communicationAPI.getMessageObservable().subscribe(subscriber);
        subscriber.unsubscribe();
        subscriber.assertNoTerminalEvent();
    }

    @Test(timeout = 5000)
    public void messageObservableDoesNotTerminateAfterTwoSecondsWhenSubscribedTest() throws InterruptedException {
        TestSubscriber subscriber = new TestSubscriber();
        communicationAPI.getMessageObservable().subscribe(subscriber);
        Thread.sleep(2000);
        subscriber.assertNoTerminalEvent();
    }

    @Test(timeout = 5000)
    public void emitStringMessageToObservableEmitsMessageToSubscribersOfMessageObservable() throws InterruptedException {
        TestSubscriber<Message> subscriber = new TestSubscriber();
        communicationAPI.getMessageObservable().subscribe(subscriber);

        JsonObject obj = new JsonObject();
        obj.addProperty(Message.TYPE_KEY, type);
        obj.add(Message.DATA_KEY, data);
        communicationAPI.emitMessageStringToObservable(obj.toString());

        subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS);

        Message received = subscriber.getOnNextEvents().get(0);
        assertTrue(received.getType().equals(type) && received.getData().equals(data));
    }

    @Test(timeout = 5000)
    public void emitMessageToObservableEmitsMessageToSubscribersOfMessageObservable() throws InterruptedException {
        TestSubscriber<Message> subscriber = new TestSubscriber();
        communicationAPI.getMessageObservable().subscribe(subscriber);

        communicationAPI.emitMessageToObservable(mockedMessage);

        subscriber.awaitValueCount(1, 5, TimeUnit.SECONDS);

        Message received = subscriber.getOnNextEvents().get(0);
        assertTrue(received.getType().equals(type) && received.getData().equals(data));
    }

    @Test(timeout = 5000)
    public void connectionPoolNotClosedWhenNotDestroyed() {
        assertFalse(jedisPool.isClosed());
    }

    @Test(timeout = 5000)
    public void destroyClosesConnectionTest() {
        communicationAPI.destroy();
        assertTrue(jedisPool.isClosed());
    }

    @Test(timeout = 5000)
    public void publishPublishesOnChannelTest() throws InterruptedException {
        AtomicBoolean rightChannel = new AtomicBoolean(false);

        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equals("channel"))
                    rightChannel.set(true);
            }
        };
        new Thread(() -> jedisPool.getResource().subscribe(pubSub, "channel")).start();
        Thread.sleep(200);
        communicationAPI.publish("channel", mockedMessage);
        Thread.sleep(200);
        pubSub.unsubscribe();
        assertTrue(rightChannel.get());
    }

    @Test(timeout = 5000)
    public void publishPublishesMessageTest() throws InterruptedException {
        AtomicBoolean rightMessage = new AtomicBoolean(false);

        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                JsonObject json = new JsonParser().parse(message).getAsJsonObject();
                if (json.get(Message.TYPE_KEY).getAsString().equals(type) && json.get(Message.DATA_KEY).getAsJsonObject().equals(data))
                    rightMessage.set(true);
            }
        };
        new Thread(() -> jedisPool.getResource().subscribe(pubSub, "channel")).start();
        Thread.sleep(200);
        communicationAPI.publish("channel", mockedMessage);
        Thread.sleep(200);
        pubSub.unsubscribe();
        assertTrue(rightMessage.get());
    }

    @After
    public void cleanup() {
        if (server != null)
            server.stop();
    }
}
