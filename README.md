# autoMapping
--- 
## mybatis mapping映射配置文件自动生成插件

### 使用方法
用idea导入项目，install到本地仓库。
在需要自动生成映射文件xml的项目里配置插件<br/>
### 注意事项
1. 配置中的根路径为项目路径
2. 类名必须使用首字母大写的驼峰命名
3. 实体必须有id字段，切实体必须使用id座位主键
4. 实体的字段命名必须以驼峰命名
5. 字段映射到数据库的字段都是将驼峰命名的大写字母改为_小写（userId -> user_id）
---


``` xml
<build>
    <plugins>
        <plugin>
            <groupId>com.alibaba.jingxun</groupId>
            <artifactId>auto-mapping-xml</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <!-- 配置文件路径（相对路径根路径为主项目路径）-->
                <name>./auto.xml</name>
                <!-- 项目的路径（1.0必须填项目的路径）-->
                <path>G:\IDEA\bookHandlerSystem</path>
                <!-- 自定义xml模板路径（不配置使用默认配置模板）-->
                <ftl></ftl>
            </configuration>
        </plugin>
    </plugins>
</build>
```
### 配置文件语法
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <packages>
         <!--使用包扫描时实体的表名默认与类名的驼峰命名一致-->
        <package>
            <!--需要自动生成xml的实体的包-->
            <name>com.alibaba.jingxun.model</name>
            <!--xml生成的路径（使用包扫描时默认xml文件与类名一致）-->
            <path>./resources/mapping/model/</path>
        </package>
       
        <package>
            <name>com.alibaba.jingxun.entity</name>
            <!--不配置path时默认xml路径与类文件路径一致-->
        </package>
    </packages>
    <!--单独生成莫格实体的xml配置-->
    <model>
        <!--实体的完全限定名（包路径+类名）-->
        <clazz>com.alibaba.jingxun.model.Student</clazz>
        <!--xml生成路径-->
        <mapping>./student.xml</mapping>
        <!--实体在数据库中的表名-->
        <table>student</table>
    </model>
</config>

```

### ftl模板语法

``` ftl
<#--table:实体对应数据库的表名-->
<#--oneself.type:实体完全限定名-->
<resultMap id="BaseResultMap" type="${oneself.type}">
    <#--oneself.params:实体参数列表->
    <#list oneself.params as param>
        <#--param.property:实体参数字段名->
        <#--param.column:实体参数对应数据库字段名->
        <#--param.jdbcType:实体参数对应数据库类型->
        <#if param.property == "id" >
        <id column="${param.column}" property="${param.property}" jdbcType="${param.jdbcType}"/>
        <#else >
        <result column="${param.column}" property="${param.property}" jdbcType="${param.jdbcType}"/>
        </#if>
    </#list>
</resultMap>

```
### 默认ftl模板的Dao方法
```java
    /**
     * 添加实体
     * @param object
     * */
    void insert(M object);
    /**
     * 删除实体
     * @param id
     * */
    void delete(@Param("id") Integer id);
    /**
     * 更新实体
     * @param model
     * */
    void update(M model);
    /**
     * 查询实体
     * @param id
     * @return
     * */
    M select(@Param("id") Integer id);
    /**
     * 获取实体数据库的最大id值
     * @return
     * */
    Integer maxId(M m);
    /**
     * 获取实体的数目
     * @return
     * */
    Long count();
    /**
     * 根据每个字段查找实体列表
     * @param field 字段
     * @param value 值
     * @return 符合条件的实体列表
     * */
    List<M> findByOneField(@Param("field") String field,@Param("value") String value);
    /**
    * 查找分页实体列表
    * @param tables list查询的表字符串
    * <p>（select * from user,role where 1 = 1）</p>
    * <p>table 为from到where之间的字符串</p>
    * @param offset 偏移量
    * @param limit 分页大小
    * @param sort 排序字段
    * @param order 排序方式
    * @param conditions 查询条件的sql（where 后面的条件）
    * @return 分页内容
    * */
    List<M> list(@Param("tables") String tables,@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("order") String order, @Param("sort") String sort, @Param("conditions") String conditions);

```
### idea启动方法
1. 构建项目，保证需要生产的实体被编译
2. 在启动项里添加maven启动项
3. 在command line 填auto-mapping-xml:auto-mapping
4. 直接运行即可


### 错误提示
1. fileNotFound 文件路径有误，注意使用./开头的文件路径的根路径是项目路径
2. 不是正确的类路径 看项目是否编译，类的完全限定名是否错误，pom配置path路径是否错误

#### 1.1版本计划
1. 将项目路径改为自动识别，就不需要配置项目路径
2. 内部嵌入java编译功能，可以在项目编译的情况下运行
3. 修改1.0版本bug
#### 1.2版本计划
1. 将项目改为不需要自行自动生成xml
2. 修改1.1版本bug