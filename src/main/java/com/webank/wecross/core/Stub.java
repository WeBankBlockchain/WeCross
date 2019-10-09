package com.webank.wecross.core;

import com.webank.wecross.resource.Resource;
import com.webank.wecross.resource.URI;

public interface Stub {
  public void init() throws Exception;

  public String getPattern();

  public Resource getResource(URI uri) throws Exception;
}
