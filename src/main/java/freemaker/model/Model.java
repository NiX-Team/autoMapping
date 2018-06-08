package freemaker.model;

import java.util.List;

/**
 * Created by 11723 on 2017/5/6.
 */
public class Model {
    //表明
    private String table;
    private String type;
    List<Param> params;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
