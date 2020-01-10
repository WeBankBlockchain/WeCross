![](./docs/images/menu_logo_wecross.svg)

**WeCross** 是由微众银行自主研发并完全开源的分布式商业区块链跨链协作平台。该平台能解决业界主流的区块链产品间接口不互通、无法协作的问题，以及区块链系统无法平行扩展、计算能力和存储容量存在瓶颈等问题。WeCross作为未来分布式商业区块链互联的基础架构，秉承公众联盟链多方参与、共享资源、智能协同和价值整合的理念，致力于促进跨行业、机构和地域的跨区块链价值交换和商业合作，实现了高效、通用和安全的区块链跨链协作机制。

[![CodeFactor](https://www.codefactor.io/repository/github/webankfintech/wecross/badge)](https://www.codefactor.io/repository/github/webankfintech/wecross) [![Build Status](https://travis-ci.org/WeBankFinTech/WeCross.svg?branch=dev)](https://travis-ci.org/WeBankFinTech/WeCross) [![codecov](https://codecov.io/gh/WeBankFinTech/WeCross/branch/dev/graph/badge.svg)](https://codecov.io/gh/WeBankFinTech/WeCross) [![Latest release](https://img.shields.io/github/release/WeBankFinTech/WeCross.svg)](https://github.com/WeBankFinTech/WeCross/releases/latest)
 ![](https://img.shields.io/github/license/WeBankFinTech/WeCross) 

## 快速开始

下载

``` shell
bash <(curl -sL https://github.com/WeBankFinTech/WeCross/releases/download/resources/download_wecross.sh)
```

生成

``` shell
cd WeCross
bash build_wecross.sh -n payment -l 127.0.0.1:8250:25500 -T
```

启动

``` shell
cd wecross/127.0.0.1-8250-25500/
bash start.sh
```

更全面的操作请参考[快速入门](https://wecross.readthedocs.io/zh_CN/dev/docs/tutorial/index.html)。

## 源码编译

```shell
./gradlew assemble
```

如果编译成功，将在当前目录生成一个dist目录。

## 技术文档

[WeCross 在线文档](https://wecross.readthedocs.io/zh_CN/latest/)

## 项目贡献

- 点亮我们的小星星(点击项目左上方Star按钮)。
- 提交代码(Pull requests)，参考我们的[代码贡献流程](CONTRIBUTING_CN.md)。
- [提问和提交BUG](https://github.com/WeBankFinTech/WeCross/issues)。

## 社区

联系我们：wecross@webank.com

