/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.configuration;

import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class FoSqlServer2012Dialect extends SQLServer2012Dialect {

    public FoSqlServer2012Dialect() {
        registerFunction("spid", new NoArgSQLFunction("@@SPID", StandardBasicTypes.LONG, false));
    }
}
