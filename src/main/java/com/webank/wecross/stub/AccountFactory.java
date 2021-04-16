package com.webank.wecross.stub;

import java.util.Map;

public interface AccountFactory {
    Account build(Map<String, Object> properties);
}
