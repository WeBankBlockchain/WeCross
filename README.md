![](./docs/images/menu_logo_wecross.svg)

**WeCross**是由微众银行自主研发并完全开源的区块链跨链协作平台，致力于促进跨行业、机构和地域的跨区块链信任传递和商业合作。WeCross不局限于满足同构区块链平行扩展后的可信数据交换需求，还进一步探索解决异构区块链之间因底层架构、数据结构、接口协议、安全机制等多维异构性导致无法互联互通问题的有效方案。

[![CodeFactor](https://www.codefactor.io/repository/github/webankfintech/wecross/badge)](https://www.codefactor.io/repository/github/webankfintech/wecross) [![Build Status](https://travis-ci.org/WeBankFinTech/WeCross.svg?branch=dev)](https://travis-ci.org/WeBankFinTech/WeCross) [![codecov](https://codecov.io/gh/WeBankFinTech/WeCross/branch/dev/graph/badge.svg)](https://codecov.io/gh/WeBankFinTech/WeCross) [![Latest release](https://img.shields.io/github/release/WeBankFinTech/WeCross.svg)](https://github.com/WeBankFinTech/WeCross/releases/latest)
 ![](https://img.shields.io/github/license/WeBankFinTech/WeCross) 

## 设计理念：4S原则

### Synergetic：跨链业务高效协同

跨链的目标是打通区块链业务之间的高墙，连接众多信任孤岛，让信任得到更大范围的传递。为了使这些基于众多区块链平台的业务能够无缝协同，首先需要设计普适通用的数据结构和交互协议，使不同区块链平台之间数据格式转化和网络协议适配所产生的代价降到最低。

WeCross 遵循满足跨链业务高效协同的设计理念，根据“一次适配，随处可用”原则， 提炼跨链交互必需的“核心接口子集”，设计通用数据结构和网络协议，解决因设计目标不同而导致的各平台接口差异性难题。

### Secure：跨链操作安全可信

区块链的重要特征之一是通过多中心化、共识机制以及密码学技术来实现数据可信存取。但这种安全机制往往只能在一个区块链平台内部形成闭环，在两个或者多个区块链平台之间进行交互访问时，需要进一步突破原有平台的安全边界，建立更强的安全保障机制。

WeCross 遵循保障跨链操作安全可信的设计理念，引入 CA 身份认证机制，对通信链路进行加密加固，严格限制访问权限，设计多维度的默克尔证明机制，以及多种原子事务机制，保障跨链交互全流程数据的可信性。

### Scalable：跨链网络分层可扩展

跨链不仅能够支持异构区块链之间互联，也能够帮助同构区块链平台进行扩展。常见的多通道、多群组和多链等扩展方案都需要依赖跨链组件打通通道、群组以及链与链之间的交互。随着跨链业务协作的演进，越来越多的业务有相互连接的需求，一对一的跨链将演变成一对多、多对多、甚至更为复杂的拓扑结构。这就要求跨链组件本身具备足够的灵活性，能够应对多种复杂的网络模型和业务需求。

WeCross 遵循支持跨链网络分层扩展的设计理念，设计跨链路由协议与模块，支持多个区块链分布式互联，承载树型、星型等各种拓扑架构，支持多层次纵深跨链协作。同时，设计多方共建、共治的治理架构，实现跨链网络的可持续扩展。

### Swift：跨链接入高效便捷

由于区块链平台存在多样化特性，开发者每接入一个新的区块链平台就需要学习一套区块链开发运维流程，跨越不同区块链平台的接入将导致学习成本的增加。

WeCross 遵循为开发者提供高效便捷接入方式的理念，设计通用 SDK、交互式控制台以及可  视化  浏览器等全套开发组件，简化跨链交互流程，设计“所见即所得”的运维工具，支持一键发起跨链操作。

综上，4S 设计理念以业务协同为核心，在多个关键维度上追求跨链操作的高安全性、高扩展性和高易用性，以应对未来形式多样、层出不穷的跨链应用场景。

## WeCross整体架构设计

![](./docs/images/architecture.png)

- **数据层**：跨链交互的核心是数据在链间的流动，数据层的抽象就尤为重要。跨链涉及的数据维度包括区块、交易、合约、消息等多个方面。WeCross以满足跨链基本要求为前提，提炼通用区块数据结构，将交易、合约和消息等抽象设计成资源类型，为资源设计通用的寻址协议。


- **交互层**：不同业务场景有不同的跨链交互模型，基于抽象数据层，WeCross建设通用区块链适配与路由中继网络，结合标准默克尔证明机制，实现跨链交互层抽象设计。


- **事务层**：基于数据结构和交互的抽象层，实现跨链事务效果。目前支持两类机制：两阶段事务和哈希时间锁定事务。未来将依据场景需求设计更多事务机制。

## 快速开始

阅读[快速入门](https://wecross.readthedocs.io/zh_CN/dev/docs/tutorial/index.html)，从连通两个区块链开始，快速体验WeCross的强大功能。

## 技术文档

阅读[WeCross 在线文档](https://wecross.readthedocs.io/zh_CN/latest/)，详细了解如何使用WeCross。

- [平台介绍](https://wecross.readthedocs.io/zh_CN/latest/docs/introduction/introduction.html)
- [程序版本](https://wecross.readthedocs.io/zh_CN/latest/docs/version/index.html)
- [快速入门](https://wecross.readthedocs.io/zh_CN/latest/docs/tutorial/index.html)
- [操作手册](https://wecross.readthedocs.io/zh_CN/latest/docs/manual/index.html)
- [跨链接入](https://wecross.readthedocs.io/zh_CN/latest/docs/stubs/index.html)
- [应用场景](https://wecross.readthedocs.io/zh_CN/latest/docs/scenarios/index.html)
- [FAQ](https://wecross.readthedocs.io/zh_CN/latest/docs/faq/faq.html)
- [社区](https://wecross.readthedocs.io/zh_CN/latest/docs/community/community.html)



## 项目贡献

- 点亮我们的小星星(点击项目左上方Star按钮)。
- 提交代码(Pull requests)，参考我们的[代码贡献流程](CONTRIBUTING_CN.md)。
- [提问和提交BUG](https://github.com/WeBankFinTech/WeCross/issues)。

## 社区

联系我们：wecross@webank.com

