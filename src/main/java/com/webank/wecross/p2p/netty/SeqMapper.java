package com.webank.wecross.p2p.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** manage uniq sequence to callback object */
@Component
public class SeqMapper {
    /** sequence to callback object mapper */
    private Map<String, Object> mapper = new ConcurrentHashMap<String, Object>();

    public Map<String, Object> getMapper() {
        return mapper;
    }

    public void setMapper(Map<String, Object> mapper) {
        this.mapper = mapper;
    }

    public Object get(String seq) {
        return mapper.get(seq);
    }

    public Object getAndRemove(String seq) {
        Object object = mapper.get(seq);
        mapper.remove(seq);
        return object;
    }

    public void add(String seq, Object object) {
        this.mapper.put(seq, object);
    }

    public void remove(String seq) {
        mapper.remove(seq);
    }
}
