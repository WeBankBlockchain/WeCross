package com.webank.wecross.network.rpc.authentication;

import com.webank.wecross.account.RouterLoginAccountContext;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.network.client.BaseClientEngine;
import com.webank.wecross.network.client.ClientConnection;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.network.rpc.netty.handler.HttpServerHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteAuthFilter implements AuthFilter {

    private Logger logger = LoggerFactory.getLogger(RemoteAuthFilter.class);

    private ClientMessageEngine remoteEngine;
    private ClientConnection clientConnection;
    private Collection<String> uriNeedAuth = new HashSet<>();

    public static class RemoteResponse {
        private String version = "1.0";
        private int errorCode = 0;
        private String message = "success";
        private Data data;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            public static final int SUCCESS = 0;
            public static final int ERROR = 1;

            private int errorCode;
            private String message;

            public int getErrorCode() {
                return errorCode;
            }

            public void setErrorCode(int errorCode) {
                this.errorCode = errorCode;
            }

            public String getMessage() {
                return message;
            }

            public void setMessage(String message) {
                this.message = message;
            }
        }
    }

    private String getUserNameFromURI(String uri) {
        // &wecross-name-token
        try {
            List<NameValuePair> nameValuePairs = new URIBuilder(uri).getQueryParams();
            for (int i = 0; i < nameValuePairs.size(); i++) {
                if ("wecross-name-token".equals(nameValuePairs.get(i).getName())) {
                    return nameValuePairs.get(i).getValue();
                }
            }
        } catch (Exception e) {
            logger.warn("getUserNameFromURI: e:", e);
        }

        return null;
    }

    @Override
    public void doAuth(ChannelHandlerContext ctx, HttpRequest httpRequest, Handler handler)
            throws Exception {

        // if accountName auth allowed
        try {
            boolean allowNameToken = clientConnection.isAllowNameToken();
            String uri = httpRequest.getUri();
            String tokenStr = httpRequest.headers().get(HttpHeaders.Names.AUTHORIZATION);
            logger.info("useNameToken: {}, token: {}, uri: {}", allowNameToken, tokenStr, uri);
            if (allowNameToken && Objects.isNull(tokenStr) && HttpServerHandler.shouldLogin(uri)) {
                String userName = getUserNameFromURI(httpRequest.getUri());
                if (userName == null) {
                    throw new InvalidParameterException(
                            "not found wecross-name-token parameter, uri: " + uri);
                }
                // routerLogin for token
                RouterLoginAccountContext routerLoginAccountContext =
                        new RouterLoginAccountContext();
                routerLoginAccountContext.setUsername(userName);
                routerLoginAccountContext.setAccountManagerEngine(remoteEngine);

                routerLoginAccountContext.routerLogin();
                String token = routerLoginAccountContext.getToken();
                if (token == null) {
                    throw new RuntimeException("routerLogin token is null");
                }

                // set
                httpRequest.headers().set(HttpHeaders.Names.AUTHORIZATION, token);
            }
        } catch (Exception e) {
            logger.debug("e: ", e);
        }

        if (shouldAuth(httpRequest)) {
            String method = httpRequest.method().name();
            String uri = httpRequest.uri();
            Iterable<Map.Entry<String, String>> headers = httpRequest.headers();
            String body =
                    ((FullHttpRequest) httpRequest).content().toString(StandardCharsets.UTF_8);
            boolean keepAlive = HttpUtil.isKeepAlive(httpRequest);

            remoteEngine.asyncSendInternal(
                    method,
                    uri,
                    headers,
                    body,
                    new BaseClientEngine.Handler() {
                        @Override
                        public void onResponse(
                                int statusCode,
                                String statusText,
                                Iterable<Map.Entry<String, String>> headers,
                                String body) {

                            if (logger.isTraceEnabled()) {
                                logger.trace(
                                        "status: {}, header: {}, text: {}, body: {}",
                                        statusCode,
                                        headers,
                                        statusText,
                                        body);
                            }

                            FullHttpResponse response =
                                    new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.valueOf(statusCode),
                                            Unpooled.wrappedBuffer(body.getBytes()));

                            if (headers == null) {
                                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/json");
                                response.headers()
                                        .set(
                                                HttpHeaderNames.CONTENT_LENGTH,
                                                response.content().readableBytes());
                            } else {
                                for (Map.Entry<String, String> header : headers) {
                                    response.headers().add(header.getKey(), header.getValue());
                                }
                            }

                            if (keepAlive) {
                                response.headers()
                                        .set(
                                                HttpHeaderNames.CONNECTION,
                                                HttpHeaderValues.KEEP_ALIVE);
                                ctx.writeAndFlush(response);
                            } else {
                                ctx.writeAndFlush(response)
                                        .addListener(ChannelFutureListener.CLOSE);
                            }
                        }
                    });

        } else {
            handler.onAuth(ctx, httpRequest);
        }
    }

    public void registerAuthUri(String uri) {
        uriNeedAuth.add(uri);
    }

    private boolean shouldAuth(HttpRequest httpRequest) {
        UriDecoder uriDecoder = new UriDecoder(httpRequest.getUri());
        return uriNeedAuth.contains(uriDecoder.getURIWithoutQueryString());
    }

    public void setRemoteEngine(ClientMessageEngine remoteEngine) {
        this.remoteEngine = remoteEngine;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
}
