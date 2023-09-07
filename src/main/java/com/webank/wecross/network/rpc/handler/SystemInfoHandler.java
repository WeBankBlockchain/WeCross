package com.webank.wecross.network.rpc.handler;

import com.webank.wecross.account.UserContext;
import com.webank.wecross.common.WeCrossDefault;
import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.host.WeCrossHost;
import com.webank.wecross.network.UriDecoder;
import com.webank.wecross.restserver.RestResponse;
import com.webank.wecross.restserver.response.StubResponse;
import com.webank.wecross.stubmanager.StubManager;
import com.webank.wecross.zone.ZoneManager;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.security.Provider;
import java.security.Security;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GET /sys/supportedStubs */
public class SystemInfoHandler implements URIHandler {
    private WeCrossHost host;
    private static final Logger logger = LoggerFactory.getLogger(SystemInfoHandler.class);

    public SystemInfoHandler(WeCrossHost host) {
        this.host = host;
    }

    public WeCrossHost getHost() {
        return host;
    }

    public void setHost(WeCrossHost host) {
        this.host = host;
    }

    @Override
    public void handle(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        UriDecoder uriDecoder = new UriDecoder(uri);
        String operation = uriDecoder.getMethod();
        switch (operation) {
            case "supportedStubs":
                listStubs(userContext, uri, method, content, callback);
                break;
            case "systemStatus":
                systemStatus(userContext, uri, method, content, callback);
                break;
            case "routerStatus":
                routerStatus(userContext, uri, method, content, callback);
                break;
        }
    }

    private void listStubs(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        RestResponse<StubResponse> restResponse = new RestResponse<>();
        StubResponse stubResponse = new StubResponse();
        ZoneManager zoneManager = host.getZoneManager();
        StubManager stubManager = zoneManager.getStubManager();
        stubResponse.setStubTypes(stubManager);
        restResponse.setData(stubResponse);
        callback.onResponse(restResponse);
    }

    private class SystemStatus {
        private String osName;
        private String osArch;
        private String osVersion;

        private String javaVMVersion;
        private String javaVMVendor;
        private String javaVMName;

        private String providerName;
        private String providerVersion;
        private String providerInfo;

        private String namedGroups;
        private String disabledNamedGroups;

        private String totalDiskSpace;
        private String totalDiskFreeSpace;
        private String totalDiskUsable;

        private String totalMemorySize;
        private String freeMemorySize;

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsArch() {
            return osArch;
        }

        public void setOsArch(String osArch) {
            this.osArch = osArch;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public String getJavaVMVersion() {
            return javaVMVersion;
        }

        public void setJavaVMVersion(String javaVMVersion) {
            this.javaVMVersion = javaVMVersion;
        }

        public String getJavaVMVendor() {
            return javaVMVendor;
        }

        public void setJavaVMVendor(String javaVMVendor) {
            this.javaVMVendor = javaVMVendor;
        }

        public String getJavaVMName() {
            return javaVMName;
        }

        public void setJavaVMName(String javaVMName) {
            this.javaVMName = javaVMName;
        }

        public String getProviderName() {
            return providerName;
        }

        public void setProviderName(String providerName) {
            this.providerName = providerName;
        }

        public String getProviderVersion() {
            return providerVersion;
        }

        public void setProviderVersion(String providerVersion) {
            this.providerVersion = providerVersion;
        }

        public String getProviderInfo() {
            return providerInfo;
        }

        public void setProviderInfo(String providerInfo) {
            this.providerInfo = providerInfo;
        }

        public String getNamedGroups() {
            return namedGroups;
        }

        public void setNamedGroups(String namedGroups) {
            this.namedGroups = namedGroups;
        }

        public String getDisabledNamedGroups() {
            return disabledNamedGroups;
        }

        public void setDisabledNamedGroups(String disabledNamedGroups) {
            this.disabledNamedGroups = disabledNamedGroups;
        }

        public String getTotalDiskSpace() {
            return totalDiskSpace;
        }

        public void setTotalDiskSpace(String totalDiskSpace) {
            this.totalDiskSpace = totalDiskSpace;
        }

        public String getTotalDiskFreeSpace() {
            return totalDiskFreeSpace;
        }

        public void setTotalDiskFreeSpace(String totalDiskFreeSpace) {
            this.totalDiskFreeSpace = totalDiskFreeSpace;
        }

        public String getTotalDiskUsable() {
            return totalDiskUsable;
        }

        public void setTotalDiskUsable(String totalDiskUsable) {
            this.totalDiskUsable = totalDiskUsable;
        }

        public String getTotalMemorySize() {
            return totalMemorySize;
        }

        public void setTotalMemorySize(String totalMemorySize) {
            this.totalMemorySize = totalMemorySize;
        }

        public String getFreeMemorySize() {
            return freeMemorySize;
        }

        public void setFreeMemorySize(String freeMemorySize) {
            this.freeMemorySize = freeMemorySize;
        }
    }

