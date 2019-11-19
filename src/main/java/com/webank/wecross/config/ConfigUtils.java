package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.exception.Status;
import com.webank.wecross.exception.WeCrossException;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static void checkPath(String path) throws WeCrossException {
        String templateUrl = "http://127.0.0.1:8080/" + path.replace('.', '/');

        try {
            new URL(templateUrl);
        } catch (Exception e) {
            throw new WeCrossException(Status.ILLEGAL_SYMBOL, "Invalid path: " + path);
        }
    }

    public static Toml getToml(String fileName) throws WeCrossException {
        try {
            Path path = Paths.get(ClassLoader.getSystemResource(fileName).toURI());
            String encryptedSecret = new String(Files.readAllBytes(path));
            return new Toml().read(encryptedSecret);
        } catch (Exception e) {
            throw new WeCrossException(
                    Status.INTERNAL_ERROR,
                    "Something wrong with parse " + fileName + ": " + e.getLocalizedMessage());
        }
    }

    public static Map<String, String> getStubsDir(String stubsPath) throws WeCrossException {
        File dir = new File(ConfigUtils.class.getClassLoader().getResource(stubsPath).getFile());
        if (!dir.isDirectory()) {
            String errorMessage = "Stubs directory not exists";
            throw new WeCrossException(Status.DIR_NOT_EXISTS, errorMessage);
        }

        Map<String, String> result = new HashMap<>();
        String stubsDir[] = dir.list();
        for (String stub : stubsDir) {
            String stubPath = stubsPath + "/" + stub + "/" + ConfigInfo.STUB_CONFIG_FILE;
            result.put(stub, stubPath);
        }

        logger.info("Stub config files: {}", result);
        return result;
    }
}
