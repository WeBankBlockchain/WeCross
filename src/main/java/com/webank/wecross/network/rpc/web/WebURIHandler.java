package com.webank.wecross.network.rpc.web;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.network.rpc.handler.URIHandler;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class WebURIHandler implements URIHandler {
    private static Logger logger = LoggerFactory.getLogger(WebURIHandler.class);

    private static final String WEB_ROOT = getWebRoot("classpath:/pages/");

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {

        try {
            String filePath = toLocalPath(uri);
            File file = getLocalFile(filePath);
            callback.onResponse(file);
        } catch (WeCrossException e) {
            if (e.getErrorCode().equals(WeCrossException.ErrorCode.PAGE_NOT_FOUND)) {
                responseError("Not found! " + e.getMessage(), callback);
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

    private void checkLocalPath(String localPath) throws Exception {
        File file = new File(localPath);
        String absolutePath = file.getAbsolutePath();

        if (!absolutePath.startsWith(WEB_ROOT)) {
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

        return uri.replaceFirst("/s/", WEB_ROOT).replace("/", File.separator);
    }

    private static String getWebRoot(String rootPath) {
        try {
            Path path;

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            path = Paths.get(resolver.getResource(rootPath).getURI());

            return path.toString() + File.separator;

        } catch (Exception e) {
            logger.error("Web root: {} not found", rootPath);
            return "";
        }
    }
}
