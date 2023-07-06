## MDNS开发笔记

sand-iot模块中，mdns的功能：
1. 在启动ofbiz后，生成一个sandflower._http._tcp.local域名。
2. 可以在本地安装的home-assistant中，通过mdns域名，将ofbiz以“网页卡片”方式加入仪表盘。

mdns功能基于[jmdns](https://github.com/jmdns/jmdns)实现。

