---
name: Default template
about: Default template for pull request
title: '<type>(scope): subject'
---

**标题**

不超过80个字符，英文

格式：
```
<type>(scope): subject
```
示例：
```
<feat>(interchain): support inter-chain call from contract
```

**type**

type用于说明PR的类别，只允许使用下面的标识

- feat: 新功能

- fix: 修复bug

- perf: 优化相关，比如提升性能、体验

- test: 增加测试

- style: 格式（不影响代码运行的变动）

- refactor: 重构（即不是新增功能，也不是修改bug的代码变动）

- docs: 文档

- chore: 构建过程或辅助工具的变动

- revert: 回滚到上一个版本

- merge: 代码合并

- sync: 同步主线或分支的Bug

- project: 项目配置如readme、changelog（不影响代码）

**scope**

scope用于说明PR影响的范围，比如交易、事务、资源、网络等等

**subject**

subject是PR目的的简短描述
