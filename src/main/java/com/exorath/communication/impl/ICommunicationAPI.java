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

import com.exorath.communication.api.CommunicationAPI;
import com.exorath.communication.api.Message;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by toonsev on 8/29/2016.
 */
public class ICommunicationAPI implements CommunicationAPI {
    private JedisPool jedisPool;
    private JedisPubSub pubSub;
    private PublishSubject<Message> messageSubject = PublishSubject.create();

    public ICommunicationAPI(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        setupPubSub();
    }

    private void setupPubSub() {
        pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                emitMessageStringToObservable(message);
            }
        };
    }

    protected void emitMessageStringToObservable(String messageString) {
        Message message = Message.fromJson(new JsonParser().parse(messageString).getAsJsonObject());
        if (message.getType() != null)
            emitMessageToObservable(message);
        else
            System.out.println("[ExoCommunication] [Error] Received a json message that did not contain a type field. ExoCommunication did not emit this.");
    }
    protected void emitMessageToObservable(Message message) {
            messageSubject.onNext(message);
    }

    @Override
    public void publish(String channel, Message message) {
        if (channel == null)
            throw new IllegalArgumentException("Channel cannot be null");
        if (message == null)
            throw new IllegalArgumentException("Message cannot be null");

        JsonObject json = Message.toJson(message);
        if (json.get(Message.TYPE_KEY) != null)
            getResource().observeOn(Schedulers.io()).subscribe(jedis -> jedis.publish(channel, json.toString()));
        else
            System.out.println("[ExoCommunication] [Error] Failed to parse a Message to json, message was not published.");
    }

    @Override
    public void subscribe(String... channels) {
        if (channels == null)
            throw new IllegalArgumentException("Channels cannot be null");
        getResource().observeOn(Schedulers.newThread()).subscribe(jedis -> jedis.subscribe(pubSub, channels));
    }

    @Override
    public Observable<Message> getMessageObservable() {
        return messageSubject;
    }

    protected Observable<Jedis> getResource() {
        return Observable.create(subscriber -> {
            subscriber.onNext(jedisPool.getResource());
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).cast(Jedis.class);
    }

    @Override
    public void destroy() {
        if (jedisPool != null)
            jedisPool.destroy();
    }
}
