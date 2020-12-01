package com.webank.wecross;

import com.webank.wecross.config.StubManagerConfig;
import com.webank.wecross.stub.StubFactory;
import com.webank.wecross.stubmanager.StubManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Generator {
    private static final int ARGS_LENGTH = 3;
    private static ApplicationContext context;

    public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(StubManagerConfig.class);

        if (args.length < ARGS_LENGTH) {
            System.out.println("Usage: connection/account <type> <path> <args>");
            return;
        }

        String op = args[0];
        String type = args[1];
        String path = args[2];
        System.out.println(String.format("operator: " + op + " type: " + type + " path: " + path));

        StubManager stubManager = context.getBean(StubManager.class);
        try {
            StubFactory stubFactory = stubManager.getStubFactory(type);

            if (op.equals("connection")) {
                stubFactory.generateConnection(path, new String[] {});
            } else if (op.equals("account")) {
                stubFactory.generateAccount(path, new String[] {});
            } else {
                System.err.println("Unknown operation: " + op);
            }
        } catch (Exception e) {
            System.err.println("Error" + e.toString());
            return;
        }
    }
}
