package com.webank.wecross.stub;

import java.util.Objects;

public class Path {
    private String zone;
    private String chain;
    private String resource;

    public static Path decode(String path) throws Exception {
        String[] sp = path.split("\\.");
        if (sp.length < 3) {
            throw new Exception("Decode path error: " + path);
        }

        Path obj = new Path();
        obj.setZone(sp[0]);
        obj.setChain(sp[1]);
        obj.setResource(sp[2]);

        return obj;
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
        return zone + "." + chain + "." + resource;
    }

    public String toURI() {
        return zone + "/" + chain + "/" + resource;
    }
}
