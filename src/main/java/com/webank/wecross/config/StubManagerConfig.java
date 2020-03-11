package com.webank.wecross.config;

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.StubManager;
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
        StubManager stubManager = new StubManager();

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            /*
            Resource[] resources =
                    resourcePatternResolver.getResources(
                            ClassUtils.convertClassNameToResourcePath("cn.webank.wecross.*"));
                            */
            Resource[] resources =
                    resourcePatternResolver.getResources(
                            ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                                    + ClassUtils.convertClassNameToResourcePath("com.webank")
                                    + "/**/*.class");
            logger.debug("Total {} resources", resources.length);

            MetadataReaderFactory metadataReaderFabtFactory = new SimpleMetadataReaderFactory();
            for (Resource resource : resources) {
                logger.debug("Reading class: {}", resource.getURI().toString());
                MetadataReader metadataReader =
                        metadataReaderFabtFactory.getMetadataReader(resource);
                if (metadataReader.getAnnotationMetadata().hasAnnotation(Stub.class.getName())) {
                    Map<String, Object> attributes =
                            metadataReader
                                    .getAnnotationMetadata()
                                    .getAnnotationAttributes(Stub.class.getName());
                    String name = (String) attributes.get("value");

                    Class<?> claz = Class.forName(metadataReader.getClassMetadata().getClassName());
                    StubFactory stubFactory =
                            (StubFactory) claz.getDeclaredConstructor().newInstance();

                    stubManager.addStubFactory(name, stubFactory);

                    logger.info("Loaded stub {}", name);
                }
            }
        } catch (Exception e) {
            logger.error("Loading stub error", e);
        }

        return stubManager;
    }
}
