## sand-iot开发笔记

推荐使用idea community来开发sand-iot模块。步骤如下：
1. 下载和安装idea community
2. 在idea中，从github检出[ofbiz-framework](https://github.com/apache/ofbiz-framework)、[sand-iot](https://github.com/langhua/sand-iot)到本地，比如

   1. Windows下：
    
   ```shell
   D:\git\ofbiz-framework
   D:\git\sand-iot
   D:\git\ofbiz-plugins
   ```
    
   2. Linux下：
    
   ```shell
   ~/git/ofbiz-framework
   ~/git/sand-iot
   ~/git/ofbiz-plugins
   ```
    
   说明：[ofbiz-plugins](https://github.com/apache/ofbiz-plugins)选装。


3. 在把sand-iot目录链接为ofbiz-framework/plugins/sand-iot
   1. Windows中，以系统管理员身份运行命令行终端：

   ```shell
   C:\Windows\System32>D:
   D:\>cd git\ofbiz-framework
   D:\git\ofbiz-framework>mklink /D plugins ..\ofbiz-plugins
   D:\git\ofbiz-framework>cd plugins
   D:\git\ofbiz-framework\plugins>mklink /D sand-iot ..\..\sand-iot
   ```

   2. Linux下：

   ```shell
   ~$cd ~/git/ofbiz-framework
   ~/git/ofbiz-framework$ln -s ../ofbiz-plugins plugins
   ~/git/ofbiz-framework$cd plugins
   ~/git/ofbiz-framework/plugins$ln -s ../../sand-iot sand-iot
   ```


4. 在Windows中使用idea和jdk 17编译ofbiz-framework可能出现的两个报错的处理：
   1. AuthHelper.java，这个修改方法，参考了[jakartaee #443](https://github.com/jakartaee/servlet/issues/443)中Mark Thomas的代码:
   ```java
    private static ClassLoader getContextClassLoader() {
        // return AccessController.doPrivileged(
        //        (PrivilegedAction<ClassLoader>) () -> {
                    ClassLoader cl = null;
                    try {
                        cl = Thread.currentThread().getContextClassLoader();
                    } catch (SecurityException e) {
                        Debug.logError(e, e.getMessage(), MODULE);
                    }
                    return cl;
        //        });
    }
   ```
   2. GroovyScriptTestCase.java:
   ```java
   public class GroovyScriptTestCase extends GroovyTestCase {
   ```
   把GroovyTestCase改为GroovyAssert即可：
   ```java
   public class GroovyScriptTestCase extends GroovyAssert {
   ```
