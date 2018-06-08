package freemaker.util.log;


import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public final class LogKit {
    private static Log log = new SystemStreamLog();

    private final static String getClassName(Class clazz){
        return clazz.getName() + " : ";
    }
    public final static void info(Class clazz,String msg){
        log.info(getClassName(clazz) + msg);
    }
    public final static void info(String msg){
        log.info(msg);
    }
    public final static void debug(Class clazz,String msg){
        log.debug(getClassName(clazz) + msg);
    }
    public final static void debug(String msg){
        log.debug(msg);
    }
    public final static void error(String msg,Throwable throwable){
        log.error(msg,throwable);
    }
    public final static void error(String msg){
        log.error(msg);
    }
}