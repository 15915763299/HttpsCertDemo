# HttpsCertDemo
* 分析了HTTPS原理，包括SSL/TSL、加密技术、证书、OpenSSL的使用。
* 简单实现了Android端、后台（使用SpringBoot）的HTTPS单向验证、双向验证。
* 项目难点不在于编码，在于概念的理解，还有各种证书格式的转换。
* 如果要在自己电脑上运行，部分证书需要重新生成（访问地址被写到证书里了，会被验证）
* 具体内容请看 `客户端与服务器通讯使用HTTPS原理分析与实操.pdf` 文件。
* 没有写成.md文件，因为太长了:sweat_smile:，也不会太太太长，除去标题目录就16页:smirk:，如果有什么不妥的地方，欢迎指正。


以下为 `客户端与服务器通讯使用HTTPS原理分析与实操.pdf` 的目录：

    一、概述
    二、基础概念
       1.网络层次
       2.HTTP的缺陷
       3.HTTPS的优势及原理
       4.SSL/TSL原理
         (1).加密技术
         (2).身份验证
         (3).根证书与证书链
         (4).CA证书的使用流程
         (5).SSL/TSL简介
     三、实操9
       1.使用OpenSSL生成自己的CA证书
       2.使用SpringBoot搭建简单后台
       3.Android端使用HTTPS访问SpringBoot后台
         (1).构建OkhttpClient
         (2).请求与结果
       4.双向认证（拓展）
         (1).生成新证书
         (2).配置Android端
         (3).转换根证书格式
         (4).配置服务端
     四、其他问题
       1.关于双端证书
       2.关于客户端根证书的更新策略

---

这些原理中CA的最为重要，关于CA证书原理，截取文档中的两张图，看懂这两张图就够了：

> <img src="/docs/pic/ca0.png" height="340"></img>
    
> <img src="/docs/pic/ca1.png" height="340"></img>
