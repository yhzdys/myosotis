# Myosotis

![GitHub last commit](https://img.shields.io/github/last-commit/yhzdys/myosotis) ![JAVA](https://img.shields.io/badge/JAVA-1.8+-green.svg) ![GitHub](https://img.shields.io/github/license/yhzdys/myosotis) ![GitHub language count](https://img.shields.io/github/languages/count/yhzdys/myosotis) ![GitHub top language](https://img.shields.io/github/languages/top/yhzdys/myosotis) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/yhzdys/myosotis) ![GitHub repo size](https://img.shields.io/github/repo-size/yhzdys/myosotis)

Myosotis（勿忘我)是一款基于java的高性能、轻量化的动态配置中心，资源开销较小，适用于各种中小型系统
客户端本可不依赖任何框架，支持jdk8及以上的java环境，同时对spring和springboot有较好的支持

客户端特性：

- 低资源占用
- 本地文件调试
- 本地快照备份
- 远程配置实时更新

### 控制台样例

http://myosotis.yhzdys.com

用户名/密码 myosotis/123456

# Quick Start

## 客户端使用

### 原生java

pom.xml添加client依赖

~~~xml
<dependency>
    <groupId>com.yhzdys</groupId>
    <artifactId>myosotis-client</artifactId>
    <version>1.0.0</version>
</dependency>
~~~

~~~java
// clientManager必须为单例
MyosotisClientManager clientManager = new MyosotisClientManager("http://127.0.0.1:7777");
MyosotisClient client = clientManager.getClient("namespace");
String configValue = client.getConfig("configKey");
~~~

### springframework

pom.xml添加spring依赖

~~~xml
<dependency>
    <groupId>com.yhzdys</groupId>
    <artifactId>myosotis-spring</artifactId>
    <version>1.0.0</version>
</dependency>
~~~

~~~java
@Bean
public MyosotisClientManager myosotisClientManager() {
    return new MyosotisClientManager("http://127.0.0.1:7777");
}

@Bean
public MyosotisClient myosotisClient(MyosotisClientManager clientManager) {
    return clientManager.getClient("namespace");
}
~~~

### springboot

pom.xml添加starter

~~~xml
<dependency>
    <groupId>com.yhzdys</groupId>
    <artifactId>myosotis-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
~~~

application.properties(yml)添加配置

~~~properties
myosotis.server.address=http://127.0.0.1:7777
myosotis.client.namespace=default
~~~

---

### 原生java客户端

~~~java
String configValue = myosotisClient.getConfig("configKey");
~~~

### spring+annotation

~~~java
@Myosotis
public class Constant {

    @MyosotisValue
    public static String configKey1;

    @MyosotisValue
    public static User user;

    @MyosotisValue(configKey = "config_key2")
    public static Map<String, String> configKey2;

    @MyosotisValue(namespace = "default", configKey = "config_key2")
    public static Long configKey3;

    @MyosotisValue(defaultValue = "[1,2,3]")
    public List<Long> configKey5;
}
~~~

## 客户端进阶使用

### 自定义

~~~java
MyosotisCustomizer customizer = new MyosotisCustomizer("http://myosotis-server.yhzdys.com");
// 自定义序列化协议，目前可支持JSON、AVRO、PROTOSTUFF(默认JSON)
customizer.serializeType(SerializeType.PROTOSTUFF);
// 开启本地文件调试(默认开启)
customizer.enableNative(true);
// 开启本地快照保存(默认开启)
customizer.enableSnapshot(true);
// 开启数据压缩(默认开启)
customizer.enableCompress(true);
// 数据压缩阈值，当数据流长度大于该值时使用LZ4压缩算法对数据进行压缩处理(默认2048)
customizer.compressThreshold(4096);
MyosotisClientManager clientManager = new MyosotisClientManager(customizer);
~~~

#### 本地文件调试

~~~
使用customizer.setEnableLocalFile(true)开启本地文件调试功能(默认开启)
本地调试开启后，可针对指定配置项进行本地配置，客户端会优先加载本地配置，适用于本地调试或功能灰度等场景
默认读取文件路径为：{user.home}/.myosotis/{namespace}/{configKey}

{configKey}文本内容使用JSON格式存储，程序会优先读取文件中的文本内容作为配置值优先加载到客户端
~~~

#### 本地快照备份

~~~
对最后一次获取到的服务端配置进行快照文件备份
使用customizer.setEnableSnapshotFile(true)开启本地快照备份功能(默认开启)
本地快照备份开启后，当服务端不可用时，会降级读取快照文件中的配置值，配置值为最后一次从服务端获取的有效配置
默认读取文件路径为：{user.home}/.myosotis/snapshot/{namespace}/{configKey}.snapshot

{configKey}.snapshot文本内容使用JSON格式存储，程序会在服务端不可用时读取文件中的文本内容作为配置值优先加载到客户端
~~~

#### 配置加载优先级

~~~
从高到低：本地配置 > 服务端配置 > 配置快照
~~~

