package com.webank.wecross.storage;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBBlockHeaderStorage implements BlockHeaderStorage {
    private RocksDB rocksDB;

    private static final String numberKey = "number";
    private static final String blockKeyPrefix = "block_";

    private Logger logger = LoggerFactory.getLogger(RocksDBBlockHeaderStorage.class);

    @Override
    public long readBlockNumber() {
        try {
            byte[] blockNumberBytes = rocksDB.get(numberKey.getBytes());
            if (blockNumberBytes != null) {
                String blockNumberStr = new String(blockNumberBytes);
                long blockNumber = Long.valueOf(blockNumberStr);
                return blockNumber;
            } else {
                return 0;
            }
        } catch (RocksDBException e) {
            logger.error("Read rocksdb error", e);
        }
        return 0;
    }

    @Override
    public byte[] readBlockHeader(long blockNumber) {
        String key = "block_" + String.valueOf(blockNumber);

        try {
            return rocksDB.get(key.getBytes());
        } catch (RocksDBException e) {
            logger.error("RocksDB read error", e);
        }
        return null;
    }

    @Override
    public void writeBlockHeader(long blockNumber, byte[] blockHeader) {
        String key = blockKeyPrefix + String.valueOf(blockNumber);

        try {
            WriteBatch writeBatch = new WriteBatch();
            writeBatch.put(numberKey.getBytes(), String.valueOf(blockNumber).getBytes());
            writeBatch.put(key.getBytes(), blockHeader);

            WriteOptions writeOptions = new WriteOptions();

            rocksDB.write(writeOptions, writeBatch);
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
