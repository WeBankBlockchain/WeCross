package com.webank.wecross.config;

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.StubManager;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

@Configuration
public class StubManagerConfig {
    private Logger logger = LoggerFactory.getLogger(StubManagerConfig.class);

    @Bean
    public StubManager newStubManager() {
        System.out.println("Initializing StubManager...");

        StubManager stubManager = new StubManager();

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources =
                    resourcePatternResolver.getResources(
                            ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                                    + ClassUtils.convertClassNameToResourcePath("com.webank")
                                    + "/**/*.class");
            if (logger.isDebugEnabled()) {
                logger.debug("Total {} resources", resources.length);
            }

            MetadataReaderFactory metadataReaderFabtFactory = new SimpleMetadataReaderFactory();
            for (Resource resource : resources) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Scan stub plugin: {}", resource.getURI().toString());
                }
                MetadataReader metadataReader =
                        metadataReaderFabtFactory.getMetadataReader(resource);
                if (metadataReader.getAnnotationMetadata().hasAnnotation(Stub.class.getName())) {
                    Map<String, Object> attributes =
                            metadataReader
                                    .getAnnotationMetadata()
                                    .getAnnotationAttributes(Stub.class.getName());
                    String name = (String) attributes.get("value");

                    if (stubManager.hasFactory(name)) {
                        throw new Exception(
                                "Duplicate stub plugin["
                                        + name
                                        + "]: "
                                        + resource.getURI().toString());
                    }

                    Class<?> claz = Class.forName(metadataReader.getClassMetadata().getClassName());
                    StubFactory stubFactory =
                            (StubFactory) claz.getDeclaredConstructor().newInstance();

                    stubManager.addStubFactory(name, stubFactory);

                    logger.info("Load stub plugin[" + name + "]: " + resource.getURI().toString());
                }
            }
        } catch (Exception e) {
            String errorMsg = "Loading stub error: " + e;
            logger.error(errorMsg);
            System.exit(-1);
        }

        return stubManager;
    }
}
