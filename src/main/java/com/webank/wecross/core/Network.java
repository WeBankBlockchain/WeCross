package com.webank.wecross.core;

import com.webank.wecross.resource.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {
  private Map<String, Stub> stubs;
  private Logger logger = LoggerFactory.getLogger(Network.class);

  public Stub getStub(URI uri) {
    return getStub(uri.getChain());
  }

  public Stub getStub(String name) {
    logger.trace("get stub: {}", name);

    Stub stub = stubs.get(name);
    try {
      stub.init();
    } catch (Exception e) {
      logger.error("Error while get stub:", e);
      return null;
    }
    return stub;
  }

  public Map<String, Stub> getStubs() {
    return stubs;
  }

  public void setStubs(Map<String, Stub> stubs) {
    this.stubs = stubs;
  }
}
