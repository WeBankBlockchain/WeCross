package com.webank.wecross.resource;

public class URI {
    private String network;
    private String chain;
    private String resource;

    public static URI decode(String uri) throws Exception {
        String[] sp = uri.split("\\.");
        if (sp.length < 3) {
            throw new Exception("Decode uri error: " + uri);
        }

        URI obj = new URI();
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
}
