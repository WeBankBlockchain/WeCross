## 部署账户服务

- 构建镜像
```shell
cd account-manager

# 执行准备脚本，参数：-b 代码分支
# 默认mysql配置是127.0.0.1:3306, root, 123456
bash prepare.sh -b dev
# 若需要另外配置mysql IP端口账号密码等，则可以输入以下参数
bash prepare.sh -b dev -H 127.0.0.1 -P 3306 -u [username] -p [password]    

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

## 跨链路由接入区块链

在部署好WeCross基础组件后，需要手动接入区块链，并配置跨链账户。

### 接入FISCO BCOS2.0

- 部署并启动FISCO BCOS区块链，要求版本为v2.1.0以上

- 配置插件

```shell
# 进入待接入链的WeCross跨链路由容器
docker exec -it wecross-router bash
# 进入wecross-router挂载目录
cd wecross-router
# 添加FISCO BCOS链，此处的bcos可自定义命名
bash add_chain.sh -t BCOS2.0 -n bcos
# 若需要添加FISCO BCOS国密链，此处的bcos_gm可自定义命名
bash add_chain.sh -t GM_BCOS2.0 -n bcos_gm
# 编辑router连接链的配置项，详情可参考下面的配置举例
vim ./conf/chains/bcos/stub.toml
```

- 编辑router连接链的配置项

```toml
# 配置举例
# conf/chains/bcos/stub.toml
[common]                # 通用配置
    name = 'bcos'       # stub配置名称，即 [stubName] = bcos
    type = 'BCOS2.0'    # stub类型，`GM_BCOS2.0`或者`BCOS2.0`，`GM_BCOS2.0`国密类型，`BCOS2.0`非国密类型

[chain]                 # FISCO-BCOS 链配置
    groupId = 1         # 连接FISCO-BCOS群组id，默认为1
    chainId = 1         # 连接FISCO-BCOS链id，默认为1

[channelService]        # FISCO-BCOS 配置，以下文件在BCOS链的nodes/127.0.0.1/sdk目录下拷贝
    caCert = 'ca.crt'   # 根证书
    sslCert = 'sdk.crt' # SDK证书
    sslKey = 'sdk.key'  # SDK私钥
    gmConnect = false   # 国密连接开关，若为true则使用BCOS节点SDK的国密证书进行连接，反之则使用非国密证书连接
    gmCaCert = 'gm/gmca.crt'        # 国密CA证书
    gmSslCert = 'gm/gmsdk.crt'      # 国密SDK证书
    gmSslKey = 'gm/gmsdk.key'       # 国密SDK密钥
    gmEnSslCert = 'gm/gmensdk.crt'  # 国密加密证书
    gmEnSslKey = 'gm/gmensdk.key'   # 国密加密密钥
    timeout = 5000                  # SDK请求超时时间
    connectionsStr = ['127.0.0.1:20200']    # 连接列表
