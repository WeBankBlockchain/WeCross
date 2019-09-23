package com.webank.wecross.stub.bcos2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.utils.Web3AsyncThreadPoolSize;

import com.webank.wecross.bcp.BlockHeader;
import com.webank.wecross.bcp.Receipt;
import com.webank.wecross.bcp.Resource;
import com.webank.wecross.bcp.Stub;
import com.webank.wecross.bcp.Transaction;

public class BCOS2Stub implements Stub{
	private Service bcosService;
	private Web3j web3;
	
	public void init() {
		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(bcosService);
		
		Web3AsyncThreadPoolSize.web3AsyncCorePoolSize = 3000;
        Web3AsyncThreadPoolSize.web3AsyncPoolSize = 2000;

        ScheduledExecutorService scheduledExecutorService =
                Executors.newScheduledThreadPool(500);
        web3 =
                Web3j.build(
                        channelEthereumService,
                        15 * 100,
                        scheduledExecutorService,
                        1);
	}

	@Override
	public Resource getResource(String path) {
		BCOS2Resource bcos2Resource = new BCOS2Resource();
		
		return null;
	}

	@Override
	public BlockHeader getBlockHeader(Integer number) {
		return null;
	}
	
	public Service getBcosService() {
		return bcosService;
	}

	public void setBcosService(Service bcosService) {
		this.bcosService = bcosService;
	}
}
