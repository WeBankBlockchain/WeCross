package com.webank.wecross.network.client;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.asynchttpclient.Dsl.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.rpc.authentication.RemoteAuthFilter;
import com.webank.wecross.stub.ObjectMapperFactory;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class NettyAsyncHttpClientEngine implements ClientMessageEngine {
    private Logger logger = LoggerFactory.getLogger(NettyAsyncHttpClientEngine.class);

    private String server;
    private AsyncHttpClient httpClient;
    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
    private ClientConnection clientConnection;

    private static int httpClientTimeOut = 100000; // ms

    @Override
    public void init() throws WeCrossException {
        logger.info(clientConnection.toString());
        server = clientConnection.getServer();
        httpClient = getHttpAsyncClient(clientConnection);
    }

    private void checkRequest(Request<?> request) throws WeCrossException {
        if (request.getVersion().isEmpty()) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.QUERY_PARAMS_ERROR, "Request version is empty");
        }
    }

    @Override
    public <T extends Response> T send(Request request, TypeReference typeReference)
            throws WeCrossException {

        checkRequest(request);
        CompletableFuture<T> responseFuture = new CompletableFuture<>();
        CompletableFuture<WeCrossException> exceptionFuture = new CompletableFuture<>();

        asyncSend(
                request,
                typeReference,
                new Callback<T>() {
                    @Override
                    public void onSuccess(T response) {
                        responseFuture.complete(response);
                        exceptionFuture.complete(null);
                    }

                    @Override
                    public void onFailed(WeCrossException e) {
                        logger.warn("send onFailed: " + e.getMessage());
                        responseFuture.complete(null);
                        exceptionFuture.complete(e);
                    }
                });

        try {
            T response = responseFuture.get(20, TimeUnit.SECONDS);
            WeCrossException exception = exceptionFuture.get(20, TimeUnit.SECONDS);

            if (logger.isDebugEnabled()) {
                logger.debug("response: {}", response);
            }

            if (exception != null) {
                throw exception;
            }

            return response;
        } catch (TimeoutException e) {
            logger.warn("http request timeout");
            throw new WeCrossException(
                    WeCrossException.ErrorCode.QUERY_TIMEOUT, "http request timeout");
        } catch (Exception e) {
            logger.warn("send exception", e);
            throw new WeCrossException(
                    WeCrossException.ErrorCode.QUERY_CLIENT_ERROR, "http request failed");
        }
    }

    @Override
    public <T extends Response> void asyncSend(
            Request<?> request, TypeReference typeReference, Callback<T> callback) {
        try {
            String url = "https://" + clientConnection.getServer() + request.getMethod();
            if (logger.isDebugEnabled()) {
                logger.debug("request: {}; url: {}", request.toString(), url);
            }

            checkRequest(request);
            httpClient
                    .preparePost(url)
                    .setHeader(HttpHeaders.Names.ACCEPT, HttpHeaders.Values.APPLICATION_JSON)
                    .setHeader(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.APPLICATION_JSON)
                    .setHeader(HttpHeaders.Names.AUTHORIZATION, request.getAuth())
                    .setBody(objectMapper.writeValueAsString(request))
                    .execute(
                            new AsyncCompletionHandler<Object>() {
                                @Override
                                public Object onCompleted(org.asynchttpclient.Response httpResponse)
                                        throws Exception {
                                    try {
                                        if (httpResponse.getStatusCode() != 200) {
                                            callback.callOnFailed(
                                                    new WeCrossException(
                                                            WeCrossException.ErrorCode
                                                                    .QUERY_SERVER_ERROR,
                                                            "AsyncSend "
                                                                    + url
                                                                    + " status: "
                                                                    + httpResponse.getStatusCode()
                                                                    + " message: "
                                                                    + httpResponse
                                                                            .getStatusText()));
                                            return null;
                                        } else {
                                            String content = httpResponse.getResponseBody();
                                            T response =
                                                    (T)
                                                            objectMapper.readValue(
                                                                    content, typeReference);
                                            callback.callOnSuccess(response);
                                            return response;
                                        }
                                    } catch (Exception e) {
                                        callback.callOnFailed(
                                                new WeCrossException(
                                                        WeCrossException.ErrorCode
                                                                .QUERY_CLIENT_ERROR,
                                                        "handle response failed: " + e.toString()));
                                        return null;
                                    }
                                }

                                @Override
                                public void onThrowable(Throwable t) {
                                    callback.callOnFailed(
                                            new WeCrossException(
                                                    WeCrossException.ErrorCode.QUERY_CLIENT_ERROR,
                                                    "AsyncSend exception: "
                                                            + t.getCause().toString()));
                                }
                            });

        } catch (Exception e) {
            logger.error("Encode json error when async sending: {}", e);
            callback.callOnFailed(
                    new WeCrossException(
                            WeCrossException.ErrorCode.QUERY_CLIENT_ERROR,
                            "Encode json error when async sending: " + e));
        }
    }

    @Override
    public void asyncSendInternal(
            String method,
            String uri,
            Iterable<Map.Entry<String, String>> headers,
            String body,
            Handler handler) {
        String url = "https://" + clientConnection.getServer() + uri;
        BoundRequestBuilder requestBuilder = httpClient.prepare(method, url).setBody(body);

        for (Map.Entry<String, String> header : headers) {
            requestBuilder.setHeader(header.getKey(), header.getValue());
        }

        requestBuilder.execute(
                new AsyncCompletionHandler<Object>() {
                    @Override
                    public Object onCompleted(org.asynchttpclient.Response response)
                            throws Exception {
                        int statusCode = response.getStatusCode();
                        String statusText = response.getStatusText();
                        Iterable<Map.Entry<String, String>> responseHeaders = response.getHeaders();
                        String responseBody = response.getResponseBody(StandardCharsets.UTF_8);

                        handler.onResponse(statusCode, statusText, responseHeaders, responseBody);
                        return response;
                    }

                    @Override
                    public void onThrowable(Throwable t) {
                        logger.error("asyncSendInternal onThrowable: ", t.getCause());

                        RemoteAuthFilter.RemoteResponse.Data data =
                                new RemoteAuthFilter.RemoteResponse.Data();
                        data.setErrorCode(RemoteAuthFilter.RemoteResponse.Data.ERROR);
                        data.setMessage(t.getCause().getMessage());

                        RemoteAuthFilter.RemoteResponse remoteResponse =
                                new RemoteAuthFilter.RemoteResponse();
                        remoteResponse.setData(data);

                        try {
                            handler.onResponse(
                                    200, "", null, objectMapper.writeValueAsString(remoteResponse));
                        } catch (JsonProcessingException e) {
                            logger.error("e: ", e);
                        }
                    }
                });
    }

    private SslContext getSslContext(ClientConnection clientConnection) throws IOException {

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
                new PathMatchingResourcePatternResolver();
        Resource sslKey =
                pathMatchingResourcePatternResolver.getResource(clientConnection.getSSLKey());
        Resource sslCert =
                pathMatchingResourcePatternResolver.getResource(clientConnection.getSSLCert());
        Resource caCert =
                pathMatchingResourcePatternResolver.getResource(clientConnection.getCaCert());

        return SslContextBuilder.forClient()
                .trustManager(caCert.getInputStream())
                .keyManager(sslCert.getInputStream(), sslKey.getInputStream())
                .sslProvider(SslProvider.JDK)
                .clientAuth(ClientAuth.REQUIRE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    private AsyncHttpClient getHttpAsyncClient(ClientConnection clientConnection)
            throws WeCrossException {
        try {
            return asyncHttpClient(
                    config().setSslContext(getSslContext(clientConnection))
                            // config().setProxyServer(proxyServer("localhost", 8080))
                            .setConnectTimeout(httpClientTimeOut)
                            .setRequestTimeout(httpClientTimeOut)
                            .setReadTimeout(httpClientTimeOut)
                            .setHandshakeTimeout(httpClientTimeOut)
                            .setShutdownTimeout(httpClientTimeOut)
                            .setSslSessionTimeout(httpClientTimeOut)
                            .setPooledConnectionIdleTimeout(httpClientTimeOut)
                            .setAcquireFreeChannelTimeout(httpClientTimeOut)
                            .setConnectionPoolCleanerPeriod(httpClientTimeOut)
                            // .setMaxConnections(connection.getMaxTotal())
                            // .setMaxConnectionsPerHost(connection.getMaxPerRoute())
                            .setKeepAlive(true));

        } catch (Exception e) {
            logger.error("Init http client error: {}", e);
            throw new WeCrossException(
                    WeCrossException.ErrorCode.QUERY_CLIENT_INIT_ERROR,
                    "Init http client error: " + e);
        }
    }

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
}
