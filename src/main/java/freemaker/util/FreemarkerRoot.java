package freemaker.util;
import com.alibaba.jingxun.CountMojo;
import freemaker.model.FreeParam;
import freemaker.model.Model;
import freemaker.model.Param;
import freemaker.util.log.LogKit;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import sun.rmi.runtime.Log;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by 11723 on 2017/5/5.
 */
public class FreemarkerRoot {
    private final static int NO_COLUMN = 0;
    //pojo存在一对一的关系
    private final static int ONE_ONT_ONE = 1;
    //pojo存在一对多的关系
    private final static int ONE_TWO_MONY = 2;
    //替换的xml文件关键字段  自动生成的代码会替换在原文件里的有该第一次字符串的地方
    private final String keyWord = "<!--ftl_contrnt-->";
    //需要修改的mapping.xml是否需要mapper节点之外的头部
    private final String ftl_have_header = "mapping_have_header.ftl";

    private final String ftl_no_header = "mapping_no_header.ftl";

    private final static Configuration CONFIGURATION = new Configuration();

    private File mappingFile;
    private File backMapping = new File("./mapping.xml");
    private File memberFtl = null;

    /**
     * 获取项目运行路径
     * 类实例化的加载freemaker工作目录并初始化
     *
     * */
    public FreemarkerRoot(String ftl, File ftlDir){
        try {
            if (ftl == null) {
                CONFIGURATION.setDirectoryForTemplateLoading(ftlDir);
            } else {
                memberFtl = new File(ftl);
                CONFIGURATION.setDirectoryForTemplateLoading(new File(memberFtl.getPath()));
            }
            CONFIGURATION.setObjectWrapper(new DefaultObjectWrapper());
            CONFIGURATION.setDefaultEncoding("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * 根据pojo实体类自动生成mybatis的mapping.xml的基本增删改查四个方法和pojo数据库映射map
     * <br />
     * 如果该pojo的mapping.xml映射文件已经存在 请在源文件里添加"<!--ftl_contrnt-->"字符串便于生成的内容替换进去
     * @param baseModelClazz 需要生成pojo的类类型
     *
     * */
    public void createMapperXml(Class baseModelClazz,String mappingFileName,String tableName) throws IOException, TemplateException {
        mappingFile = new File(mappingFileName);
        int type = xmlIsHaveKeyword(mappingFile);
        if (type == 0){
            LogKit.info(this.getClass(),"文件校验失败，退出代码生成");
            return;
        }
        LogKit.info(this.getClass(),"文件校验通过 开始代码生成");
        Template template = null;
        if (memberFtl != null) {
            template = CONFIGURATION.getTemplate(memberFtl.getName());
        } else {
            template = CONFIGURATION.getTemplate(type == 2 ? ftl_no_header : ftl_have_header);
        }

        Map<String, Object> paramMap = generateInsertContent(baseModelClazz,tableName);
        backMapping.createNewFile();
        Writer writer = new OutputStreamWriter(new FileOutputStream(backMapping),"UTF-8");
        template.process(paramMap, writer);
        changProjectXml(mappingFile);
        writer.close();
        backMapping.delete();

    }
    /**
     * 获取本次操作的pojo实体的字段映射关系
     * @return map
     * <br />
     *      map里包含键值
     *      <br/>
     *      model_name 本次操作实体的名称首字母大写<br/>
     *      oneself 本次操作实体字段类<br/>
     *      oneself 本次操作实体包含其他pojo的字段类list
     *
     * */
    private Map<String,Object> generateInsertContent(Class clazz,String tableName){
        Map<String,Object> map = new HashMap<String,Object>();
        FreeParam freeParam = getFreeParam(clazz,tableName);
        map.put("table",freeParam.getModel().getTable().toLowerCase());
        map.put("oneself",freeParam.getModel());
        map.put("others",freeParam.getModels());
        return map;
    }
    /**
     * 获取本次操作实体类的freeParam对象
     * @param type 需要操作的实体类的类类型
     * */
    private FreeParam getFreeParam(Class<?> type,String tableName) {
        FreeParam freeParam = new FreeParam();
        Model model = new Model();
        model.setTable(tableName);
        List<Param> paramList = new ArrayList<Param>();
        for (Field field:type.getDeclaredFields()) {
            JdbcType jdbcType = TypeUtil.getJdbcType(field.getType());
            if (jdbcType == null) {
                continue;
            }
            Param param = new Param(getColumnName(field.getName()),field.getName(),jdbcType.name());
            paramList.add(param);
        }
        model.setParams(paramList);
        model.setType(type.getName());
        freeParam.setModel(model);
        return freeParam;
    }

    /**
     * 根据驼峰命名返回数据库命名
     * */
    public static String getColumnName(String property) {
        char[] chars = property.toCharArray();
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = chars.length - 1;i > 0;i --) {
                if (chars[i] >= 'A' && chars[i] <= 'Z') {
                    if (chars[i - 1] < 'A' || chars[i - 1] > 'Z') {
                        builder.insert(0,chars[i - 1] + "_" + chars[i]);
                        i--;
                        continue;
                    }
                }
                builder.insert(0,chars[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(property + " 字段命名不合法 应该为驼峰命名");
        }
        builder.insert(0,chars[0]);
        return builder.toString().toLowerCase();
    }

    /**
     * 根据模本生成的四种基本方法xml内容后再原先的xml内容的添加进去
     * <br />
     * 如果源文件不存在  将会新创建一个mapping.xml文件并附上头节点
     * */
    private void changProjectXml(File file){
        try {
            String ftlContent = readFile(backMapping);
            String orignal = null;
            if (file.exists()) {
                orignal = readFile(file);
            } else {
                if (!file.isAbsolute()) {
                    file = new File( "./" + file);
                }
                file.createNewFile();
            }
            orignal = orignal == null ? ftlContent : orignal.replaceAll(keyWord,ftlContent);
            orignal = escape(orignal);
            LogKit.info(this.getClass(),"开始修改文件" + file);
            generateXml(file,orignal);
            LogKit.info(this.getClass(),"修改文件" + file + "成功");
        }catch (IOException e){
            LogKit.error("修改" + file + "失败!!!",e);
            e.printStackTrace();
        }
    }
    /**
     * @return 0 不合法 1新生成 2替换
     * */
    private int xmlIsHaveKeyword(File file){
        try {
            String type = file.exists() ? readFile(file) : null;
            if (type == null) {
                return 1;
            } else if (type.matches("[\\s\\S]*" + keyWord + "[\\s\\S]*")) {
                return 2;
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    private String readFile(File file) throws IOException {
        FileReader reade = null;
        BufferedReader bufferedReader = null;
        try {
            reade = new FileReader(file);
             bufferedReader = new BufferedReader(reade);
            StringBuffer buffer = new StringBuffer();
            String str= null;
            while ((str = bufferedReader.readLine()) != null){
                buffer.append(str + "\r\n");
            }
            return buffer.toString();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }finally {
            reade.close();
            bufferedReader.close();
        }
    }
    private void generateXml(File file,String content) throws IOException{
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogKit.error(file + "文件写入失败！！！",e);
        }finally {
            writer.close();
        }
    }

    private String escape(String content){
        return content.replace('@','$');
    }
}
