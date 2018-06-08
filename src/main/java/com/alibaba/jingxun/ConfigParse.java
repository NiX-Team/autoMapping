package com.alibaba.jingxun;

import freemaker.util.log.LogKit;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kiss
 * @date 2018/06/08 13:02
 */
public abstract class ConfigParse {
    abstract List<Model> parse(File file) throws Exception;

    /**
     * 获取路径下的.java文件名  （类名）
     * */
    private List<String> getModelFileName(File dir){
        File[] dirFiles = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                // 接受dir目录
                boolean acceptDir = file.isDirectory();
                // 接受java文件
                boolean acceptClass = file.getName().endsWith(".class");
                return acceptDir || acceptClass;
            }
        });
        List<String> javaFileNames = new ArrayList<String>();
        for (File file:dirFiles){
            if (file.isFile()) {
                LogKit.info( "获取到文件" + file.getName());
                javaFileNames.add(file.getName());
            }
            if (file.isDirectory()) {
                javaFileNames.addAll(getModelFileName(file));
            }
        }
        return javaFileNames;
    }

    /**
     * 根据包名获取文件夹
     * */
    private File getPackageFileDir(String packName) {
        LogKit.info("根据包名获取文件夹");
        return new File(CountMojo.rootPath + "\\target\\classes\\" +  packName.replaceAll("\\.","/"));
    }

    /**
     * 根据类名获取packageName包下的所有类类型
     * */
    public List<Class<?>> getClasses(String packName){
        List<Class<?>> classes = new ArrayList<Class<?>>();
        List<String> classFileNames = getModelFileName(getPackageFileDir(packName));
        for (String fileName:classFileNames) {
            try {
                Class clazz = fileToClass(packName + "." + fileName.replace(".class",""));
                LogKit.info("clazz:" + clazz);
                classes.add(clazz);
                LogKit.info("获取到" + fileName.replaceAll(".class", "") + "的类类型");
            } catch (Exception e) {
                LogKit.error("获取类类型失败！！！",e);
                e.printStackTrace();
            }
        }
        return classes;
    }

    public Class fileToClass(String objectComName) throws Exception {
        LogKit.info("自定义列加载器");
        LogKit.info("object:" + objectComName);
        return IClassLoad.getClassLoader().loadClass(CountMojo.rootPath + "\\target\\classes\\",objectComName);
    }

}