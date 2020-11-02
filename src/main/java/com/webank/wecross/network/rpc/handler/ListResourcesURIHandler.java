package com.webank.wecross.network.rpc.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.NetworkQueryStatus;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.restserver.RestRequest;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.fetcher.ResourceFetcher;
import com.webank.wecross.restserver.request.ResourceRequest;
import com.webank.wecross.restserver.response.ResourceResponse;
import com.webank.wecross.stub.ObjectMapperFactory;
import com.webank.wecross.stub.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET/POST /sys/listResources */
public class ListResourcesURIHandler implements URIHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListResourcesURIHandler.class);

    private ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private ResourceFetcher resourceFetcher;

    public ListResourcesURIHandler(ResourceFetcher resourceFetcher) {
        this.resourceFetcher = resourceFetcher;
    }

    public ResourceFetcher getResourceFetcher() {
        return resourceFetcher;
    }

    public void setResourceFetcher(ResourceFetcher resourceFetcher) {
        this.resourceFetcher = resourceFetcher;
    }

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        RestResponse<ResourceResponse> restResponse = new RestResponse<>();

        if (logger.isDebugEnabled()) {
            logger.debug("uri: {}, request string: {}", uri, content);
        }

        try {
            /** /sys/listResource?path=payment.bcos&offset=10&size=10 */
            if (uri.contains("?path=") && uri.contains("&offset=") && uri.contains("&size=")) {
                UriDecoder uriDecoder = new UriDecoder(uri);
                String path;
                int offset, size;
                try {
                    path = uriDecoder.getQueryBykey("path");
                    offset = Integer.parseInt(uriDecoder.getQueryBykey("offset"));
                    size = Integer.parseInt(uriDecoder.getQueryBykey("size"));
                } catch (Exception e) {
                    restResponse.setErrorCode(NetworkQueryStatus.URI_QUERY_ERROR);
                    restResponse.setMessage(e.getMessage());
                    callback.onResponse(restResponse);
                    return;
                }

                if (offset < 0 || size <= 0 || size > WeCrossDefault.MAX_SIZE_FOR_LIST) {
                    restResponse.setErrorCode(NetworkQueryStatus.URI_QUERY_ERROR);
                    restResponse.setMessage(
                            "Wrong offset or size, offset >= 0, 1 <= size <= "
                                    + WeCrossDefault.MAX_SIZE_FOR_LIST);
                    callback.onResponse(restResponse);
                    return;
                }

                Path chain;
                try {
                    chain = Path.decode(path);
                } catch (Exception e) {
                    logger.warn("Decode chain path error: {}", path);
                    restResponse.setErrorCode(NetworkQueryStatus.URI_QUERY_ERROR);
                    restResponse.setMessage("Decode chain path error");
                    callback.onResponse(restResponse);
                    return;
                }

                restResponse.setData(resourceFetcher.fetchResources(chain, offset, size));
            } else {
                RestRequest<ResourceRequest> restRequest =
                        objectMapper.readValue(
                                content, new TypeReference<RestRequest<ResourceRequest>>() {});
                restRequest.checkRestRequest();
                ResourceRequest resourceRequest = restRequest.getData();
                restResponse.setData(
                        resourceFetcher.fetchResources(resourceRequest.isIgnoreRemote()));
            }
        } catch (WeCrossException e) {
            logger.warn("Process request error: ", e);
            restResponse.setErrorCode(NetworkQueryStatus.NETWORK_PACKAGE_ERROR + e.getErrorCode());
            restResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.warn("Process request error: ", e);
            restResponse.setErrorCode(NetworkQueryStatus.INTERNAL_ERROR);
            restResponse.setMessage(e.getMessage());
        }

        callback.onResponse(restResponse);
    }
}
