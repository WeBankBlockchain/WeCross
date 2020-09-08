package com.webank.wecross.stub;

public class TransactionContext {
    private Account account;
    private Path path;
    private ResourceInfo resourceInfo;
    private BlockManager blockManager;

    public TransactionContext(
            Account account, Path path, ResourceInfo resourceInfo, BlockManager blockManager) {
        this.account = account;
        this.path = path;
        this.resourceInfo = resourceInfo;
        this.blockManager = blockManager;
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

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public void setBlockManager(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @Override
    public String toString() {
        return "TransactionContext{"
                + "account="
                + account
                + ", resourceInfo="
                + resourceInfo.toString()
                + ", blockManager="
                + blockManager
                + '}';
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
