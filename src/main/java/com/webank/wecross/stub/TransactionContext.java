package com.webank.wecross.stub;

import com.webank.wecross.stubmanager.BlockHeaderManager;

public class TransactionContext<T> {
    private T data;
    private Account account;
    // private Path path;
    private ResourceInfo resourceInfo;
    private BlockHeaderManager blockHeaderManager;

    public TransactionContext(
            T data,
            Account account,
            ResourceInfo resourceInfo,
            BlockHeaderManager blockHeaderManager) {
        this.data = data;
        this.account = account;
        this.resourceInfo = resourceInfo;
        this.blockHeaderManager = blockHeaderManager;
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

    @Override
    public String toString() {
        return "TransactionContext{"
                + "data="
                + data.toString()
                + ", account="
                + account
                + ", resourceInfo="
                + resourceInfo.toString()
                + ", blockHeaderManager="
                + blockHeaderManager
                + '}';
    }
}
