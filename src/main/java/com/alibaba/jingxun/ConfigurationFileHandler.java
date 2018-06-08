package com.alibaba.jingxun;

import freemaker.util.log.LogKit;

import java.io.File;

/**
 * @author Kiss
 * @date 2018/06/08 10:46
 * 配置文件管理
 */
public class ConfigurationFileHandler {
    public enum FileType{
        XML,
        YML,
        PROPERTIES,
        OTHER
    }
    public static FileType configurationFileType;
    /**
     * 判断配置文件文件名的合法性并返回文件格式
     * */
    private static FileType getConfigurationFileType(String fileName) {
        if (fileName.matches(".*\\.xml")) {
            return FileType.XML;
        } else if (fileName.matches(".*\\.yml")) {
            return FileType.YML;
        } else if (fileName.matches(".*\\.properties")) {
            return FileType.PROPERTIES;
        } else {
            return FileType.OTHER;
        }
    }

    /**
     * 获取配置文件
     * */
    public static File getConfiguraFile(String inputFile) throws IllegalArgumentException{
        LogKit.info("配置文件输入路径：" + inputFile);
        configurationFileType = getConfigurationFileType(inputFile);
        if (configurationFileType.equals(FileType.OTHER)) {
            throw new IllegalArgumentException("配置文件后缀不合法 目前仅支持(xml,yml,properties)类型配置文件");
        }
        LogKit.info("配置文件类型校验通过");
        File configurationFile =  new File(inputFile);
        if (!configurationFile.exists()) {
            throw new IllegalArgumentException("配置文件不存在:" + configurationFile);
        }
        if (!configurationFile.isFile()) {
            throw new IllegalArgumentException("配置文件不合法 不属于普通文件");
        }
        LogKit.info("配置文件详情校验通过");
        LogKit.info("配置文件准确路径：" + configurationFile.toString());
        return configurationFile;
    }
}