    private void systemStatus(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        SystemStatus status = new SystemStatus();
        status.setOsArch(System.getProperty("os.arch"));
        status.setOsName(System.getProperty("os.name"));
        status.setOsVersion(System.getProperty("os.version"));

        status.setJavaVMVersion(System.getProperty("java.vm.version"));
        status.setJavaVMVendor(System.getProperty("java.vm.vendor"));
        status.setJavaVMName(System.getProperty("java.vm.name"));

        status.setNamedGroups(System.getProperty("jdk.tls.namedGroups"));
        status.setDisabledNamedGroups(System.getProperty("jdk.disabled.namedCurves"));

        Provider provider = Security.getProviders()[0];
        status.setProviderInfo(provider.getInfo());
        status.setProviderName(provider.getName());
        status.setProviderVersion(String.valueOf(provider.getVersion()));

        // disk space usage
        File[] disks = File.listRoots();
        long total = 0;
        long free = 0;
        long usable = 0;
        for (File disk : disks) {
            // B to GB
            total += disk.getTotalSpace() / 1024 / 1024 / 1024;
            free += disk.getFreeSpace() / 1024 / 1024 / 1024;
            usable += disk.getUsableSpace() / 1024 / 1024 / 1024;
        }
        status.setTotalDiskSpace(total + "GB");
        status.setTotalDiskFreeSpace(free + "GB");
        status.setTotalDiskUsable(usable + "GB");

        // memory usage
        com.sun.management.OperatingSystemMXBean operatingSystemMXBean =
                (com.sun.management.OperatingSystemMXBean)
                        ManagementFactory.getOperatingSystemMXBean();
        long totalPhysicalMemorySize =
                operatingSystemMXBean.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024;
        long freePhysicalMemorySize =
                operatingSystemMXBean.getFreePhysicalMemorySize() / 1024 / 1024 / 1024;
        status.setTotalMemorySize(totalPhysicalMemorySize + "GB");
        status.setFreeMemorySize(freePhysicalMemorySize + "GB");

        RestResponse<SystemStatus> restResponse = new RestResponse<SystemStatus>();
        restResponse.setData(status);

        callback.onResponse(restResponse);
    }

    private class RouterStatus {
        private String version;
        private String supportedStubs;
        private String rpcNetInfo;
        private String p2pNetInfo;
        private String adminAccount;
        private boolean enableAccessControl;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getSupportedStubs() {
            return supportedStubs;
        }

        public void setSupportedStubs(String supportedStubs) {
            this.supportedStubs = supportedStubs;
        }

        public String getRpcNetInfo() {
            return rpcNetInfo;
        }

        public void setRpcNetInfo(String rpcNetInfo) {
            this.rpcNetInfo = rpcNetInfo;
        }

        public String getP2pNetInfo() {
            return p2pNetInfo;
        }

        public void setP2pNetInfo(String p2pNetInfo) {
            this.p2pNetInfo = p2pNetInfo;
        }

        public String getAdminAccount() {
            return adminAccount;
        }

        public void setAdminAccount(String adminAccount) {
            this.adminAccount = adminAccount;
        }

        public boolean isEnableAccessControl() {
            return enableAccessControl;
        }

        public void setEnableAccessControl(boolean enableAccessControl) {
            this.enableAccessControl = enableAccessControl;
        }
    }

    private void routerStatus(
            UserContext userContext, String uri, String method, String content, Callback callback) {
        RouterStatus routerStatus = new RouterStatus();
        routerStatus.setVersion(WeCrossDefault.VERSION);
        String supportedStubs =
                host.getZoneManager()
                        .getStubManager()
                        .getStubFactories()
                        .keySet()
                        .stream()
                        .collect(Collectors.joining(","));

        routerStatus.setSupportedStubs(supportedStubs);
        try {
            routerStatus.setAdminAccount(host.getAccountManager().getAdminUA().getName());
        } catch (WeCrossException e) {
            logger.error("Get AdminUA error", e);
        }

        routerStatus.setP2pNetInfo(
                host.getP2PService().getNettyService().getInitializer().getConfig().getListenIP()
                        + ":"
                        + host.getP2PService()
                                .getNettyService()
                                .getInitializer()
                                .getConfig()
                                .getListenPort());
        routerStatus.setRpcNetInfo(
                host.getRpcService().getRpcBootstrap().getConfig().getListenIP()
                        + ":"
                        + host.getRpcService().getRpcBootstrap().getConfig().getListenPort());

        routerStatus.setEnableAccessControl(
                host.getAccountManager()
                        .getUniversalAccountFactory()
                        .getFilterFactory()
                        .isEnableAccessControl());

        RestResponse<RouterStatus> restResponse = new RestResponse<RouterStatus>();
        restResponse.setData(routerStatus);

        callback.onResponse(restResponse);
    }
}
