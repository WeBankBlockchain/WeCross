package com.webank.wecross.network.rpc.web;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.rpc.handler.URIHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class WebURIHandler implements URIHandler {
    private static Logger logger = LoggerFactory.getLogger(WebURIHandler.class);
    private String webRoot;

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {

        try {
            String filePath = toLocalPath(uri);
            File file = getLocalFile(filePath);
            callback.onResponse(file);
        } catch (WeCrossException e) {
            if (e.getErrorCode().equals(WeCrossException.ErrorCode.PAGE_NOT_FOUND)) {
                response404(callback);
            } else {
                responseError("Error: " + e.getMessage(), callback);
            }

        } catch (Exception e) {
            responseError(e.getMessage(), callback);
        }
    }

    private void responseError(String message, Callback callback) {
        callback.onResponse(message);
    }

    private void response404(Callback callback) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        callback.onResponse(response);
    }

    private void checkLocalPath(String localPath) throws Exception {
        File file = new File(localPath);
        String absolutePath = file.getAbsolutePath();

        if (!absolutePath.startsWith(getWebRoot())) {
            logger.warn("Invalid uri, path:" + localPath);
            throw new Exception("Invalid uri");
        }

        if (!file.exists()) {
            logger.warn("Not found:" + localPath);
            throw new Exception("Invalid uri");
        }
    }

    private File getLocalFile(String localPath) throws WeCrossException {
        try {
            checkLocalPath(localPath);

            return new File(localPath);

        } catch (Exception e) {
            logger.warn("getLocalFile e:", e);
            throw new WeCrossException(WeCrossException.ErrorCode.PAGE_NOT_FOUND, e.getMessage());
        }
    }

    private String toLocalPath(String uri) {

        return uri.replaceFirst("/s/", getWebRoot()).replace("/", File.separator);
    }

    public void setWebRoot(String rootPath) {
        try {
            // to avoid path manipulation
            rootPath = rootPath.replace("..", "");
            Path path;

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            path = Paths.get(resolver.getResource(rootPath).getURI());

            this.webRoot = path + File.separator;

        } catch (Exception e) {
            logger.error("Web root: {} not found", rootPath);
        }
    }

    public String getWebRoot() {
        return webRoot;
    }
}
