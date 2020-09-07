package com.webank.wecross;

import com.webank.wecross.config.StubManagerConfig;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.StubManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Generator {
    private static final Logger logger  = LoggerFactory.getLogger(Generator.class);
    private static final int ARGS_LENGTH = 3;
    private static ApplicationContext context;
    public static void main(String[] args) {

        context = new AnnotationConfigApplicationContext(StubManagerConfig.class);

        if (args.length < ARGS_LENGTH) {
            logger.debug("Usage: connection/account <type> <path> <args>");
            return;
        }

        String op = args[0];
        String type = args[1];
        String path = args[2];
        logger.debug("operator:{} type:{} path:{}", op, type, path);

        StubManager stubManager = context.getBean(StubManager.class);
        try {
            StubFactory stubFactory = stubManager.getStubFactory(type);

            if (op.equals("connection")) {
                stubFactory.generateConnection(path, new String[] {});
            } else if (op.equals("account")) {
                stubFactory.generateAccount(path, new String[] {});
            } else {
                logger.error("Unknown operation: " + op);
            }
        } catch (Exception e) {
            logger.error("Error" + e.toString());
            return;
        }
    }
}