### 客户端事件订阅

~~~
客户端提供两种事件订阅接口，分别为：
com.yhzdys.myosotis.event.listener.ConfigListener
com.yhzdys.myosotis.event.listener.NamespaceListener

ConfigListener提供单个命名空间下单个配置变动事件的订阅能力
NamespaceListener提供单个命名空间下所有配置变动事件的订阅能力(若命名空间下存在较多配置，不建议开启)
~~~

> 注意：Myosotis虽然提供了一套较为完善的事件发布与订阅机制，但请勿将其当成MQ来使用，务必让工具干自己该干的事

#### ConfigListener

~~~java
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;

/**
 * 自定义你的配置监听器
 */
public class YourConfigListener implements ConfigListener {

    public String namespace() {
        // 返回你需要订阅的命名空间
        return "default_namespace";
    }
    
    public String configKey() {
        // 返回你所订阅的命名空间下的配置项
        return "your_config_key";
    }
    
    public void handle(MyosotisEvent event) {
        // 开始处理配置事件
    }
}

// 添加监听器到clientManager
clientManager.addConfigListener(new YourConfigListener());
~~~

#### NamespaceListener

~~~java
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.NamespaceListener;

/**
 * 自定义你的命名空间监听器
 */
public class YourNamespaceListener implements NamespaceListener {

    public String namespace() {
        // 返回你需要订阅的命名空间
        return "default_namespace";
    }

    public void handle(MyosotisEvent event) {
        // 开始处理配置事件
    }
}

// 添加监听器到clientManager
clientManager.addNamespaceListener(new YourNamespaceListener());
~~~

---

# 运维手册

## 最新版本打包

进入根目录

~~~shell
mvn clean package -Dmaven.test.skip
~~~

在/target目录下，获得myosotis-*.zip和myosotis-*.tar.gz，任选其一
解压后的目录结构：

~~~
|bin
  |- startup.sh                   服务启动脚本
  |- shutdown.sh                  服务下线脚本
|application
  |- myosotis-console.jar         控制台应用
  |- myosotis-server.jar          服务端应用
|config
  |- cluster.conf                 集群配置
  |- datasource.conf              数据源配置
  |- console.conf                 控制台配置
  |- server.conf                  服务端配置
|database
  |- myosotis.db                  内置数据库(sqlite3)
|support
  |- mysql.sql                    MySQL DDL
  |- nginx.conf                   负载均衡配置参考
|README.md                        readme
~~~

## 服务启停

### 启动

~~~shell
./bin/startup.sh -c # 仅启动控制台
./bin/startup.sh -s # 仅启动服务端
./bin/startup.sh -a # 同时启动服务端和控制台

# 控制台访问 http://ip:port/console/index.html
# JVM参数可在startup.sh中调整，变量名为：CONSOLE_JAVA_OPT、SERVER_JAVA_OPT
~~~

### 关停

~~~shell
./bin/shutdown.sh
~~~

## 配置文件

### cluster.conf

~~~
## 以一行为最小单位，填写节点的ip+port
## eg: 
192.168.1.1:7777
192.168.1.2:7777
192.168.1.3:8080
~~~

### datasource.conf

| 参数                      | 释义                    | 默认值   | 参考值                       |
|-------------------------|-----------------------|-------|---------------------------|
| myosotis.sqlite3.path   | sqlite3文件，推荐仅在本地部署时使用 | 内置数据库 | /opt/myosotis/myosotis.db |
| myosotis.mysql.url      | mysql数据库url           |       | mysql.com:3306/myosotis   |
| myosotis.mysql.username | mysql用户名              |       |                           |
| myosotis.mysql.password | mysql密码               |       |                           |

### console.conf

| 参数                    | 释义              | 默认值   | 参考值  |
|-----------------------|-----------------|-------|------|
| myosotis.log.dir      | 日志路径            | ./log |      |
| myosotis.console.port | 内置tomcat端口      | 7776  | 7776 |

### server.conf

| 参数                                | 释义              | 默认值    | 参考值  |
|-----------------------------------|-----------------|--------|------|
| myosotis.log.dir                  | 日志路径            | ./log  |      |
| myosotis.server.port              | 内置tomcat端口      | 7777   | 7777 |
| myosotis.server.minThreads        | 内置tomcat最小线程数   | cpu核心数 | 8    |
| myosotis.server.maxThreads        | 内置tomcat最大线程数   | 1024   | 32   |
| myosotis.server.connectionTimeout | 建立连接超时时间        | 2000   | 2000 |
| myosotis.server.maxConnections    | 最大连接数           | 1024   | 32   |
| myosotis.server.keepAliveRequests | 最大keep-alive连接数 | 1024   | 32   |
| myosotis.server.acceptCount       | 最大等待连接数         | 8      | 8    |
| myosotis.server.enableCompress    | 启用数据压缩          | true   | true |
| myosotis.server.compressThreshold | 数据压缩阈值          | 2048   | 2048 |

---