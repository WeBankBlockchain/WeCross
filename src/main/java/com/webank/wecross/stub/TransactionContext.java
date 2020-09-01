package com.webank.wecross.stub;

public class TransactionContext {
    private Account account;
    private Path path;
    private ResourceInfo resourceInfo;
    private BlockHeaderManager blockHeaderManager;

    public TransactionContext(
            Account account,
            Path path,
            ResourceInfo resourceInfo,
            BlockHeaderManager blockHeaderManager) {
        this.account = account;
        this.path = path;
        this.resourceInfo = resourceInfo;
        this.blockHeaderManager = blockHeaderManager;
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
                + "account="
                + account
                + ", resourceInfo="
                + resourceInfo.toString()
                + ", blockHeaderManager="
                + blockHeaderManager
                + '}';
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
