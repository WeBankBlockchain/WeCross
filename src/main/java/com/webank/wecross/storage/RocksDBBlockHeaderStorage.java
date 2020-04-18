package com.webank.wecross.storage;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBBlockHeaderStorage implements BlockHeaderStorage {
    private boolean dbClosed = false;

    private RocksDB rocksDB;

    private static final String numberKey = "number";
    private static final String blockKeyPrefix = "block_";

    private Logger logger = LoggerFactory.getLogger(RocksDBBlockHeaderStorage.class);

    @Override
    public long readBlockNumber() {
        if (dbClosed == true) {
            logger.warn("Read RocksDB error: RocksDB has been closed");
            return -1;
        }

        try {
            byte[] blockNumberBytes = rocksDB.get(numberKey.getBytes());
            if (blockNumberBytes != null) {
                String blockNumberStr = new String(blockNumberBytes);
                long blockNumber = Long.valueOf(blockNumberStr);
                return blockNumber;
            } else {
                return -1;
            }
        } catch (RocksDBException e) {
            logger.error("Read RocksDB error", e);
        }
        return -1;
    }

    @Override
    public byte[] readBlockHeader(long blockNumber) {
        if (dbClosed == true) {
            logger.warn("Read RocksDB error: RocksDB has been closed");
            return null;
        }

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
        if (dbClosed == true) {
            logger.warn("Write RocksDB error: RocksDB has been closed");
            return;
        }

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
        this.dbClosed = false;
    }

    @Override
    public void close() {
        dbClosed = true;
        rocksDB.close();
    }
}
