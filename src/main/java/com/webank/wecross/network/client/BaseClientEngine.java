package com.webank.wecross.network.client;

import java.util.Map;

public interface BaseClientEngine {
    interface Handler {
        void onResponse(
                int statusCode,
                String statusText,
                Iterable<Map.Entry<String, String>> headers,
                String body);
    }

    void asyncSendInternal(
            String method,
            String uri,
            Iterable<Map.Entry<String, String>> headers,
            String body,
            Handler handler);
}
