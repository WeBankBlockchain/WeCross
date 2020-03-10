package com.webank.wecross.config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

import com.webank.wecross.stub.Stub;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stub.StubManager;

@Configuration
public class StubManagerConfig {
	private Logger logger = LoggerFactory.getLogger(StubManagerConfig.class);
	
	@Bean
	public StubManager newStubManager() {
		StubManager stubManager = new StubManager();
		
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resources = resourcePatternResolver.getResources(ClassUtils.convertClassNameToResourcePath("cn.webank.stub.wecross.*"));
			
			MetadataReaderFactory metadataReaderFabtFactory = new SimpleMetadataReaderFactory();
			for(Resource resource: resources) {
				MetadataReader metadataReader = metadataReaderFabtFactory.getMetadataReader(resource);
				if(metadataReader.getAnnotationMetadata().hasAnnotation(Stub.class.getName())) {
					Map<String, Object> attributes = metadataReader.getAnnotationMetadata().getAnnotationAttributes(Stub.class.getName());
					String name = (String) attributes.get("value");
					
					Class<?> claz = Class.forName(metadataReader.getClassMetadata().getClassName());
					StubFactory stubFactory = (StubFactory) claz.getDeclaredConstructor().newInstance();
					
					stubManager.addDriver(name, stubFactory.newDriver());
					
					logger.info("Loaded stub {}", name);
				}
			}
		} catch (Exception e) {
			logger.error("Loading stub error", e);
		}
		
		return stubManager;
	}
}
