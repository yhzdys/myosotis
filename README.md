# Myosotis

[![OSCS](https://www.oscs1024.com/platform/badge/yhzdys/myosotis.svg?size=small)](https://www.murphysec.com/dr/9vRqDqkgkn3BnIjrR4)
![LICENSE](https://img.shields.io/github/license/yhzdys/myosotis)
![CircleCI](https://img.shields.io/circleci/build/github/yhzdys/myosotis/main)
[![CodeFactor](https://www.codefactor.io/repository/github/yhzdys/myosotis/badge)](https://www.codefactor.io/repository/github/yhzdys/myosotis)
![JAVA](https://img.shields.io/badge/JAVA-1.8+-green.svg)
![Last Commit](https://img.shields.io/github/last-commit/yhzdys/myosotis)
![Code Size](https://img.shields.io/github/languages/code-size/yhzdys/myosotis)

Myosotis（勿忘我)是基于java开发的一款轻量化、高性能的动态配置中心，适用于各种中小型系统  
客户端本不依赖任何框架，支持jdk8及以上的java环境，同时对spring和springboot有较好的支持

客户端特性：

- 轻量化设计、低占用
- 远程配置实时更新
- 本地快照备份
- Spring解耦

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
    <version>${myosotis.version}</version>
</dependency>
~~~

~~~java
// MyosotisApplication 需要设置为单例
MyosotisApplication application = new MyosotisApplication("http://127.0.0.1:7777");
MyosotisClient client = application.getClient("namespace");
String configValue = client.getConfig("configKey");
~~~

### springframework

pom.xml添加spring依赖

~~~xml
<dependency>
    <groupId>com.yhzdys</groupId>
    <artifactId>myosotis-spring</artifactId>
    <version>${myosotis.version}</version>
</dependency>
~~~

~~~java
@Bean
public MyosotisApplication myosotisApplication() {
    return new MyosotisApplication("http://127.0.0.1:7777");
}

@Bean
public MyosotisClient myosotisClient(MyosotisApplication application) {
    return application.getClient("namespace");
}

/**
 * 开启注解配置支持
 */
@Bean
public MyosotisValueAutoConfiguration myosotisValueAutoConfiguration() {
    return new MyosotisValueAutoConfiguration();
}
~~~

### springboot

pom.xml添加starter

~~~xml
<dependency>
    <groupId>com.yhzdys</groupId>
    <artifactId>myosotis-spring-boot-starter</artifactId>
    <version>${myosotis.version}</version>
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
String configValue = myosotisClient.getString("configKey");
Long configValue = myosotisClient.getLong("configKey");
Boolean configValue = myosotisClient.getBoolean("configKey");
// 自定义ValueParser，支持lambda表达式
? configValue = client.get("configKey", (configValue) -> {...});
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
Config config = new Config("http://myosotis-server.yhzdys.com");
// 自定义序列化协议，目前可支持JSON、AVRO、PROTOSTUFF(默认JSON)
config.serializeType(SerializeType.PROTOSTUFF);
// 开启本地快照保存(默认开启)
config.enableSnapshot(true);
// 开启数据压缩(默认开启)
config.enableCompress(true);
// 数据压缩阈值，当数据流长度大于该值时使用LZ4压缩算法对数据进行压缩处理(默认2048)
config.compressThreshold(4096);
MyosotisApplication application = new MyosotisApplication(config);
~~~

#### 本地快照备份

~~~
对最后一次获取到的服务端配置进行快照文件备份
使用config.enableSnapshotFile(true)开启本地快照备份功能(默认开启)
本地快照备份开启后，当服务端不可用时，会降级读取快照文件中的配置值，配置值为最后一次从服务端获取的有效配置
默认读取文件路径为：{user.home}/.myosotis/snapshot/{namespace}/{configKey}.snapshot
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

// 添加监听器到application
application.addConfigListener(new YourConfigListener());
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

// 添加监听器到application
application.addNamespaceListener(new YourNamespaceListener());
~~~

---

# 运维手册

## 最新版本

### 下载

https://github.com/yhzdys/myosotis/releases

### 自助打包

~~~shell
git clone https://github.com/yhzdys/myosotis.git
cd myosotis
mvn clean package -Dmaven.test.skip
cd target
~~~

在target目录下，获得myosotis-*.zip和myosotis-*.tar.gz，任选其一
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

| 参数                      | 释义                         | 默认值   | 参考值                       |
|-------------------------|----------------------------|-------|---------------------------|
| myosotis.sqlite3.path   | sqlite3文件（绝对路径）推荐仅在本地部署时使用 | 内置数据库 | /opt/myosotis/myosotis.db |
| myosotis.mysql.url      | mysql数据库url                |       | mysql.com:3306/myosotis   |
| myosotis.mysql.username | mysql用户名                   |       |                           |
| myosotis.mysql.password | mysql密码                    |       |                           |

### console.conf

| 参数                    | 释义         | 默认值   | 参考值  |
|-----------------------|------------|-------|------|
| myosotis.log.dir      | 日志目录（绝对路径） | ./log |      |
| myosotis.console.port | 内置tomcat端口 | 7776  | 7776 |

### server.conf

| 参数                                | 释义              | 默认值    | 参考值  |
|-----------------------------------|-----------------|--------|------|
| myosotis.log.dir                  | 日志目录（绝对路径）            | ./log  |      |
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