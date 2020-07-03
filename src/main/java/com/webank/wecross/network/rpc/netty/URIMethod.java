package com.webank.wecross.network.rpc.netty;

import java.util.Objects;
import org.apache.logging.log4j.util.Strings;

public class URIMethod {
    private String uri;
    private String method;

    public URIMethod(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isResourceURI() {
        if (Strings.isEmpty(uri)) {
            return false;
        }

        // /network/stub/resource/method
        // /network/stub/method
        int count = uri.substring(1).split("/").length;
        return uri.startsWith("/") && (count == 4 || count == 3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URIMethod path = (URIMethod) o;
        return Objects.equals(method, path.method) && Objects.equals(uri, path.uri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, uri);
    }

    @Override
    public String toString() {
        return "URIMethod{" + "method='" + method + '\'' + ", uri='" + uri + '\'' + '}';
    }
}
