package com.webank.wecross.network.p2p.netty.common;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Additional tools for P2P module */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * create UUID new UUID
     *
     * @return
     */
    public static String newUUID() {
        String seq = UUID.randomUUID().toString().replaceAll("-", "");
        return seq;
    }

    /**
     * @param IP
     * @return true if IP valid IP string otherwise false
     */
    public static boolean validIP(String IP) {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(IP);
        return matcher.matches();
    }

    /**
     * @param port
     * @return true if port valid IP port otherwise false
     */
    public static boolean validPort(Integer port) {
        return port.intValue() > 0 && port.intValue() <= 65535;
    }
}
