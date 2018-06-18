package com.alibaba.jingxun;

import freemaker.util.log.LogKit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.util.Map;

@Mojo(name = "auto-mapping")//目标名称，必须的
public class CountMojo extends AbstractMojo {

    //配置变量，在插件的configuration元素下配置变量名
    //配置文件路径（相对路径或者绝对路径）
    @Parameter
    private String name = "./auto-mapping.xml";
    @Parameter
    private String ftl = null;
    public static String rootPath;
    /**
     * 目标执行的方法
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AutoHandler autoHandler = new AutoHandler(ConfigurationFileHandler.getConfiguraFile(name),ConfigurationFileHandler.configurationFileType,ftl);
        rootPath = ((MavenProject)getPluginContext().get("project")).getFile().getParent();
        try {
           LogKit.info("rootPath:" + rootPath);
           printMap(getPluginContext());
            autoHandler.start();
        } catch (Exception e) {
            e.printStackTrace();
            LogKit.error(e.getMessage(),e);
            throw new MojoExecutionException(e.getMessage());
        } finally {

        }
    }

    private void printMap(Map map) {
        for (Object key:map.keySet()) {
            LogKit.info("key:" + key);
            if (map.get(key) instanceof Map) {
                printMap((Map) map.get(key));
            } else {
                LogKit.info("value:" + map.get(key));
            }
        }
    }

}