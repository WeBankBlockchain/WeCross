package com.webank.wecross.zone;

import static com.webank.wecross.exception.WeCrossException.ErrorCode.GET_CHAIN_CHECKSUM_ERROR;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.Connection;
import com.webank.wecross.stub.Driver;
import com.webank.wecross.stub.ResourceInfo;
import com.webank.wecross.utils.Sha256Utils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainInfo {
    private static Logger logger = LoggerFactory.getLogger(ChainInfo.class);
    private String name;
    private String stubType;
    private List<ResourceInfo> resources = new LinkedList<>();
    private Map<String, String> properties;
    private String checksum;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStubType() {
        return stubType;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public List<ResourceInfo> getResources() {
        return resources;
    }

    public void setResources(List<ResourceInfo> resources) {
        this.resources = resources;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public static String buildChecksum(Driver driver, Connection connection)
            throws WeCrossException {
        CompletableFuture<byte[]> genensisBlockHeaderFuture = new CompletableFuture<>();
        driver.asyncGetBlock(
                0,
                true,
                connection,
                new Driver.GetBlockCallback() {
                    @Override
                    public void onResponse(Exception e, Block block) {
                        if (!Objects.isNull(e)) {
                            logger.error(
                                    "Could not get genesisBlock from connection: {}",
                                    connection.getProperties());
                            genensisBlockHeaderFuture.complete(null);
                        } else {
                            genensisBlockHeaderFuture.complete(block.getRawBytes());
                        }
                    }
                });

        byte[] genesisBlockHeader;
        try {
            genesisBlockHeader = genensisBlockHeaderFuture.get(20, TimeUnit.SECONDS);
            if (genesisBlockHeader == null) {
                String errorMessage =
                        "Could not get genesisBlock from connection: " + connection.getProperties();
                logger.error(errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (Exception e) {
            throw new WeCrossException(GET_CHAIN_CHECKSUM_ERROR, e.getMessage(), e.getCause());
        }
        String checksum = Sha256Utils.sha256String(genesisBlockHeader);
        return checksum;
    }

    @Override
    public String toString() {
        return "ChainInfo{"
                + "name='"
                + name
                + '\''
                + ", stubType='"
                + stubType
                + '\''
                + ", resources="
                + resources
                + ", properties="
                + properties
                + ", checksum='"
                + checksum
                + '\''
                + '}';
    }
}
