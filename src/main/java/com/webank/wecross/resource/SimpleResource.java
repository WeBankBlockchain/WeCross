package com.webank.wecross.resource;

// No reliable chain, just respond what you call
public class SimpleResource extends Resource {
    @Override
    public GetDataResponse getData(GetDataRequest request) {
        return null;
    }

    @Override
    public SetDataResponse setData(SetDataRequest request) {
        return null;
    }

    @Override
    public TransactionResponse call(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("Call simplie resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public TransactionResponse sendTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        response.setErrorCode(0);
        response.setErrorMessage("sendTransaction simplie resource success");
        response.setHash("010157f4");
        response.setResult(new Object[] {request});
        return response;
    }

    @Override
    public void registerEventHandler(EventCallback callback) {}

    @Override
    public TransactionRequest createRequest() {
        return null;
    }

    @Override
    public int getDistance() {
        return 0;
    }
}
