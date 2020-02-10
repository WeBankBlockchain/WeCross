package com.webank.wecross.stub.fabric.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricSdkConfig {

    private Logger logger = LoggerFactory.getLogger(FabricSdkConfig.class);
    private FabricConfig fabricConfig = new FabricConfig();

    public FabricConfig getFabricConfig() {
        return fabricConfig;
    }

    public void setFabricConfig(FabricConfig fabricConfig) {
        this.fabricConfig = fabricConfig;
    }

    public FabricSdk initFabricStub() {
        FabricSdk fabricSdk = new FabricSdk();
        try {

            logger.info("fabricConfig info:{}", fabricConfig);
            HFClient hfClient = initializeClient(fabricConfig);
            fabricSdk.setHfClient(hfClient);
            Channel channel = this.initializeChannel(hfClient, fabricConfig);
            fabricSdk.setChannel(channel);
        } catch (InvalidArgumentException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException
                | NoSuchMethodException
                | CryptoException
                | ClassNotFoundException
                | TransactionException e) {
            logger.error("initFabricStub failed:{}", e);
        }
        return fabricSdk;
    }

    public HFClient initializeClient(FabricConfig fabricConfig)
            throws InvalidArgumentException, IllegalAccessException, InvocationTargetException,
                    InstantiationException, NoSuchMethodException, CryptoException,
                    ClassNotFoundException {
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        hfClient.setUserContext(new FabricUser(fabricConfig));
        return hfClient;
    }

    // Create Channel
    public Channel initializeChannel(HFClient client, FabricConfig fabricConfig)
            throws InvalidArgumentException, TransactionException {
        Orderer orderer1 = getOrderer(client, fabricConfig);
        Channel channel = client.newChannel(fabricConfig.getChannelName());
        channel.addOrderer(orderer1);
        for (int i = 0; i < fabricConfig.getPeerConfigs().size(); i++) {
            FabricPeerConfig perrConfig = fabricConfig.getPeerConfigs().get(i);
            Peer peer = getPeer(client, perrConfig, i);
            channel.addPeer(peer);
        }
        channel.initialize();
        return channel;
    }

    public Orderer getOrderer(HFClient client, FabricConfig fabricConfig)
            throws InvalidArgumentException {
        Properties orderer1Prop = new Properties();
        orderer1Prop.setProperty("pemFile", fabricConfig.getOrdererTlsCaFile());
        orderer1Prop.setProperty("sslProvider", "openSSL");
        orderer1Prop.setProperty("negotiationType", "TLS");
        orderer1Prop.setProperty("ordererWaitTimeMilliSecs", "300000");
        orderer1Prop.setProperty("hostnameOverride", "orderer");
        orderer1Prop.setProperty("trustServerCertificate", "true");
        orderer1Prop.setProperty("allowAllHostNames", "true");
        Orderer orderer =
                client.newOrderer("orderer", fabricConfig.getOrdererAddress(), orderer1Prop);
        return orderer;
    }

    public Peer getPeer(HFClient client, FabricPeerConfig perrConfig, Integer index)
            throws InvalidArgumentException {
        logger.info("getPeerTlsCaFile:{}", perrConfig.getPeerTlsCaFile());
        Properties peer0Prop = new Properties();
        peer0Prop.setProperty("pemFile", perrConfig.getPeerTlsCaFile());
        peer0Prop.setProperty("sslProvider", "openSSL");
        peer0Prop.setProperty("negotiationType", "TLS");
        peer0Prop.setProperty("hostnameOverride", "peer0");
        peer0Prop.setProperty("trustServerCertificate", "true");
        peer0Prop.setProperty("allowAllHostNames", "true");
        Peer peer = client.newPeer("peer" + index, perrConfig.getPeerAddress(), peer0Prop);
        return peer;
    }
}
