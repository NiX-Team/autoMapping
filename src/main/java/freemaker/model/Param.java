package freemaker.model;

/**
 * Created by 11723 on 2017/5/6.
 */
public class Param {
    private String property;
    private String column;
    private String jdbcType;
    public Param(String column,String property,String jdbcType){
        this.column = column;
        this.property = property;
        this.jdbcType = jdbcType;
    }
    public Param(){}

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }
}
