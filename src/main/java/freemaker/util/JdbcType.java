package freemaker.util;

/**
 * @author Kiss
 * @date 2018/06/08 12:01
 */
import java.util.HashMap;
import java.util.Map;

public enum JdbcType {
    ARRAY(2003),
    BIT(-7),
    TINYINT(-6),
    SMALLINT(5),
    INTEGER(4),
    BIGINT(-5),
    FLOAT(6),
    REAL(7),
    DOUBLE(8),
    NUMERIC(2),
    DECIMAL(3),
    CHAR(1),
    VARCHAR(12),
    LONGVARCHAR(-1),
    DATE(91),
    TIME(92),
    TIMESTAMP(93),
    BINARY(-2),
    VARBINARY(-3),
    LONGVARBINARY(-4),
    NULL(0),
    OTHER(1111),
    BLOB(2004),
    CLOB(2005),
    BOOLEAN(16),
    CURSOR(-10),
    UNDEFINED(-2147482648),
    NVARCHAR(-9),
    NCHAR(-15),
    NCLOB(2011),
    STRUCT(2002);

    public final int TYPE_CODE;
    private static Map<Integer, JdbcType> codeLookup = new HashMap();

    private JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    public static JdbcType forCode(int code) {
        return (JdbcType)codeLookup.get(code);
    }

    static {
        JdbcType[] arr$ = values();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            JdbcType type = arr$[i$];
            codeLookup.put(type.TYPE_CODE, type);
        }

    }
}
