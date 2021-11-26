# `AMS`实战

[TOC]

## 一、理论

### 1.1 `AMS`调起服务流程

`AMS`(`ActivityManagerService`)是`Android`的核心服务，它主要负责四大组件的启动、切换、调度以及应用进程的管理工作，`AMS`掌握了所有应用程序的创建、管理，所以它在`frameworks`层有着核心的作用。

![微信截图_20211116205138](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202111162052007.png)

#### 1.1.1 `AMS`源码低版本和高版本的差异

> 1. 低版本中是可以找到`IActivityManager`类的（`Google`工程师全部自己手写了一套类似于`AIDL`的代码来完成`Binder`的通讯）；
> 2. 高版本中是找不到`IActivityManager`类的（直接通过`AIDL`生成的，来完成`Binder`的通讯）。



### 1.2  跳转到`LoginActivity`流程图

![image-20211118205228667](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202111182052902.png)

从源码角度流程分析：

![image-20211119210130990](https://gitee.com/tianyalusty/pic-go-repository/raw/master/img/202111192101123.png)

## 二、实战

本文实现了`Android5.0-9.0`版本的通过代理`ProxyActivity`绕过`AMS`检测实现跳转到未注册的`Activity`的功能。
