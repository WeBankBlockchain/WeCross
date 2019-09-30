package com.webank.wecross.stub.bcos;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.utils.Web3AsyncThreadPoolSize;

import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.URI;

public class BCOSStub implements Stub {
	private String pattern;
	private Service bcosService;
	private Web3j web3;
	private Map<String, BCOSResource> resources;

	public void init() {
		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(bcosService);

		Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 30;
		Web3AsyncThreadPoolSize.web3AsyncPoolSize = 20;

		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(500);
		setWeb3(Web3j.build(channelEthereumService, 15 * 100, scheduledExecutorService, 1));
	}
	
	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public Resource getResource(URI path) {
		BCOSResource resource = resources.get(path.getResource());
		
		if(resource != null) {
			resource.setWeb3(web3);
			resource.setBcosService(bcosService);
			
			return resource;
		}

		return resource;
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
