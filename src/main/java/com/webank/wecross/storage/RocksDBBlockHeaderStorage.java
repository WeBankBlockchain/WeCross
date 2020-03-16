package com.webank.wecross.storage;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBBlockHeaderStorage implements BlockHeaderStorage {
    private RocksDB rocksDB;

    private static final String numberKey = "number";
    private static final String blockKeyPrefix = "block_";

    private Logger logger = LoggerFactory.getLogger(RocksDBBlockHeaderStorage.class);

    @Override
    public int readBlockNumber() {
        try {
            byte[] blockNumberBytes = rocksDB.get(numberKey.getBytes());
            String blockNumberStr = new String(blockNumberBytes);
            int blockNumber = Integer.valueOf(blockNumberStr);

            return blockNumber;
        } catch (RocksDBException e) {
            logger.error("Read rocksdb error", e);
        }
        return 0;
    }

    @Override
    public byte[] readBlockHeader(int blockNumber) {
        String key = "block_" + blockNumber;

        try {
            return rocksDB.get(key.getBytes());
        } catch (RocksDBException e) {
            logger.error("RocksDB read error", e);
        }
        return null;
    }

    @Override
    public void writeBlockHeader(int blockNumber, byte[] blockHeader) {
        String key = blockKeyPrefix + String.valueOf(blockNumber);

        try {
            rocksDB.put(key.getBytes(), blockHeader);
        } catch (RocksDBException e) {
            logger.error("RocksDB write error", e);
        }
    }

    public RocksDB getRocksDB() {
        return rocksDB;
    }

    public void setRocksDB(RocksDB rocksDB) {
        this.rocksDB = rocksDB;
    }
}
