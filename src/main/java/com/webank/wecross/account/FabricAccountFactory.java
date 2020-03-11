package com.webank.wecross.account;

import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.ErrorCode;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.utils.ConfigUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.fisco.bcos.channel.client.PEMManager;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class FabricAccountFactory {
    public Logger logger = LoggerFactory.getLogger(FabricAccountFactory.class);

    public FabricAccount build(String name, String accountPath) throws Exception {
        User user = buildUser(name, accountPath);
        FabricAccount account = new FabricAccount(user);
        return account;
    }

    public User buildUser(String name, String accountPath) throws Exception {
        String accountConfigFile =
                accountPath + File.separator + WeCrossDefault.ACCOUNT_CONFIG_NAME;
        Map<String, String> accountConfig =
                (Map<String, String>) ConfigUtils.getTomlMap(accountConfigFile).get("account");

        String mspid = accountConfig.get("mspid");
        Enrollment enrollment = buildEnrollment(accountPath);
        return new User() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Set<String> getRoles() {
                return new HashSet<String>();
            }

            @Override
            public String getAccount() {
                return "";
            }

            @Override
            public String getAffiliation() {
                return "";
            }

            @Override
            public Enrollment getEnrollment() {
                return enrollment;
            }

            @Override
            public String getMspId() {
                return mspid;
            }
        };
    }

    public Enrollment buildEnrollment(String accountPath) throws Exception {
        String accountConfigFile =
                accountPath + File.separator + WeCrossDefault.ACCOUNT_CONFIG_NAME;
        Map<String, String> accountConfig =
                (Map<String, String>) ConfigUtils.getTomlMap(accountConfigFile).get("account");

        String keystoreFile = accountPath + File.separator + accountConfig.get("keystore");
        if (keystoreFile == null) {
            String errorMessage =
                    "\"keystore\" in [account] item not found, please check " + accountConfigFile;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        String signcertFile = accountPath + File.separator + accountConfig.get("signcert");
        if (signcertFile == null) {
            String errorMessage =
                    "\"certFile\" in [account] item not found, please check " + accountConfigFile;
            throw new WeCrossException(ErrorCode.FIELD_MISSING, errorMessage);
        }

        PrivateKey privateKey = loadPemPrivateKey(keystoreFile);
        String certContent = loadPemCert(signcertFile);

        return new Enrollment() {
            @Override
            public PrivateKey getKey() {
                return privateKey;
            }

            @Override
            public String getCert() {
                return certContent;
            }
        };
    }

    public PrivateKey loadPemPrivateKey(String keyPath) throws Exception {
        PEMManager pem = new PEMManager();
        pem.setPemFile(keyPath);
        pem.load();
        return pem.getPrivateKey();
    }

    public String loadPemCert(String certPath) throws Exception {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Path path = Paths.get(resolver.getResource(certPath).getURI());
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            logger.error("getKey failed path:{} errmsg:{}", certPath, e);
            return "";
        }
    }
}
