package com.ymz.maven;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * 将非maven项目中的jar转换成pom
 *
 * @author ymz
 */
public class MakePomFromJars {
    /**
     * 需生成pom文件的 lib路径
     */
    private static final String libPath = "E:/MakePomFromJar/src/main/resources/lib";
    /**
     * 输出文件
     */
    private static final String outFilePath = "E:/output.txt";

    public static void main(String[] args) {
        List<String> failJar = new ArrayList<>();
        List<String> notFindGroupIdJar = new ArrayList<>();
        List<Map<String, String>> propertiesList = new ArrayList<>();
        Element project = new DOMElement("project");
        Element dependencys = new DOMElement("dependencys");
        Element properties = new DOMElement("properties");
        File dir = new File(libPath);
        File[] files = dir.listFiles();
        int i = 0;
        int success = 0;
        int notGroupId = 0;
        assert files != null;
        for (File jar : files) {
            try {
                i++;
                JarInputStream jis = new JarInputStream(new FileInputStream(jar));
                Manifest manifest = jis.getManifest();
                jis.close();
                String bundleName = manifest.getMainAttributes().getValue("Bundle-Name");
                String bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");
                Element ele = null;
                if (bundleName != null) {
                    bundleName = bundleName.toLowerCase().replace(" ", "-");
                    ele = getDecencies(bundleName, bundleVersion, propertiesList);
                }
                if (ele == null || ele.elements().size() == 0) {
                    bundleName = "";
                    bundleVersion = "";
                    String[] ns = jar.getName().replace(".jar", "").split("-");
                    for (String s : ns) {
                        if (Character.isDigit(s.charAt(0))) {
                            bundleVersion += s + "-";
                        } else {
                            bundleName += s + "-";
                        }
                    }
                    if (bundleVersion.endsWith("-")) {
                        bundleVersion = bundleVersion.substring(0, bundleVersion.length() - 1);
                    }
                    if (bundleName.endsWith("-")) {
                        bundleName = bundleName.substring(0, bundleName.length() - 1);
                    }
                    ele = getDecencies(bundleName, bundleVersion, propertiesList);
                }
                if (ele.elements().size() == 0) {
                    notGroupId++;
                    notFindGroupIdJar.add(jar.getName());
                    String artifactStr = bundleName.replaceAll("-", ".") + ".version";
                    HashMap<String, String> map = new HashMap<>(1);
                    map.put(artifactStr, bundleVersion);
                    propertiesList.add(map);
                    ele.add(new DOMElement("groupId").addText("NotFindGroupId"));
                    ele.add(new DOMElement("artifactId").addText("${" + artifactStr + "}"));
                    ele.add(new DOMElement("version").addText(bundleVersion));
                }
                dependencys.add(ele);
                success++;
            } catch (Exception e) {
                failJar.add(jar.getName());
            }
            if (i % 50 == 0) {
                System.out.println();
            }
            System.out.print(".");
        }
        propertiesList = propertiesList.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
        propertiesList.forEach(v -> v.forEach((key, val) -> {
            Element version = new DOMElement(key);
            version.addText(val);
            properties.add(version);
        }));
        if (!failJar.isEmpty()) {
            System.out.println();
            System.out.println();
            System.out.println("---------------fail jar---------------------");
            for (String s : failJar) {
                System.out.println(s);
            }
            System.out.println("--------------------------------------------");
        }
        if (!notFindGroupIdJar.isEmpty()) {
            System.out.println();
            System.out.println();
            System.out.println("---------------not find groupId jar---------------------");
            for (String s : notFindGroupIdJar) {
                System.out.println(s);
            }
            System.out.println("--------------------------------------------");
        }
        System.out.println();
        System.out.println("total jar:" + files.length);
        System.out.print("success jar:" + success);
        if (notGroupId > 0) {
            System.out.println(" --> not find groupId:" + notGroupId);
        } else {
            System.out.println();
        }
        System.out.println("fail jar:" + failJar.size());
        project.add(properties);
        project.add(dependencys);
        formatXml(project.asXML());
    }

    private static Element getDecencies(String key, String ver, List<Map<String, String>> propertiesList) {
        Element dependency = new DOMElement("dependency");
        try {
            String url = "http://search.maven.org/solrsearch/select?q=a%3A%22" + key + "%22%20AND%20v%3A%22" + ver + "%22&rows=3&wt=json";
            Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(30000).get();
            String elem = doc.body().text();
            JSONObject response = JSONObject.parseObject(elem).getJSONObject("response");
            if (response.containsKey("docs") && response.getJSONArray("docs").size() > 0) {
                JSONObject docJson = response.getJSONArray("docs").getJSONObject(0);
                Element groupId = new DOMElement("groupId");
                Element artifactId = new DOMElement("artifactId");
                Element version = new DOMElement("version");
                groupId.addText(docJson.getString("g"));
                artifactId.addText(docJson.getString("a"));
                String artifactStr = docJson.getString("a");
                artifactStr = artifactStr.replaceAll("-", ".") + ".version";
                HashMap<String, String> map = new HashMap<>(1);
                map.put(artifactStr, docJson.getString("v"));
                propertiesList.add(map);
                version.addText("${" + artifactStr + "}");
                dependency.add(groupId);
                dependency.add(artifactId);
                dependency.add(version);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dependency;
    }

    private static void formatXml(String xml) {
        try {
            SAXReader saxReader = new SAXReader();
            org.dom4j.Document document = saxReader.read(IOUtils.toInputStream(xml));
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf-8");
            Writer out = new FileWriter(outFilePath);
            XMLWriter xmlwriter = new XMLWriter(out, format);
            xmlwriter.write(document);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}