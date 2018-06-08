package com.alibaba.jingxun;
import freemaker.util.FreemarkerRoot;
import freemaker.util.log.LogKit;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kiss
 * @date 2018/06/08 13:03
 */
public class XmlConfigParse extends ConfigParse{
    private static DocumentBuilderFactory dbFactory = null;
    private static DocumentBuilder db = null;
    private final String _package = "package";
    private final String packageName = "name";
    private final String packageMappingPath = "path";
    private final String modelNodeName = "model";
    private final String modelNodeClazz = "clazz";
    private final String modelNodeMapping = "mapping";
    private final String modelNodeTable = "table";
    private List<Model> models = new ArrayList<Model>();
    static{
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
    @Override
    public List<Model> parse(File file) throws Exception {
        Document document = db.parse(file);
        //根据包扫描
        scanPackages(document);
        //根据配置的model扫描
        scanModel(document);
        return models;
    }

    private void scanModel(Document document) {
        NodeList modelNodes = document.getElementsByTagName(modelNodeName);
        for (int i = 0;i < modelNodes.getLength();i ++) {
            NodeList nodes = modelNodes.item(i).getChildNodes();
            Class clazz = null;
            String mappingPath = null;
            String table = null;
            for (int j = 0;j < nodes.getLength();j ++) {
                Node node = nodes.item(j);
                if (modelNodeClazz.equals(node.getNodeName())) {
                    try {
                        clazz = fileToClass(node.getFirstChild().getNodeValue());
                        LogKit.info("clazz:" + clazz);
                        if (mappingPath != null) {
                            if (!mappingPath.matches(".*\\.xml")) {
                                if (clazz != null) {
                                    mappingPath += ("/" + clazz.getSimpleName() + ".xml");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("clazz的值" + node.getFirstChild().getNodeValue() + "不是正确的类路径");
                    }
                } else if (modelNodeMapping.equals(node.getNodeName())) {
                    mappingPath = node.getFirstChild().getNodeValue();
                    if (!mappingPath.matches(".*\\.xml")) {
                        if (clazz != null) {
                            mappingPath += ("/" + clazz.getSimpleName() + ".xml");
                        }
                    }
                } else if (modelNodeTable.equals(node.getNodeName())) {
                    table = node.getFirstChild().getNodeValue();
                }
            }
            if (clazz == null) {
                throw new RuntimeException(modelNodeName + "节点没有配置" + modelNodeClazz + "节点或者配置信息错误");
            }
            if (mappingPath == null || mappingPath.isEmpty()) {
                throw new RuntimeException(modelNodeName + "节点没有配置" + modelNodeMapping + "节点或者配置信息错误");
            }
            if (table == null || table.isEmpty()) {
                table = FreemarkerRoot.getColumnName(clazz.getSimpleName());
            }
            Model model = new Model();
            model.setClazz(clazz);
            model.setMappingFileName(mappingPath);
            model.setTable(table);
            models.add(model);
        }
    }

    private void scanPackages(Document document) {
        NodeList packageList = document.getElementsByTagName(_package);
        if (packageList != null) {
            for (int i = 0;i < packageList.getLength();i ++) {
                NodeList nodes = packageList.item(i).getChildNodes();
                String name = null;
                String path = null;
                for (int j = 0;j < nodes.getLength();j ++) {
                    Node node = nodes.item(j);
                    if (packageName.equals(node.getNodeName())) {
                        name = node.getFirstChild().getNodeValue();
                    } else if (packageMappingPath.equals(node.getNodeName())) {
                        path = node.getFirstChild().getNodeValue();
                    }
                }
                scanPackage(name,path);
            }
        }
    }

    private void scanPackage(String pack,String mappingPath) {
        if (pack == null) {
            throw new RuntimeException("<package></package>的值不能为空");
        }
        if (mappingPath == null) {
            mappingPath = "./src/main/java/" + pack.replaceAll("\\.","/");
        }
        List<Class<?>> classes = getClasses(pack);
        for (Class clazz:classes) {
            Model model = new Model();
            model.setClazz(clazz);
            model.setMappingFileName(mappingPath + "./" + clazz.getSimpleName() + ".xml");
            model.setTable(FreemarkerRoot.getColumnName(clazz.getSimpleName()));
            models.add(model);
        }

    }

}
