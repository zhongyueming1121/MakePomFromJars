# MakePomFromJars
重构老项目时，将非maven项目中lib下的jar自动转换成pom
[MakePomFromJars](https://github.com/zhongyueming1121/MakePomFromJars "MakePomFromJars")

项目从非maven升级到maven项目，lib下一堆jar包需要整理，非常痛苦，
这个工具可以非常方便的转换大多数jar为pom依赖，
剩下的工作就是慢慢解决冲突了 :)

2019/12/4 增加了版本号抽出到properties标签下统一管理
2021/1/15 增加一个不抽离版本号的方法，实际使用发现抽离版本号不好用
```java
    //需生成pom文件的 lib路径
    private static final String libPath = "E:/MakePomFromJar/src/main/resources/lib";
    // 输出文件 
    private static final String outFilePath = "E:/output.txt";
```
输出

```shell
.................................................
..................................................
..................................................
...............

---------------fail jar---------------------
aspectjrt-1.8.13.jar
aspectjweaver.jar
aspectjweaver-1.8.13.jar
--------------------------------------------


---------------not find groupId jar---------------------
bcmail-jdk15on-147.jar
bcpkix-jdk15on-147.jar
bcprov-ext-jdk15on-147.jar
bcprov-jdk15on-147.jar
--------------------------------------------
total jar:164
success jar:158 --> not find groupId:4
fail jar:3

Process finished with exit code 0
```

输出的依赖文件

```
<?xml version="1.0" encoding="utf-8"?>

<project>
  <properties>
    <dom4j.version>1.6.1</dom4j.version>
    <druid.version>1.1.5</druid.version>
    <elasticsearch.version>5.5.1</elasticsearch.version>
    <esapi.version>2.2.0.0</esapi.version>
    <ezmorph.version>1.0.6</ezmorph.version>
    <fastjson.version>1.2.58.sec06</fastjson.version>
  </properties>
  <dependencys>
    <dependency>
      <groupId>org.lucee</groupId>
      <artifactId>dom4j</artifactId>
      <version>${dom4j.version}</version>
    </dependency>
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid</artifactId>
      <version>${druid.version}</version>
    </dependency>
    <dependency>
      <groupId>org.elasticsearch.distribution.zip</groupId>
      <artifactId>elasticsearch</artifactId>
      <version>${elasticsearch.version}</version>
    </dependency>
    <dependency>
      <groupId>org.owasp.esapi</groupId>
      <artifactId>esapi</artifactId>
      <version>${esapi.version}</version>
    </dependency>
    <dependency>
      <groupId>net.sf.ezmorph</groupId>
      <artifactId>ezmorph</artifactId>
      <version>${ezmorph.version}</version>
    </dependency>
    <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>fastjson</artifactId>
      <version>${fastjson.version}</version>
    </dependency>
  </dependencys>
</project>
```
