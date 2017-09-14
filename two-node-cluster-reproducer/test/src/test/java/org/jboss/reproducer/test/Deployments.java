/**
 *
 */
package org.jboss.reproducer.test;

import java.io.File;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author bmaxwell
 *
 */
public class Deployments {

    private static Logger log = Logger.getLogger(Deployments.class.getName());

    public static Archive<?> MDB_API = createApiLibrary("mdb", "org.jboss.reproducer.ejb.api.mdb");
    public static Archive<?> SLSB_API = createApiLibrary("slsb", "org.jboss.reproducer.ejb.api.slsb");

    public static Archive<?> createEjbSubDeployment(EJBInfo ejbInfo) {
        String outputName = ejbInfo.getEjbName() + ".jar";
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, outputName);
        // add ejb package
        if(ejbInfo.getEjbPackage() != null)
            jar.addPackage(ejbInfo.getEjbPackage());
        if(log.isDebugEnabled())
            outputTestDeployment(ejbInfo.getEjbPackage(), jar);
        return jar;
    }

    public static Archive<?> createApiLibrary(String name, String...packages) {
        String outputName = name + "-api.jar";
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, outputName);
        // add ejb api
        for(String Package : packages)
            jar.addPackage(Package);
        return jar;
    }


    public static Archive<?> createEjbApiLibrary(EJBInfo ejbInfo) {
        String outputName = ejbInfo.getEjbName() + "-api.jar";
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, outputName);
        // add ejb api
        if(ejbInfo.getEjbInterface().getPackage() != null)
            jar.addPackage(ejbInfo.getEjbInterface().getPackage());
        if(log.isDebugEnabled())
            outputTestDeployment(ejbInfo.getEjbPackage(), jar);
        return jar;
    }


    private static StringAsset JBOSS_WEB_XML = new StringAsset(
            "<?xml version=\"1.0\"?>\n<jboss-web>\n<security-domain>other</security-domain>\n</jboss-web>\n");

    private static StringAsset getWebXmlRoleRequired(String securityRoleRequired) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version='1.0' encoding='UTF-8'?>");
        sb.append(
                "<web-app version=\"3.0\" xmlns=\"http://java.sun.com/xml/ns/javaee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append(
                " xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\">");
        sb.append("<security-constraint>\n");
        sb.append("<web-resource-collection>\n");
        sb.append("<web-resource-name>All resources</web-resource-name>\n");
        sb.append("<url-pattern>/*</url-pattern>\n");
        sb.append("</web-resource-collection>\n");
        sb.append("<auth-constraint>\n");
        sb.append(String.format("<role-name>%s</role-name>\n", securityRoleRequired));
        sb.append("</auth-constraint>\n");
        sb.append("</security-constraint>\n");
        sb.append("<security-role>\n");
        sb.append(String.format("<role-name>%s</role-name>\n", securityRoleRequired));
        sb.append("</security-role>\n");
        sb.append("<login-config>\n");
        sb.append("<auth-method>BASIC</auth-method>\n");
        sb.append("<realm-name>Test Logon</realm-name>\n");
        sb.append("</login-config>\n");
        sb.append("</web-app>\n");
        return new StringAsset(sb.toString());
    }

    private static StringAsset getEjbClientXml(boolean excludeLocalReceivers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<jboss-ejb-client xmlns=\"urn:jboss:ejb-client:1.2\">");
        sb.append("<client-context>");
        sb.append("  <ejb-receivers exclude-local-receiver=\"true\">");
        // sb.append(" <ejb-receivers exclude-local-receiver=\"false\">");
        sb.append("    <remoting-ejb-receiver outbound-connection-ref=\"remote-ejb-connection\"/>");
        sb.append("  </ejb-receivers>");
        sb.append("</client-context>");
        sb.append("</jboss-ejb-client>");
        return new StringAsset(sb.toString());
    }

    public static Archive<?> createWarDeployment(String suffix, boolean addJBossEJBClientXml, ServletInfo servletInfo) {
        String outputName = servletInfo.getServletSimpleName() + suffix + ".war";
        WebArchive war = ShrinkWrap.create(WebArchive.class, outputName);
        // add servlet package
        war.addPackage(servletInfo.getServletPackage());
        // add packages needed in the app such as ejb api
        for(Package p : servletInfo.getPackagesRequired())
            war.addPackage(p);

        war.addAsWebInfResource(JBOSS_WEB_XML, "jboss-web.xml");
        war.addAsWebInfResource(getWebXmlRoleRequired(servletInfo.getSecurityRole()), "web.xml");
        if (addJBossEJBClientXml)
            war.addAsWebInfResource(getEjbClientXml(false), "jboss-ejb-client.xml");

        String packageDir = servletInfo.getServletPackage().replaceAll("\\.", "/");
        File outputDir = new File(packageDir + "target/arquillian-deployments/");
        outputDir.mkdirs();
        if(log.isDebugEnabled())
            outputTestDeployment(servletInfo.getServletPackage(), war);
        return war;
    }

    public static Archive<?> createEarDeployment(String name, Archive<?>[] earLib, Archive<?>...subDeployments) {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, name + ".ear");

        ear.addAsLibraries(earLib);

        for(Archive<?> subDeployment : subDeployments)
            ear.addAsModule(subDeployment);

//        if(log.isDebugEnabled())
            outputTestDeployment("org.jboss.reproducer.test", ear);

        return ear;
    }

    public static void outputTestDeployment(String p, Archive archive) {
        String packageDir = p.replaceAll("\\.", "/");
        File outputDir = new File("target/arquillian-deployments/" + packageDir);
        outputDir.mkdirs();
        archive.as(ZipExporter.class).exportTo(new File(outputDir, archive.getName()), true);
    }

    public static void outputTestDeployment(Package p, Archive archive) {
        String packageDir = p.getName().replaceAll("\\.", "/");
        File outputDir = new File("target/arquillian-deployments/" + packageDir);
        outputDir.mkdirs();
        archive.as(ZipExporter.class).exportTo(new File(outputDir, archive.getName()), true);
    }
    public static void outputTestDeployment(Class testClass, Archive archive) {
        String packageDir = testClass.getPackage().getName().replaceAll("\\.", "/");
        File outputDir = new File("target/arquillian-deployments/" + packageDir);
        outputDir.mkdirs();
        archive.as(ZipExporter.class).exportTo(new File(outputDir, archive.getName()), true);
    }
}