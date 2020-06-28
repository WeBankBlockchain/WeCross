package com.webank.wecross.utils;


import com.fasterxml.jackson.databind.ObjectMapper;

public  class ObjectMapperFactory {
    public static ObjectMapper getObjectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }
}
