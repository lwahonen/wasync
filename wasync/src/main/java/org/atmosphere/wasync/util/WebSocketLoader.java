/*
 * Copyright 2012 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.wasync.util;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that can be used to load a WebSocket enabled Server
 *
 * @author jeanfrancois Arcand
 */
public class WebSocketLoader {

    private final static Logger logger = LoggerFactory.getLogger(WebSocketLoader.class);

    public static void main(String[] s) throws InterruptedException, IOException {

        if (s.length == 0) {
            s = new String[]{"5", "5000", "1", "http://127.0.0.1:8080/"};
        }

        int run = Integer.valueOf(s[0]);
        final int clientNum = Integer.valueOf(s[1]);
        final int messageNum = Integer.valueOf(s[2]);
        String url = "http://" + s[3];

        System.out.println("Stressing: " + url);
        System.out.println("Number of Client: " + clientNum);
        System.out.println("Number of Message: " + messageNum);
        System.out.println("Number of run: " + run);
        long count = 0;

        for (int r = 0; r < run; r++) {

            DefaultAsyncHttpClientConfig.Builder b = new DefaultAsyncHttpClientConfig.Builder();
            b.setFollowRedirect(true)
                    .setConnectTimeout(-1)
                    .setReadTimeout(-1)
                    .setUserAgent("loader/1.1")
                    .setTcpNoDelay(true)
                    .setKeepAlive(true)
                    .setUseProxySelector(true);

            final AsyncHttpClient c = new DefaultAsyncHttpClient(b.build());
            Client client = ClientFactory.getDefault().newClient();
            RequestBuilder request = client.newRequestBuilder();
            request.method(Request.METHOD.GET).uri(url);
            request.transport(Request.TRANSPORT.WEBSOCKET);
            request.header("Sec-WebSocket-Protocol", "rpm-protocol");

            final CountDownLatch l = new CountDownLatch(clientNum);
            final CountDownLatch messages = new CountDownLatch(messageNum * clientNum);
            long clientCount = l.getCount();
            final AtomicLong total = new AtomicLong(0);

            Socket[] sockets = new Socket[clientNum];
            for (int i = 0; i < clientCount; i++) {
                final AtomicLong start = new AtomicLong(0);
                sockets[i] = client.create(client.newOptionsBuilder().runtime(c).reconnect(false).build())
                        .on(new Function<Integer>() {
                            @Override
                            public void on(Integer statusCode) {
                                start.set(System.currentTimeMillis());
                                l.countDown();
                            }
                        }).on(new Function<String>() {

                            int mCount = 0;

                            @Override
                            public void on(String s) {
                                if (s.startsWith("message")) {
                                    String[] m = s.split("\n\r");
                                    mCount += m.length;
                                    messages.countDown();
//                                    System.out.println("Message left receive " + messages.getCount() + " message " + s);
                                    if (mCount == messageNum) {
//                                        System.out.println("All messages received " + mCount);
                                        total.addAndGet(System.currentTimeMillis() - start.get());
                                    }
                                }
                            }
                        }).on(new Function<Throwable>() {
                            @Override
                            public void on(Throwable t) {
                                t.printStackTrace();
                            }
                        });

            }

            for (int i = 0; i < clientCount; i++) {
                sockets[i].open(request.build());
            }

            l.await(30, TimeUnit.SECONDS);

            System.out.println("OK, all Connected: " + clientNum);

            for (int i = 0; i < clientCount; i++) {
                for (int j = 0; j < messageNum; j++) {
                    sockets[i].fire("message" + i);
                }
            }
            messages.await(1, TimeUnit.HOURS);
            for (int i = 0; i < clientCount; i++) {
                sockets[i].close();
            }
            count += (total.get() / clientCount);
            System.out.println("Run " + r + " => Total run : " + (total.get() / clientCount));
            c.close();
            System.gc();
        }
        System.out.println("=== Means " + (count / run) + "=====");
    }

}