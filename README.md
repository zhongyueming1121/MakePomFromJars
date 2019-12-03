# MakePomFromJars
重构老项目时，将非maven项目中lib下的jar自动转换成pom

项目从非maven升级到maven项目,lib下一堆jar包需要整理，非常痛苦，
这个工具可以非常方便的转换大多数jar为pom依赖

`
    //需生成pom文件的 lib路径
    private static final String libPath = "E:/MakePomFromJar/src/main/resources/lib";
    // 输出文件 
    private static final String outFilePath = "E:/output.txt";
`
**输出**

`
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
`
