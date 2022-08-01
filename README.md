# Myosotis

[![OSCS](https://www.oscs1024.com/platform/badge/yhzdys/myosotis.svg?size=small)](https://www.murphysec.com/dr/NZ31XuI4v8zIJkCamQ)
![LICENSE](https://img.shields.io/github/license/yhzdys/myosotis)
![Maven](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fyhzdys%2Fmyosotis%2Fmaven-metadata.xml)
![CircleCI](https://img.shields.io/circleci/build/github/yhzdys/myosotis/main)
[![CodeFactor](https://www.codefactor.io/repository/github/yhzdys/myosotis/badge)](https://www.codefactor.io/repository/github/yhzdys/myosotis)
![Java](https://img.shields.io/badge/java-1.8%2B-green)
![Code Size](https://img.shields.io/github/languages/code-size/yhzdys/myosotis)

Myosotis（勿忘我)是基于java开发的一款轻量化、高性能的动态配置中心，适用于各种中小型系统  
客户端本不依赖任何框架，支持jdk8及以上的java环境，同时对spring和springboot有较好的支持

客户端特性：

- 轻量设计低占用
- 配置实时更新
- 本地快照备份
- Spring解耦

### 控制台样例

* [Console](http://myosotis.yhzdys.com/console/index.html)
* 用户名/密码:myosotis/123456

# Quick Start

## 客户端使用

### 最新版本

[Maven](https://repo1.maven.org/maven2/com/yhzdys/myosotis/)

### java

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
String configValue = client.getString("configKey");
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
myosotis.client.namespace=default
myosotis.server.address=http://127.0.0.1:7777
~~~

---

### java client

~~~java
MyosotisApplication application = new MyosotisApplication("http://127.0.0.1:7777");
MyosotisClient client = application.getClient("namespace");

String configValue = client.getString("configKey");
Long configValue = client.getLong("configKey");
Boolean configValue = client.getBoolean("configKey");
// 自定义ValueParser，支持lambda表达式
? configValue = client.get("configKey", (configValue) -> {...});
~~~

### annotation

~~~java
/**
 * 当client对象只有一个时，可不指定“namespace”属性，默认为该client下的namespace
 */
@Myosotis(namespace = "defalut")
public class Constant {

    /**
     * 默认匹配与属性名称一致的configKey
     */
    @MyosotisValue
    public static String configKey1;

    /**
     * 自动类型转换
     */
    @MyosotisValue
    public static User user;

    /**
     * 指定configKey
     */
    @MyosotisValue(configKey = "config_key2")
    public static Map<String, String> configKey2;

    /**
     * 指定namepsace下的configKey
     */
    @MyosotisValue(namespace = "default", configKey = "config_key2")
    public static Long configKey3;

    /**
     * 当配置为空时，手动指定默认值
     */
    @MyosotisValue(defaultValue = "[1,2,3]")
    public List<Long> configKey5;
}
~~~

## 客户端进阶使用

### 自定义

~~~java
Config config = new Config("http://myosotis-server.yhzdys.com");
// 自定义序列化协议，目前可支持JSON、AVRO、PROTOSTUFF(默认JSON)
config.serializeType(SerializeType.JSON);
// 开启本地快照保存(默认开启)
config.enableSnapshot(true);
// 开启数据压缩(默认开启)
config.enableCompress(true);
// 数据压缩阈值，当数据流长度大于设定值时对数据进行压缩处理(默认2048)
config.compressThreshold(2048L);
MyosotisApplication application = new MyosotisApplication(config);
~~~

#### 本地快照备份*

~~~
对最后一次获取到的服务端配置进行快照文件备份
使用config.enableSnapshotFile(true)开启本地快照备份功能(默认开启)
本地快照备份开启后，当服务端不可用时，会降级读取快照文件中的配置值，配置值为最后一次从服务端获取的有效配置
快照文件路径为：{user.home}/.myosotis/snapshot/{namespace}/{configKey}.snapshot
~~~

### 配置的变更&订阅

~~~
客户端提供两种事件订阅接口，分别为：
com.yhzdys.myosotis.event.listener.ConfigListener
com.yhzdys.myosotis.event.listener.NamespaceListener

ConfigListener提供单个命名空间下单个配置变动事件的订阅能力
NamespaceListener提供单个命名空间下所有配置变动事件的订阅能力(若命名空间下存在较多配置，不建议使用)
~~~

> 注意：客户端虽提供了一套完整的事件发布与订阅机制，但请勿将其当成MQ来使用，务必让工具干自己该干的事

#### ConfigListener

~~~java
import com.yhzdys.myosotis.entity.MyosotisEvent;
import com.yhzdys.myosotis.event.listener.ConfigListener;

/**
 * 自定义你的配置监听器
 */
public class YourConfigListener implements ConfigListener {

    public String namespace() {
        return "返回你需要订阅的命名空间";
    }
    
    public String configKey() {
        return "返回你所订阅的命名空间下的配置项";
    }
    
    public void handle(MyosotisEvent event) {
        // TODO 开始处理配置事件
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
        return "返回你需要订阅的命名空间";
    }

    public void handle(MyosotisEvent event) {
        // TODO 开始处理配置事件
    }
}

// 添加监听器到application
application.addNamespaceListener(new YourNamespaceListener());
~~~

---

# 运维手册

## 最新版本

### 下载

[Releases](https://github.com/yhzdys/myosotis/releases)

### 自助打包

~~~shell
git clone https://github.com/yhzdys/myosotis.git
cd myosotis
mvn clean package -Dmaven.test.skip
cd target
~~~

在target目录下，获得myosotis-{version}.zip和myosotis-{version}.tar.gz
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

### 关闭

~~~shell
./bin/shutdown.sh -c # 只关闭控制台
./bin/shutdown.sh -s # 只关闭服务端
./bin/shutdown.sh -a # 同时关闭控制台和服务端
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
| myosotis.mysql.username | mysql用户名                   |       | root                      |
| myosotis.mysql.password | mysql密码                    |       | root                      |

### console.conf

| 参数                    | 释义         | 默认值   | 参考值               |
|-----------------------|------------|-------|-------------------|
| myosotis.log.dir      | 日志目录（绝对路径） | ./log | /var/log/myosotis |
| myosotis.console.port | 内置tomcat端口 | 7776  | 7776              |

### server.conf

| 参数                                | 释义              | 默认值    | 参考值               |
|-----------------------------------|-----------------|--------|-------------------|
| myosotis.log.dir                  | 日志目录（绝对路径）      | ./log  | /var/log/myosotis |
| myosotis.server.port              | 内置tomcat端口      | 7777   |                   |
| myosotis.server.minThreads        | 内置tomcat最小线程数   | cpu核心数 |                   |
| myosotis.server.maxThreads        | 内置tomcat最大线程数   | 512    |                   |
| myosotis.server.connectionTimeout | 建立连接超时时间        | 2000   |                   |
| myosotis.server.maxConnections    | 最大连接数           | 1024   |                   |
| myosotis.server.keepAliveRequests | 最大keep-alive连接数 | 512    |                   |
| myosotis.server.acceptCount       | 最大等待连接数         | 8      |                   |
| myosotis.server.enableCompress    | 启用数据压缩          | true   | true              |
| myosotis.server.compressThreshold | 数据压缩阈值          | 2048   | 2048              |

---