package com.webank.wecross.network.rpc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.network.client.BaseClientEngine;
import com.webank.wecross.network.client.ClientMessageEngine;
import com.webank.wecross.stub.ObjectMapperFactory;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class RemoteAuthFilter implements AuthFilter {
    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private ClientMessageEngine remoteEngine;
    private Collection<String> uriNeedAuth = new HashSet<>();

    @Override
    public void doAuth(ChannelHandlerContext ctx, HttpRequest httpRequest, Handler handler)
            throws Exception {
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
                            FullHttpResponse response =
                                    new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.valueOf(statusCode),
                                            Unpooled.wrappedBuffer(body.getBytes()));

                            for (Map.Entry<String, String> header : headers) {
                                response.headers().add(header.getKey(), header.getValue());
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
        String uri = httpRequest.uri();
        return uriNeedAuth.contains(uri);
    }

    public void setRemoteEngine(ClientMessageEngine remoteEngine) {
        this.remoteEngine = remoteEngine;
    }
}
