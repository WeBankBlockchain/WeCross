package com.webank.wecross.stub.bcos2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.utils.Web3AsyncThreadPoolSize;

import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.URI;

public class BCOS2Stub implements Stub {
	private Service bcosService;
	private Web3j web3;

	public void init() {
		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(bcosService);

		Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 30;
		Web3AsyncThreadPoolSize.web3AsyncPoolSize = 20;

		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(500);
		setWeb3(Web3j.build(channelEthereumService, 15 * 100, scheduledExecutorService, 1));
	}

	@Override
	public Resource getResource(URI path) {
		BCOS2ContractResource bcos2Resource = new BCOS2ContractResource();
		bcos2Resource.setBcos2Service(bcosService);
		bcos2Resource.setWeb3(web3);

		return bcos2Resource;
	}

	public Service getBcosService() {
		return bcosService;
	}

	public void setBcosService(Service bcosService) {
		this.bcosService = bcosService;
	}

	public Web3j getWeb3() {
		return web3;
	}

	public void setWeb3(Web3j web3) {
		this.web3 = web3;
	}
}
