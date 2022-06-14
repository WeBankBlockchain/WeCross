package com.webank.wecross.stub;

import java.util.Objects;

public class Path {
    private String zone;
    private String chain;
    private String resource;

    public static Path decode(String path) throws Exception {
        String[] sp = path.split("\\.");
        if (sp.length < 2) {
            throw new Exception("Decode path error: " + path);
        }

        for (String name : sp) {
            if (name == null || name.length() == 0) {
                throw new Exception("Decode path error: " + path);
            }
        }

        Path obj = new Path();
        obj.setZone(sp[0]);
        obj.setChain(sp[1]);
        if (sp.length > 2) {
            obj.setResource(sp[2]);
        }

        return obj;
    }

    public Path() {}

    public Path(Path path) {
        this.zone = path.getZone();
        this.chain = path.getChain();
        this.resource = path.getResource();
    }

    public Path(String str) throws Exception {
        Path path = Path.decode(str);
        this.zone = path.zone;
        this.chain = path.chain;
        this.resource = path.resource;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Path)) {
            return false;
        }
        Path path = (Path) o;
        return Objects.equals(getZone(), path.getZone())
                && Objects.equals(getChain(), path.getChain())
                && Objects.equals(getResource(), path.getResource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getZone(), getChain(), getResource());
    }

    @Override
    public String toString() {
        if (Objects.nonNull(resource)) {
            return zone + "." + chain + "." + resource;
        } else {
            return zone + "." + chain;
        }
    }

    // compact zone and chain as ChainName
    public String toChainName() {
        return zone + "." + chain;
    }

    // whether in chain
    public boolean isInChain(String chainName) {
        return chainName.equals(toChainName());
    }

    public String toURI() {
        if (Objects.nonNull(resource)) {
            return zone + "/" + chain + "/" + resource;
        } else {
            return zone + "/" + chain;
        }
    }
}
