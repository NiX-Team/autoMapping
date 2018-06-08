package com.alibaba.jingxun;

import javax.jws.WebMethod;
import javax.management.MXBean;

/**
 * @author Kiss
 * @date 2018/06/08 12:43
 */

public class Model {
    private Class clazz;
    private String mappingFileName;
    private String table;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getMappingFileName() {
        return mappingFileName;
    }

    public void setMappingFileName(String mappingFileName) {
        this.mappingFileName = mappingFileName;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "Model{" +
                "clazz=" + clazz +
                ", mappingFileName='" + mappingFileName + '\'' +
                ", table='" + table + '\'' +
                '}';
    }
}
