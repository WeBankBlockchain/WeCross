package com.webank.wecross.utils;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.FIELD_MISSING;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class ConfigUtils {

    public static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    public static final String CERT_PATTERN =
            "^-{5}BEGIN CERTIFICATE-{5}$(?s).*?^-{5}END CERTIFICATE-{5}\n$";

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
        } catch (IllegalStateException e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.UNEXPECTED_CONFIG,
                    "Toml file " + fileName + "format error: " + e.getMessage());
        } catch (FileNotFoundException e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.DIR_NOT_EXISTS,
                    "Toml file " + fileName + "not found: " + e.getMessage());
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.INTERNAL_ERROR,
                    "Something wrong with parse " + fileName + ": " + e.getMessage());
        }
    }

    public static String classpath2Absolute(String fileName) throws WeCrossException {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            return resolver.getResource(fileName).getFile().getAbsolutePath();
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
            logger.debug("Local stubs: {} is empty", stubsPath);
            // throw new WeCrossException(FIELD_MISSING,ResourceQueryStatus.DIR_NOT_EXISTS,
            // errorMessage);
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

        String[] stubsDir = dir.list();
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

    public static boolean parseBoolean(Toml toml, String key, boolean defaultReturn) {
        Boolean res = toml.getBoolean(key);

        if (res == null) {
            logger.info("{} has not set, default to {}", key, defaultReturn);
            return defaultReturn;
        }
        return res;
    }

    public static int parseInt(Toml toml, String key, int defaultReturn) {
        Long res = toml.getLong(key);

        if (res == null) {
            logger.info(key + " has not set, default to {}", defaultReturn);
            return defaultReturn;
        }
        return res.intValue();
    }

    public static int parseInt(Toml toml, String key) throws WeCrossException {
        Long res = toml.getLong(key);

        if (res == null) {
            String errorMessage = "'" + key + "' item not found";
            throw new WeCrossException(FIELD_MISSING, errorMessage);
        }
        return res.intValue();
    }

    public static long parseLong(Toml toml, String key, long defaultReturn) {
        Long res = toml.getLong(key);

        if (res == null) {
            logger.info(key + " has not set, default to {}", defaultReturn);
            return defaultReturn;
        }
        return res.longValue();
    }

    public static String parseString(Toml toml, String key, String defaultReturn) {
        try {
            return parseString(toml, key);
        } catch (WeCrossException e) {
            return defaultReturn;
        }
    }

    public static String parseString(Toml toml, String key) throws WeCrossException {
        String res = toml.getString(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new WeCrossException(FIELD_MISSING, errorMessage);
        }
        return res;
    }

    public static String parseString(Map<String, String> map, String key) throws WeCrossException {
        String res = map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new WeCrossException(FIELD_MISSING, errorMessage);
        }
        return res;
    }

    public static String parseStringBase(Map<String, Object> map, String key)
            throws WeCrossException {
        @SuppressWarnings("unchecked")
        String res = (String) map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item not found";
            throw new WeCrossException(FIELD_MISSING, errorMessage);
        }
        return res;
    }

    public static List<String> parseStringList(Map<String, Object> map, String key)
            throws WeCrossException {
        @SuppressWarnings("unchecked")
        List<String> res = (List<String>) map.get(key);

        if (res == null) {
            String errorMessage = "\"" + key + "\" item illegal";
            throw new WeCrossException(FIELD_MISSING, errorMessage);
        }
        return res;
    }

    public static Map<String, String> parseMapBase(Map<String, Object> map, String key)
            throws WeCrossException {
        @SuppressWarnings("unchecked")
        Map<String, String> res = (Map<String, String>) map.get(key);

        if (res == null) {
            throw new WeCrossException(FIELD_MISSING, "'" + key + "' item not found");
        }
        return res;
    }
}
