package com.webank.wecross.stub;

import java.util.Objects;

public class Path {
    private String network;
    private String chain;
    private String resource;

    public static Path decode(String path) throws Exception {
        String[] sp = path.split("\\.");
        if (sp.length < 3) {
            throw new Exception("Decode path error: " + path);
        }

        Path obj = new Path();
        obj.setNetwork(sp[0]);
        obj.setChain(sp[1]);
        obj.setResource(sp[2]);

        return obj;
    }

    /**
     * @param uri
     * @return
     */
    public static Path fromURI(String uri) {
        String[] sp = uri.split("/");
        Path obj = new Path();
        obj.setNetwork(sp[0]);
        obj.setChain(sp[1]);
        obj.setResource(sp[2]);

        return obj;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
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
        return Objects.equals(getNetwork(), path.getNetwork())
                && Objects.equals(getChain(), path.getChain())
                && Objects.equals(getResource(), path.getResource());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNetwork(), getChain(), getResource());
    }

    @Override
    public String toString() {
        return network + "." + chain + "." + resource;
    }

    public String toURI() {
        return network + "/" + chain + "/" + resource;
    }
}
