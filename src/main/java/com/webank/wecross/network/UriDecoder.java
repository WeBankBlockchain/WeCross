package com.webank.wecross.network;

import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.List;
import java.util.Map;

public class UriDecoder {
    private String uri;

    public UriDecoder(String uri) {
        this.uri = uri;
    }

    public String getQueryBykey(String key) throws Exception {
        QueryStringDecoder decoderQuery = new QueryStringDecoder(uri);
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        if (!uriAttributes.containsKey(key)) {
            throw new Exception(key + " not found in uri");
        }
        return uriAttributes.get(key).get(0);
    }

    public String getMethod() {
        int end = uri.contains("?") ? uri.indexOf("?") : uri.length();
        String[] splits = uri.substring(1, end).split("/");
        return splits[splits.length - 1];
    }

    public String getURIWithoutQueryString() {
        int end = uri.contains("?") ? uri.indexOf("?") : uri.length();
        return uri.substring(0, end);
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
