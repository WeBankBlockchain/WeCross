package com.webank.wecross.test.routine.xa;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.routine.xa.XATransactionManager;
import com.webank.wecross.zone.Zone;
import com.webank.wecross.zone.Chain;
import com.webank.wecross.zone.ZoneManager;
import com.webank.wecross.stub.Account;
import com.webank.wecross.stub.Path;
import com.webank.wecross.stubmanager.BlockHeaderManager;

public class XATransactionManagerTest {
	@Test
	public void testPrepare() throws Exception {
		BlockHeaderManager blockHeaderManager = Mockito.mock(BlockHeaderManager.class);
		
		Chain chain = Mockito.mock(Chain.class);
		Mockito.when(chain.getBlockHeaderManager()).thenReturn(blockHeaderManager);
		
		Zone zone = Mockito.mock(Zone.class);
		Mockito.when(zone.getChain(Mockito.any(Path.class))).thenReturn(chain);
		
		Resource proxyResource = Mockito.mock(Resource.class);
		
		ZoneManager zoneManager = Mockito.mock(ZoneManager.class);
		Mockito.when(zoneManager.getZone(Mockito.any(Path.class))).thenReturn(zone);
		
		XATransactionManager xaTransactionManager = new XATransactionManager();
		xaTransactionManager.setZoneManager(zoneManager);
		
		String transactionID = "0001";
		Account account = Mockito.mock(Account.class);
		List<Path> resources = new ArrayList<Path>();
		resources.add(Path.decode("a.b.c1"));
		resources.add(Path.decode("a.b.c2"));
		
		xaTransactionManager.asyncPrepare(transactionID, account, resources, (e, result) -> {
			Assert.assertNull(e);
			Assert.assertEquals(0, result);
		});
	}
}
