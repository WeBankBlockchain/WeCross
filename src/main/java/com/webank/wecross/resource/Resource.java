package com.webank.wecross.resource;

public interface Resource {
    public Path getPath();

    public GetDataResponse getData(GetDataRequest request);

    public SetDataResponse setData(SetDataRequest request);

    public TransactionResponse call(TransactionRequest request);

    public TransactionResponse sendTransaction(TransactionRequest request);

    public void registerEventHandler(EventCallback callback);

    public TransactionRequest createRequest();
}
