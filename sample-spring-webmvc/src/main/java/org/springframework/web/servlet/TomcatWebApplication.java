package org.springframework.web.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public class TomcatWebApplication {

    private static final String PATH = "sample-spring-webmvc/";
    private static final String WEBAPP_PATH  = PATH + "src/main/webapp";
    private static final String CLASSES_PATH = PATH + "build/classes";
    private static final int    PORT         = 8080;
    private static final String WORK_PATH    = PATH + "build/tomcat." + PORT;

    public static void main(String[] args) throws LifecycleException {
        // 1. 创建 Tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
        tomcat.setBaseDir(new File(WORK_PATH).getAbsolutePath());

        // 2. 配置连接器参数
        Connector connector = tomcat.getConnector();
        connector.setURIEncoding("UTF-8");
        connector.setProperty("connectionTimeout", "20000");
        connector.setProperty("maxThreads", "200");

        // 3. 静态资源目录，创建上下文
        Context ctx = tomcat.addWebapp("",
                new File(WEBAPP_PATH).getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(
                new DirResourceSet(resources, "/WEB-INF/classes",
                new File(CLASSES_PATH).getAbsolutePath(), "/")
        );
        ctx.setResources(resources);

        //  4. 启动 Tomcat
        tomcat.start();
        tomcat.getServer().await();
    }
}
