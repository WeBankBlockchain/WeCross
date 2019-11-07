package com.webank.wecross.p2p.netty.common;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
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

    /**
     * get peer ip, port from channel connect context
     *
     * @param context
     * @return
     */
    public static Host channelContextPeerHost(ChannelHandlerContext context) {

        if (null == context) {
            return null;
        }

        String host =
                ((SocketChannel) context.channel()).remoteAddress().getAddress().getHostAddress();
        Integer port = ((SocketChannel) context.channel()).remoteAddress().getPort();

        return new Host(host, port);
    }
}
