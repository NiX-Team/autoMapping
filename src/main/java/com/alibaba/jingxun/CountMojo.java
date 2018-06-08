package com.alibaba.jingxun;

import freemaker.util.log.LogKit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Map;

@Mojo(name = "auto-mapping")//目标名称，必须的
public class CountMojo extends AbstractMojo {

    //配置变量，在插件的configuration元素下配置变量名
    //配置文件路径（相对路径或者绝对路径）
    @Parameter
    private String name = "./auto-mapping.xml";
    @Parameter
    private String ftl = null;
    @Parameter
    public String path;
    public static String rootPath;
    /**
     * 目标执行的方法
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AutoHandler autoHandler = new AutoHandler(ConfigurationFileHandler.getConfiguraFile(name),ConfigurationFileHandler.configurationFileType,ftl);
        rootPath = path;
        try {
           LogKit.info("rootPath:" + rootPath);
            autoHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
            LogKit.error(e.getMessage(),e);
            throw new MojoExecutionException(e.getMessage());
        } finally {

        }
    }

}