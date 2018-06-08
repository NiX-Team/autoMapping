<resultMap id="BaseResultMap" type="${oneself.type}">
    <#list oneself.params as param>
        <#if param.name == "id" >
        <id column="id" property="id" jdbcType="INTEGER"/>
        <#else >
        <result column="${param.name}" property="${param.name}" jdbcType="${param.type}"/>
        </#if>
    </#list>
</resultMap>
<#---->