package com.webank.wecross.stub;

public class TransactionContext<T> {
    private T data;
    private Account account;
    // private Path path;
    private ResourceInfo resourceInfo;
    private BlockHeaderManager blockHeaderManager;

    public TransactionContext(T data, Account account, ResourceInfo resourceInfo) {
        this.data = data;
        this.account = account;
        this.resourceInfo = resourceInfo;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public BlockHeaderManager getBlockHeaderManager() {
        return blockHeaderManager;
    }

    public void setBlockHeaderManager(BlockHeaderManager blockHeaderManager) {
        this.blockHeaderManager = blockHeaderManager;
    }
}
