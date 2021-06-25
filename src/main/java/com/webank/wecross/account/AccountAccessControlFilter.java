package com.webank.wecross.account;

import com.webank.wecross.exception.WeCrossException;
import com.webank.wecross.stub.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AccountAccessControlFilter {
    Map<String, Map<String, Collection<String>>> allowPaths =
            new HashMap<>(); // zone 2 chain 2 resource

    public AccountAccessControlFilter(String[] accountAllowPaths) throws WeCrossException {
        if (accountAllowPaths == null) {
            return;
        }

        Collection<Path> zonePaths = new HashSet<>();
        Collection<Path> chainPaths = new HashSet<>();
        Collection<Path> resourcePaths = new HashSet<>();

        // need sort and add. eg: given [a.b.c, a.b, a, d.e, f.g.h] we need to add [a, d.e, f.g.h]
        // only
        for (String allowPath : accountAllowPaths) {
            Path path = null;
            try {
                path = Path.decode(allowPath);
            } catch (Exception e) {
                throw new WeCrossException(
                        WeCrossException.ErrorCode.PATH_FORMAT_ERROR, e.getMessage());
            }

            if (path.getZone() == null || path.getZone().equals("*")) {
                continue;
            }

            if (path.getChain() == null || path.getChain().equals("*")) {
                zonePaths.add(path);
                continue;
            }

            if (path.getResource() == null || path.getResource().equals("*")) {
                chainPaths.add(path);
                continue;
            }

            resourcePaths.add(path);
        }

        // add [a] first
        for (Path path : zonePaths) {
            addAllowPath(path);
        }

        // add [a.b, d.e] (a.b will be ignore)
        for (Path path : chainPaths) {
            addAllowPath(path);
        }

        // add [a.b.c, f.g.h] (a.b.c will be ignore)
        for (Path path : resourcePaths) {
            addAllowPath(path);
        }
    }

    public void addAllowPath(Path path) throws WeCrossException {
        if (!hasPermission(path)) {
            addAllowPathForce(path);
        }
    }

    private void addAllowPathForce(Path path) {
        String zone = path.getZone();
        String chain = path.getChain();
        String resource = path.getResource();

        if (zone == null || zone.length() == 0 || zone.equals("*")) {
            return;
        }
        allowPaths.putIfAbsent(zone, new HashMap<>());

        if (chain == null || chain.length() == 0 || chain.equals("*")) {
            return;
        }
        Map<String, Collection<String>> allowChainPaths = allowPaths.get(zone);
        allowChainPaths.putIfAbsent(chain, new HashSet<>());

        if (resource == null || resource.length() == 0 || resource.equals("*")) {
            return;
        }
        Collection<String> allowResourcePath = allowChainPaths.get(chain);
        allowResourcePath.add(resource);
    }

    public boolean hasPermission(String path) throws WeCrossException {

        try {
            return hasPermission(Path.decode(path));
        } catch (Exception e) {
            throw new WeCrossException(
                    WeCrossException.ErrorCode.PATH_FORMAT_ERROR, e.getMessage());
        }
    }

    public boolean hasPermission(Path path) {
        String zone = path.getZone();
        String chain = path.getChain();
        String resource = path.getResource();
        if (zone == null || zone.length() == 0) {
            return true; // null can be access by everyone
        }

        if (hasAllChainPermission(zone)) {
            return true;
        }

        if (chain == null || chain.length() == 0 || chain.equals("*")) {
            return false; // only give zone path, but allowPaths has some certain chains, denied
        }

        if (hasAllResourcePermission(zone, chain)) {
            return true;
        }

        if (resource == null || resource.length() == 0 || resource.equals("*")) {
            return false; // only give zone.chain path, but allowPaths has sone certain resource,
            // denied
        }

        return hasResourcePermission(zone, chain, resource);
    }

    private boolean hasAllChainPermission(String zone) {
        // has all permission under this zone
        Map<String, Collection<String>> allowChainPaths = allowPaths.get(zone);
        return allowChainPaths != null && allowChainPaths.isEmpty();
    }

    private boolean hasAllResourcePermission(String zone, String chain) {
        // has all permission under this zone.chain
        Map<String, Collection<String>> allowChainPaths = allowPaths.get(zone);
        if (allowChainPaths != null && !allowChainPaths.isEmpty()) {

            Collection<String> allowResourcePaths = allowChainPaths.get(chain);
            return allowResourcePaths != null && allowResourcePaths.isEmpty();
        }
        return false;
    }

    private boolean hasResourcePermission(String zone, String chain, String resource) {
        Map<String, Collection<String>> allowChainPaths = allowPaths.get(zone);
        if (allowChainPaths != null && !allowChainPaths.isEmpty()) {
            Collection<String> allowResourcePaths = allowChainPaths.get(chain);
            if (allowResourcePaths != null && !allowResourcePaths.isEmpty()) {
                return allowResourcePaths.contains(resource);
            }
        }
        return false;
    }

    public String[] dumpPermission() {

        List<String> res = new LinkedList<>();

        for (Map.Entry<String, Map<String, Collection<String>>> zone2chains :
                allowPaths.entrySet()) {
            String zone = zone2chains.getKey();
            Path path = new Path();
            path.setZone(zone);

            Map<String, Collection<String>> allowChainPaths = zone2chains.getValue();
            if (allowChainPaths == null || allowChainPaths.isEmpty()) {
                path.setChain("*");
                res.add(path.toString());
            } else {
                for (Map.Entry<String, Collection<String>> chain2Resources :
                        allowChainPaths.entrySet()) {
                    String chain = chain2Resources.getKey();
                    path.setChain(chain);
                    Collection<String> allowResourcePaths = chain2Resources.getValue();
                    if (allowResourcePaths == null || allowResourcePaths.isEmpty()) {
                        path.setResource("*");
                        res.add(path.toString());
                    } else {
                        for (String resource : allowResourcePaths) {
                            path.setResource(resource);
                            res.add(path.toString());
                        }
                    }
                }
            }
        }
        return res.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.dumpPermission());
    }
}
