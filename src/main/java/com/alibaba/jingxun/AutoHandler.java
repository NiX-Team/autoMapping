package com.alibaba.jingxun;

import freemaker.XmlService;
import freemaker.util.FreemarkerRoot;
import freemaker.util.log.LogKit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Kiss
 * @date 2018/06/08 10:59
 * 自动生成管理
 */
public class AutoHandler {
    private final File configFile;
    private final ConfigurationFileHandler.FileType fileType;
    private final String ftl;
    private ConfigParse configParse;
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 20, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("auto-mapping");
            return t;
        }
    });

    private CountDownLatch countDownLatch = null;

    /**
     * 需要自动生成mapping.xml的对象列表
     * */
    private List<Model> models = new ArrayList<Model>();

    public AutoHandler(File configFile, ConfigurationFileHandler.FileType fileType,String ftl) {
        this.configFile = configFile;
        this.fileType = fileType;
        this.ftl = ftl;
    }

    public void start() throws Exception {
        switch (fileType) {
            case XML:configParse = new XmlConfigParse();break;
            case YML:configParse = new XmlConfigParse();break;
            case PROPERTIES:configParse = new XmlConfigParse();break;
            case OTHER:return;
            default:return;
        }
        models = configParse.parse(configFile);
        LogKit.info("需要自动生成的对象：");
        LogKit.info(Arrays.toString(models.toArray()));
        countDownLatch = new CountDownLatch(models.size());
        File ftlDir = new File("./.auto");
        File file = new File("./.auto/mapping_have_header.ftl");
        File file1 = new File("./.auto/mapping_no_header.ftl");
        if (!ftlDir.mkdir() || !file.createNewFile() || !file1.createNewFile()) {
            return;
        }
        writeFile(file,haveHeaderFtl);
        writeFile(file1,noHaveHeaderFtl);
        try {
            final FreemarkerRoot freemakerRoot = new FreemarkerRoot(ftl,ftlDir);
            for (Model model:models) {
                autoOneModel(model,freemakerRoot);
            }
            countDownLatch.await();
        }finally {
            file.delete();
            file1.delete();
            ftlDir.deleteOnExit();
        }
    }

    private void writeFile(File file,String content) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    /**
     * 自动生成一个对象的mapping文件
     * */
    private void autoOneModel(final Model model,final FreemarkerRoot freemakerRoot) {
        LogKit.info("开始自动生成mapping：" + model);
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    XmlService.startGenerateMappingXml(model.getClazz(), model.getMappingFileName(), model.getTable(), freemakerRoot);
                }catch (Exception e) {
                    LogKit.error(e.getMessage(),e);
                } finally {
                    countDownLatch.countDown();
                }
            }
        });
    }

    private final String haveHeaderFtl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "<mapper namespace=\"com.nix.cinema.dao.${oneself.table}Mapper\">\n" +
            "    <resultMap id=\"BaseResultMap\" type=\"${oneself.type}\">\n" +
            "    <#list oneself.params as param>\n" +
            "    <#if param.property == \"id\" >\n" +
            "        <id column=\"${param.column}\" property=\"${param.property}\" jdbcType=\"${param.jdbcType}\"/>\n" +
            "    <#else >\n" +
            "        <result column=\"${param.column}\" property=\"${param.property}\" jdbcType=\"${param.jdbcType}\"/>\n" +
            "    </#if>\n" +
            "    </#list>\n" +
            "    <#if others?has_content>\n" +
            "    <#list others as other>\n" +
            "    <#--<#if other.columnType == 1>-->\n" +
            "        <#--<association property=\"${other.name}\" javaType=\"${other.type}\"  column=\"id\"/>-->\n" +
            "    <#--</#if>-->\n" +
            "    <#--<#if other.columnType == 2>-->\n" +
            "         <#--<collection property=\"${other.name}\" ofType=\"${other.type}\" column=\"id\"/>-->\n" +
            "    <#--</#if>-->\n" +
            "    </#list>\n" +
            "    </#if>\n" +
            "    </resultMap>\n" +
            "    <insert id=\"insert\" parameterType=\"${oneself.type}\">\n" +
            "        insert into `${table}`\n" +
            "        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "            <if test=\"${param.property} != null\">\n" +
            "                `${param.column}`,\n" +
            "            </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "             <if test=\"${param.property} != null\">\n" +
            "                ${r\"#{\"}${param.column},jdbcType=${param.jdbcType}${r\"}\"},\n" +
            "             </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "    </insert>\n" +
            "    <delete id=\"delete\" parameterType=\"java.lang.Integer\">\n" +
            "        delete  from `${table}` where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </delete>\n" +
            "    <update id=\"update\" parameterType=\"${oneself.type}\">\n" +
            "        update `${table}`\n" +
            "        set\n" +
            "        <trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "            <if test=\"${param.property} != null\">\n" +
            "                `${param.column}` = ${r\"#{\"}${param.property},jdbcType=${param.jdbcType}${r\"}\"},\n" +
            "            </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "        where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </update>\n" +
            "    <select id=\"select\" parameterType=\"int\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>\n" +
            "\n" +
            "    <select id=\"maxId\" resultType=\"Integer\">\n" +
            "        select max(`id`) from `${table}`;\n" +
            "    </select>\n" +
            "\n" +
            "    <select id=\"count\" resultType=\"Long\">\n" +
            "        select count(`id`) from `${table}`;\n" +
            "    </select>\n" +
            "\n" +
            "    <select id=\"findByOneField\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where `@{field}` = ${r\"#{\"}value,jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>\n" +
            "\n" +
            "    <select id=\"list\" resultMap=\"BaseResultMap\">\n" +
            "        select `${table}`.* from @{tables}\n" +
            "        <if test=\"conditions != null\">\n" +
            "            where @{conditions}\n" +
            "        </if>\n" +
            "        <if test=\"order != null and sort != null\">\n" +
            "            order by @{order} @{sort}\n" +
            "        </if>\n" +
            "        <if test=\"offset != null\">\n" +
            "            limit ${r\"#{offset,jdbcType=INTEGER}\"},${r\"#{limit,jdbcType=INTEGER}\"}\n" +
            "        </if>\n" +
            "    </select>\n" +
            "\n" +
            "    <select id=\"selectLazy\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where id = ${r\"#{\"}${table},jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>\n" +
            "</mapper>";
    private final String noHaveHeaderFtl = "<resultMap id=\"BaseResultMap\" type=\"${oneself.type}\">\n" +
            "    <#list oneself.params as param>\n" +
            "        <#if param.property == \"id\" >\n" +
            "        <id column=\"${param.column}\" property=\"${param.property}\" jdbcType=\"${param.jdbcType}\"/>\n" +
            "        <#else >\n" +
            "        <result column=\"${param.column}\" property=\"${param.property}\" jdbcType=\"${param.jdbcType}\"/>\n" +
            "        </#if>\n" +
            "    </#list>\n" +
            "    <#if others?has_content>\n" +
            "        <#list others as other>\n" +
            "        <#--<#if other.columnType == 1>-->\n" +
            "        <#--<association property=\"${other.name}\" javaType=\"${other.type}\"  column=\"id\"/>-->\n" +
            "        <#--</#if>-->\n" +
            "        <#--<#if other.columnType == 2>-->\n" +
            "        <#--<collection property=\"${other.name}\" ofType=\"${other.type}\" column=\"id\"/>-->\n" +
            "        <#--</#if>-->\n" +
            "        </#list>\n" +
            "    </#if>\n" +
            "</resultMap>\n" +
            "    <insert id=\"insert\" parameterType=\"${oneself.type}\">\n" +
            "        insert into `${table}`\n" +
            "        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "            <if test=\"${param.property} != null\">\n" +
            "                `${param.column}`,\n" +
            "            </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "            <if test=\"${param.property} != null\">\n" +
            "                ${r\"#{\"}${param.column},jdbcType=${param.jdbcType}${r\"}\"},\n" +
            "            </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "    </insert>\n" +
            "    <delete id=\"delete\" parameterType=\"java.lang.Integer\">\n" +
            "        delete  from `${table}` where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </delete>\n" +
            "    <update id=\"update\" parameterType=\"${oneself.type}\">\n" +
            "        update `${table}`\n" +
            "        set\n" +
            "        <trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">\n" +
            "        <#list oneself.params as param>\n" +
            "            <if test=\"${param.property} != null\">\n" +
            "                `${param.column}` = ${r\"#{\"}${param.property},jdbcType=${param.jdbcType}${r\"}\"},\n" +
            "            </if>\n" +
            "        </#list>\n" +
            "        </trim>\n" +
            "        where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </update>\n" +
            "    <select id=\"select\" parameterType=\"int\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where id = ${r\"#{\"}id,jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>\n" +
            "     <select id=\"maxId\" resultType=\"Integer\">\n" +
            "         select max(`id`) from `${table}`;\n" +
            "     </select>\n" +
            "\n" +
            "     <select id=\"count\" resultType=\"Long\">\n" +
            "         select count(`id`) from `${table}`;\n" +
            "     </select>\n" +
            "    <select id=\"findByOneField\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where `@{field}` = ${r\"#{\"}value,jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>\n" +
            "    <select id=\"list\" resultMap=\"BaseResultMap\">\n" +
            "        select `${table}`.* from @{tables}\n" +
            "        <if test=\"conditions != null\">\n" +
            "            where @{conditions}\n" +
            "        </if>\n" +
            "        <if test=\"order != null and sort != null\">\n" +
            "            order by @{order} @{sort}\n" +
            "        </if>\n" +
            "        <if test=\"offset != null\">\n" +
            "            limit ${r\"#{offset,jdbcType=INTEGER}\"},${r\"#{limit,jdbcType=INTEGER}\"}\n" +
            "        </if>\n" +
            "    </select>\n" +
            "    <select id=\"selectLazy\" resultMap=\"BaseResultMap\">\n" +
            "        select * from `${table}` where id = ${r\"#{\"}${table},jdbcType=INTEGER${r\"}\"}\n" +
            "    </select>";
}
