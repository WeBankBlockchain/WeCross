package com.webank.wecross.storage;

import java.io.File;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBBlockHeaderStorageFactory implements BlockHeaderStorageFactory {
    private Logger logger = LoggerFactory.getLogger(RocksDBBlockHeaderStorageFactory.class);
    private String basePath;

    public RocksDBBlockHeaderStorageFactory() {
        RocksDB.loadLibrary();
    }

    @Override
    public BlockHeaderStorage newBlockHeaderStorage(String path) {
        RocksDBBlockHeaderStorage rocksDBBlockHeaderStorage = new RocksDBBlockHeaderStorage();
        Options options = new Options();
        options.setCreateIfMissing(true);
        options.setCreateMissingColumnFamilies(true);

        String dbPath = basePath + "/" + path;
        try {
            File dir = new File(dbPath);
            if (!dir.exists()) {
                dir.mkdirs();
            } else {
                if (!dir.isDirectory()) {
                    logger.error("File {} exists and isn't dir", dbPath);
                }
            }

            RocksDB rocksDB = RocksDB.open(options, dbPath);
            rocksDBBlockHeaderStorage.setRocksDB(rocksDB);
        } catch (RocksDBException e) {
            logger.error("RocksDB open failed", e);
        }
        return rocksDBBlockHeaderStorage;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
