package com.hospital.server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import java.io.File;
/**
 * Embedded Apache Tomcat Runner.
 * Allows running the entire web application directly using:
 *   mvn compile exec:java
 * Without needing external Tomcat installation or configuration.
 */
public class TomcatServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.trim().isEmpty()) {
            port = Integer.parseInt(portEnv.trim());
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Initializes the default HTTP connector

        File webappDir = new File("src/main/webapp");
        if (!webappDir.exists()) {
            // In case executed from backend subfolder or root folder
            webappDir = new File("backend/src/main/webapp");
        }

        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webappDir.getAbsolutePath());
        ctx.setParentClassLoader(TomcatServer.class.getClassLoader());

        // Map target/classes to /WEB-INF/classes so servlets and compiled classes are found
        File additionWebInfClasses = new File("target/classes");
        if (!additionWebInfClasses.exists()) {
            additionWebInfClasses = new File("backend/target/classes");
        }
        WebResourceRoot resources = new StandardRoot(ctx);
        if (additionWebInfClasses.exists()) {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(), "/"));
        }
        ctx.setResources(resources);

        System.out.println("=================================================");
        System.out.println(" Hospital Backend Server Started Successfully! ");
        System.out.println(" URL: http://localhost:" + port + "/");
        System.out.println("=================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}

