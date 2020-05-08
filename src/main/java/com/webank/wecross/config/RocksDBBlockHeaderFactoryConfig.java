package com.webank.wecross.config;

import com.moandjiezana.toml.Toml;
import com.webank.wecross.storage.BlockHeaderStorageFactory;
import com.webank.wecross.storage.RocksDBBlockHeaderStorageFactory;
import javax.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocksDBBlockHeaderFactoryConfig {
    @Resource(name = "newToml")
    Toml toml;

    @Bean
    BlockHeaderStorageFactory newBlockHeaderStorageFactory() {
        System.out.println("Initializing BlockHeaderStorageFactory ...");

        String basePath = toml.getString("db.path", "db/");

        RocksDBBlockHeaderStorageFactory rocksDBBlockHeaderStorageFactory =
                new RocksDBBlockHeaderStorageFactory();
        rocksDBBlockHeaderStorageFactory.setBasePath(basePath);
        return rocksDBBlockHeaderStorageFactory;
    }
}
