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

import com.exorath.communication.impl.ICommunicationAPI;
import redis.clients.jedis.JedisPool;
import rx.Observable;

/**
 * Created by toonsev on 8/29/2016.
 */
public interface CommunicationAPI {
    /**
     * Makes the communicationAPI subscribe to a list of channels. Any messages published to these chans will be forwarded to this API's listeners.
     * <p>
     * The operation is done asynchronously.
     *
     * @param channels the channels to subscribe to
     */
    void subscribe(String... channels);

    /**
     * Closes all open connections created. You should call this when the application terminates.
     */
    void destroy();

    void publish(String channel, Message message);
    /**
     * Returns an observable that will emit a message whenever a message is send to one of the registered channels in this api.
     * <p>
     * This observable will never complete (Atm it's actually a {@link rx.subjects.PublishSubject} in the backend).
     *
     * @return an observable that will emit a message whenever a message is send to one of the registered channels in this api
     */
    Observable<Message> getMessageObservable();

    static CommunicationAPI create(JedisPool jedisPool) {
        return new ICommunicationAPI(jedisPool);
    }
}
