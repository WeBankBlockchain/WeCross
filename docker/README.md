## 部署账户服务

- 构建镜像
```shell
cd account-manager

# 执行准备脚本，参数：代码分支
bash prepare.sh dev

# 构建镜像
docker build -t wecross-account-manager:dev -m 4g .
```

- 完成配置
```shell
cd account-manager/account-conf

# 根据实际情况完成 account-conf 目录下的配置
```

- 启动容器

```shell
cd account-manager
# 将 account-conf 目录挂载到容器的 /wecross-account-manager/conf 目录
docker run --name wecross-account-manager --network host -v account-conf:/wecross-account-manager/conf -itd wecross-account-manager:dev bash
```

## 部署跨链路由

- 构建镜像
```shell
cd wecross-router

# 执行准备脚本，参数：代码分支
bash prepare.sh dev

# 构建镜像
docker build -t wecross-router:dev -m 4g .
```

- 完成配置
```shell
cd wecross-router/router-conf

# 根据实际情况完成 router-conf 目录下的配置
```

- 启动容器

```shell
cd wecross-router
# 将 account-conf 目录挂载到容器的 /wecross-router/conf 目录
docker run --name wecross-router --network host -v router-conf:/wecross-router/conf -itd wecross-router:dev bash
```
