package com.webank.wecross.resource;

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

    public boolean equals(Path path) {
        return path.getNetwork().equals(this.network)
                && path.getChain().equals(this.chain)
                && path.getResource().equals(this.resource);
    }

    @Override
    public String toString() {
        return network + "." + chain + "." + resource;
    }
}