```

- 拷贝区块链SDK证书到wecross-router

```shell
# 将区块链节点SDK证书从宿主机拷贝至容器中，此处 nodes/127.0.0.1/sdk/ 为区块链的SDK证书目录
docker cp ./nodes/127.0.0.1/sdk/  wecross-router:/wecross-router/conf/chains/bcos
# 再次进入wecross-router容器
docker exec -it wecross-router bash
# 进入链的配置目录，并将`sdk`文件夹中的证书移到外层
cd /wecross-router/conf/chains/bcos && mv ./sdk/* .
```

- 部署系统合约

```shell
# 部署非国密链
  # 部署代理合约
bash deploy_system_contract.sh -t BCOS2.0 -c chains/bcos -P
  # 部署桥接合约
bash deploy_system_contract.sh -t BCOS2.0 -c chains/bcos -H

# 部署国密链
  # 部署代理合约
bash deploy_system_contract.sh -t GM_BCOS2.0 -c chains/bcos -P
  # 部署桥接合约
bash deploy_system_contract.sh -t GM_BCOS2.0 -c chains/bcos -H

# 若后续有更新系统合约的需求，首先更新conf/chains/bcos下的系统合约代码，在上述命令添加-u参数，执行并重启跨链路由
```

- 重启路由

```shell
cd /wecross-router && bash stop.sh && bash start.sh
```

### 接入Hyperledger Fabric 1.4

- 部署并启动Hyperledger Fabric区块链，要求版本为 >= v1.4.0, < v2.0

- 配置插件

```shell
# 进入待接入链的WeCross跨链路由容器
docker exec -it wecross-router bash
# 进入wecross-router挂载目录
cd wecross-router
# 添加Hyperledger Fabric链，此处的fabric可自定义命名
bash add_chain.sh -t Fabric1.4 -n fabric

# 编辑router连接链的配置项，详情可参考下面的配置举例
vim ./conf/chains/fabric/stub.toml
```

- 编辑router连接链的配置项

```toml
# 配置举例
# conf/chains/fabric/stub.toml
[common]
    name = 'fabric'				# 指定的连接的链的名字，与该配置文件所在的目录名一致，对应path中的{zone}/{chain}/{resource}的chain
    type = 'Fabric1.4'			# 插件的类型

[fabricServices]
    channelName = 'mychannel'
    orgUserName = 'fabric_admin' # 指定一个机构的admin账户，用于与orderer通信
    ordererTlsCaFile = 'orderer-tlsca.crt' # orderer证书名字，指向与此配置文件相同目录下的证书
    ordererAddress = 'grpcs://localhost:7050' # orderer的url

[orgs] # 机构节点列表
    [orgs.Org1] # 机构1：Org1
        tlsCaFile = 'org1-tlsca.crt' # Org1的证书
        adminName = 'fabric_admin_org1' # Org1的admin账户，在下一步骤中配置
        endorsers = ['grpcs://localhost:7051'] # endorser的ip:port列表，可配置多个

    [orgs.Org2] # 机构2：Org2
        tlsCaFile = 'org2-tlsca.crt' # Org2的证书
        adminName = 'fabric_admin_org2' # Org2的admin账户，在下一步骤中配置
        endorsers = ['grpcs://localhost:9051'] # endorser的ip:port列表，可配置多个
```
- 配置账户
```shell
# 接入Fabric链，需要配置一个admin账户
bash add_account.sh -t Fabric1.4 -n fabric_admin 

# 为Fabric链的每个Org都配置一个admin账户，此处有两个org（Org1和Org2），分别配两个账户
  # 配Org1的admin
bash add_account.sh -t Fabric1.4 -n fabric_admin_org1

  # 配Org2的admin
bash add_account.sh -t Fabric1.4 -n fabric_admin_org2

# 修改mspid，将 'Org1MSP' 更改为 'Org2MSP'
vim conf/accounts/fabric_admin_org2/account.toml
```

- 拷贝区块链证书以及账户证书到wecross-router

```shell
# 从宿主机将证书密钥配置进wecross-router容器

# 配置fabric_admin 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin/account.crt

# 配置fabric_admin_org1 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin_org1/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin_org1/account.crt

# 配置fabric_admin_org2 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin_org2/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/signcerts/Admin@org2.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin_org2/account.crt

# 拷贝orderer证书
docker cp ${FABRIC_NETWORK}/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem   wecross-router:/wecross-router/conf/chains/fabric/orderer-tlsca.crt
  # 拷贝org1证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt   wecross-router:/wecross-router/conf/chains/fabric/org1-tlsca.crt
  # 拷贝org2证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt   wecross-router:/wecross-router/conf/chains/fabric/org2-tlsca.crt
```

- 部署系统合约

```shell
# 部署代理合约
bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -P

# 部署桥接合约
bash deploy_system_contract.sh -t Fabric1.4 -c chains/fabric -H

# 若后续有更新系统合约的需求，首先更新conf/chains/fabric下的系统合约代码，在上述命令添加-u参数，执行并重启跨链路由
```

- 重启路由

```shell
cd /wecross-router && bash stop.sh && bash start.sh
```

### 接入Hyperledger Fabric 2.0

- 部署并启动Hyperledger Fabric区块链，要求版本为 >= v2.0

- 配置插件

```shell
# 进入待接入链的WeCross跨链路由容器
docker exec -it wecross-router bash
# 进入wecross-router挂载目录
cd wecross-router
# 添加Hyperledger Fabric链，此处的fabric可自定义命名
bash add_chain.sh -t Fabric2.0 -n fabric2

# 编辑router连接链的配置项，详情可参考下面的配置举例
vim ./conf/chains/fabric2/stub.toml
```

- 编辑router连接链的配置项

```toml
# 配置举例
# conf/chains/fabric2/stub.toml
[common]
    name = 'fabric2'				# 指定的连接的链的名字，与该配置文件所在的目录名一致，对应path中的{zone}/{chain}/{resource}的chain
    type = 'Fabric1.4'			# 插件的类型

[fabricServices]
    channelName = 'mychannel'
    orgUserName = 'fabric_admin' # 指定一个机构的admin账户，用于与orderer通信
    ordererTlsCaFile = 'orderer-tlsca.crt' # orderer证书名字，指向与此配置文件相同目录下的证书
    ordererAddress = 'grpcs://localhost:7050' # orderer的url

[orgs] # 机构节点列表
    [orgs.Org1] # 机构1：Org1
        tlsCaFile = 'org1-tlsca.crt' # Org1的证书
        adminName = 'fabric_admin_org1' # Org1的admin账户，在下一步骤中配置
        endorsers = ['grpcs://localhost:7051'] # endorser的ip:port列表，可配置多个

    [orgs.Org2] # 机构2：Org2
        tlsCaFile = 'org2-tlsca.crt' # Org2的证书
        adminName = 'fabric_admin_org2' # Org2的admin账户，在下一步骤中配置
        endorsers = ['grpcs://localhost:9051'] # endorser的ip:port列表，可配置多个
```
- 配置账户
```shell
# 接入Fabric链，需要配置一个admin账户
bash add_account.sh -t Fabric2.0 -n fabric_admin 

# 为Fabric链的每个Org都配置一个admin账户，此处有两个org（Org1和Org2），分别配两个账户
  # 配Org1的admin
bash add_account.sh -t Fabric2.0 -n fabric_admin_org1

  # 配Org2的admin
bash add_account.sh -t Fabric2.0 -n fabric_admin_org2

# 修改mspid，将 'Org1MSP' 更改为 'Org2MSP'
vim conf/accounts/fabric_admin_org2/account.toml
```

- 拷贝区块链证书以及账户证书到wecross-router

```shell
# 从宿主机将证书密钥配置进wecross-router容器

# 配置fabric_admin 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin/account.crt

# 配置fabric_admin_org1 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin_org1/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts/Admin@org1.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin_org1/account.crt

# 配置fabric_admin_org2 
  # 拷贝私钥
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/keystore/*_sk   wecross-router:/wecross-router/conf/accounts/fabric_admin_org2/account.key
  # 拷贝证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp/signcerts/Admin@org2.example.com-cert.pem   wecross-router:/wecross-router/conf/accounts/fabric_admin_org2/account.crt

# 拷贝orderer证书
docker cp ${FABRIC_NETWORK}/crypto-config/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem   wecross-router:/wecross-router/conf/chains/fabric2/orderer-tlsca.crt
  # 拷贝org1证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt   wecross-router:/wecross-router/conf/chains/fabric2/org1-tlsca.crt
  # 拷贝org2证书
docker cp ${FABRIC_NETWORK}/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt   wecross-router:/wecross-router/conf/chains/fabric2/org2-tlsca.crt
```

- 部署系统合约

```shell
# 部署代理合约
bash deploy_system_contract.sh -t Fabric2.0 -c chains/fabric2 -P

# 部署桥接合约
bash deploy_system_contract.sh -t Fabric2.0 -c chains/fabric2 -H

# 若后续有更新系统合约的需求，首先更新conf/chains/fabric下的系统合约代码，在上述命令添加-u参数，执行并重启跨链路由
```

- 重启路由

```shell
cd /wecross-router && bash stop.sh && bash start.sh
```

## 重启wecross-router容器

当遇到需要更新区块链配置、变更路由配置或变更区块链时，需要重启wecross-router容器。

因为wecross-router容器的配置是挂载到容器的，因此，再次启动容器，并将配置目录挂载到容器内即可。

```shell
# 重启启动容器
cd wecross-router
# 将 account-conf 目录挂载到容器的 /wecross-router/conf 目录
docker run --name wecross-router --network host -v router-conf:/wecross-router/conf -itd wecross-router:dev bash
```

