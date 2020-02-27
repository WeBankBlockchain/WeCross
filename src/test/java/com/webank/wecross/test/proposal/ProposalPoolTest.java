package com.webank.wecross.test.proposal;

public class ProposalPoolTest {

    // ProposalPool
    /*
    @Test
    public void basicTest() throws Exception {
        ProposalPool pool = new ProposalPool();
        pool.setName("zone.stub.resource proposal pool");
        Assert.assertTrue(pool.isEmpty());

        try {
            pool.put(0, new MockProposal(0));
            Assert.assertFalse(pool.isEmpty());
        } catch (Exception e) {
            Assert.assertTrue(false); // Assert never comes here
        }

        try {
            // Put same seq once again
            pool.put(0, new MockProposal(0)); // expect to throw
            Assert.assertTrue(false); // Assert never comes here
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        pool.clear();
        Assert.assertTrue(pool.isEmpty());

        try {
            for (int seq = 100; seq < 100 + pool.maxSize; seq++) {
                pool.put(seq, new MockProposal(seq));
            }
        } catch (Exception e) {
            Assert.assertTrue(false); // Assert never comes here
        }

        try {
            // Put one more
            pool.put(100 + pool.maxSize, new MockProposal(100 + pool.maxSize)); // expect to throw
            Assert.assertTrue(false); // Assert never comes here
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void timeoutTest() throws Exception {
        ProposalPool pool = new ProposalPool();
        Assert.assertTrue(pool.isEmpty());

        for (int seq = 0; seq < 20; seq++) {
            pool.put(seq, new MockProposal(seq, System.currentTimeMillis() + 2 * 1000));
        }
        Assert.assertFalse(pool.isEmpty());

        Thread.sleep(2500);
        Assert.assertTrue(pool.isEmpty());

        try {
            for (int seq = 1; seq <= 5; seq++) {
                pool.put(100 - seq, new MockProposal(seq, System.currentTimeMillis() + seq * 1000));
            }
            Thread.sleep(200);

            for (int seq = 5; seq >= 0; seq--) {
                System.out.println("Timeout test seq: " + seq);
                Assert.assertEquals(pool.size(), seq);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            Assert.assertTrue(false); // Assert never comes here
        }
    }
    */
}
