package freemaker;

import freemaker.util.FreemarkerRoot;
import freemaker.util.log.LogKit;
import freemarker.template.TemplateException;
import java.io.IOException;

/**
 *
 * @author 11723
 * @date 2017/5/7
 */
public class XmlService {

    public static void startGenerateMappingXml(Class clazz, String mappingFileName, String tableName, FreemarkerRoot freemarker){
        try {
            long start = System.currentTimeMillis();
            LogKit.info(XmlService.class,"开始自动写入" + mappingFileName + "的mapping.xml内容");
            freemarker.createMapperXml(clazz,mappingFileName,tableName);
            LogKit.info(XmlService.class,"写入" + mappingFileName + "的mapping.xml文件成功");
            LogKit.info(XmlService.class,"耗时：" + (System.currentTimeMillis() - start)/1000 + "毫秒");
        } catch (IOException e) {
            LogKit.error("文件操作异常！！！",e);
            e.printStackTrace();
        } catch (TemplateException e) {
            LogKit.error("ftl格式错误！！！",e);
            e.printStackTrace();
        }
    }
}
