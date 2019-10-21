package com.webank.wecross.resource;

public interface Resource {
    public Path getPath();

    public void setPath(Path path);

    public GetDataResponse getData(GetDataRequest request);

    public SetDataResponse setData(SetDataRequest request);

    public TransactionResponse call(TransactionRequest request);

    public TransactionResponse sendTransaction(TransactionRequest request);

    public void registerEventHandler(EventCallback callback);

    public TransactionRequest createRequest();

    public int getAccessDepth(); // 0 local, > 0 remote

    public boolean isLocal();
}
