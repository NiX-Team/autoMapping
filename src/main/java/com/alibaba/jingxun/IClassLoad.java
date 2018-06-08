package com.alibaba.jingxun;

import java.io.*;

/**
 * @author Kiss
 * @date 2018/06/08 17:45
 */

public class IClassLoad extends ClassLoader {

    private IClassLoad(){}
    private volatile static IClassLoad iClassLoad = null;

    public static IClassLoad getClassLoader() {
        if (iClassLoad == null) {
            synchronized (IClassLoad.class) {
                if (iClassLoad == null) {
                    iClassLoad = new IClassLoad();
                }
            }
        }
        return iClassLoad;
    }

    /**
     * 字节码文件存放位置
     */
    private String path;
    /**
     * 继承ClassLoader进行重写,加载目标类的字节码
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clazz = null;//this.findLoadedClass(name); // 父类已加载
        byte[] classData = getClassData(name);  //根据类的二进制名称,获得该class文件的字节码数组
        if (classData == null) {
            throw new ClassNotFoundException();
        }
        clazz = defineClass(name, classData, 0, classData.length);  //将class的字节码数组转换成Class类的实例
        return clazz;
    }

    public Class<?> loadClass(String path,String name) throws ClassNotFoundException {
        this.path = path;
        return super.loadClass(name);
    }

    /**
     * 读取.class文件
     */
    private byte[] getClassData(String name) {
        name = classNameToPath(name);
        InputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            is = new FileInputStream(new File(name));
            int len = -1;
            byte[] buff = new byte[1024*4];
            while((len = is.read(buff)) != -1) {
                baos.write(buff,0,len);
            }
            byte[] byteArray = baos.toByteArray();
            return byteArray;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 格式转换
     */
    private String classNameToPath(String name) {
        return path +  name.replace(".", "/") + ".class";
    }
}
