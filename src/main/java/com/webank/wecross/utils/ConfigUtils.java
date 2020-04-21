package com.webank.wecross.utils;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static void checkPath(String path) throws WeCrossException {
        String templateUrl = WeCrossDefault.TEMPLATE_URL + path.replace('.', '/');

        try {
            new URL(templateUrl);
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.ILLEGAL_SYMBOL, "Invalid path: " + path);
        }
    }

    public static Toml getToml(String fileName) throws WeCrossException {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            return new Toml().read(resolver.getResource(fileName).getInputStream());
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "Something wrong with parse " + fileName + ": " + e.getMessage());
        }
    }

    public static Map<String, Object> getTomlMap(String fileName) throws WeCrossException {
        return getToml(fileName).toMap();
    }

    public static Map<String, String> getStubsDir(String stubsPath) throws WeCrossException {
        Map<String, String> result = new HashMap<>();

        File dir;
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            dir = resolver.getResource(stubsPath).getFile();
            // dir = new File(ConfigUtils.class.getClassLoader().getResource(stubsPath).getFile());
        } catch (Exception e) {
            logger.debug("Local stubs: " + stubsPath + " is empty");
            // throw new WeCrossException(ResourceQueryStatus.DIR_NOT_EXISTS, errorMessage);
            return result;
        }

        if (!dir.isDirectory()) {
            String errorMessage = stubsPath + " is not a valid directory";
            throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
        }

        String thisPath = stubsPath;
        if (stubsPath.endsWith("/")) {
            thisPath = stubsPath.substring(0, stubsPath.length() - 1);
        }

        String stubsDir[] = dir.list();
        for (String stub : stubsDir) {
            // ignore hidden dir
            if (stub.startsWith(".")) {
                continue;
            }

            String stubPath = thisPath + File.separator + stub;
            if (!fileIsExists(stubPath + File.separator + WeCrossDefault.STUB_CONFIG_FILE)) {
                String errorMessage = "Stub configuration file: " + stubPath + " does not exist";
                throw new WeCrossException(WeCrossException.ErrorCode.DIR_NOT_EXISTS, errorMessage);
            }
            result.put(stub, stubPath);
        }

        logger.info("Stub config files: {}", result);
        return result;
    }

    // Check if the file exists or not
    public static boolean fileIsExists(String path) {
        try {
            PathMatchingResourcePatternResolver resolver_temp =
                    new PathMatchingResourcePatternResolver();
            resolver_temp.getResource(path).getFile();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
