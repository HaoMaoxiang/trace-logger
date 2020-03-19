package tech.mervyn.interceptor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.mervyn.logger.LogUtil;
import tech.mervyn.properties.TraceLogProperty;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * <p>
 * MyBatis拦截器，打印sql语句，以及执行时间
 * </p>
 *
 * @author HaoMaoxiang@126.com
 * @since 2020/2/23
 */
@Component
@Intercepts(
        {@Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                Object.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                        Object.class, RowBounds.class, ResultHandler.class})}
)
public class MyBatisInterceptor implements Interceptor {

    private final static Logger log = LoggerFactory.getLogger(MyBatisInterceptor.class);

    @Resource
    private TraceLogProperty logProperty;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!logProperty.getSqlLog()) {
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        // 获取参数，if语句成立，表示sql语句有参数，参数格式是map形式
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }

        //id为执行的mapper方法的全路径名，如tech.mervyn.dao.UserMapper.insertUser
        String id = mappedStatement.getId();
        // sql 语句类型
        SqlCommandType type = mappedStatement.getSqlCommandType();
        // 获取节点的配置
        Configuration configuration = mappedStatement.getConfiguration();
        //获取到sql语句
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        String sql = boundSql.getSql();
        if (logProperty.getSqlParamsLog()) {
            // 进行？的替换
            sql = showSql(configuration, boundSql);
        }
        LocalDateTime start = LocalDateTime.now();
        Object proceed = invocation.proceed();
        log.info(LogUtil.logMsg("_com_mysql_success", "method", id, "type", type, "sql", sql,
                "return", proceed, "proc_time", LogUtil.procTime(start)));
        return proceed;
    }

    private String showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // sql语句中多个空格都用一个空格代替
        String sql = boundSql.getSql().replace("[\\s]+", " ");
        if (!parameterMappings.isEmpty() && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            // 如果根据parameterObject.getClass(）可以找到对应的类型，则替换
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?",
                        Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值,
                // 主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        // 该分支是动态sql
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } else {
                        // 打印出缺失，提醒该参数缺失并防止错位
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    /**
     * <p>
     * 如果参数是String，则添加单引号， 如果是日期，则转换为时间格式器并加单引号； 对参数是null和不是null的情况作了处理
     * </p>
     *
     * @param obj 参数
     * @return 处理后的参数
     */
    private String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                    DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(obj) + "'";
        } else {
            if (obj != null) {
                value = String.valueOf(obj);
            } else {
                value = "";
            }
        }
        return value;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }

    }

    @Override
    public void setProperties(Properties properties) {

    }
}
