package tech.mervyn.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * @author HaoMaoxiang@126.com
 * @since 2020/2/23
 */
@Configuration
public class TraceLogProperty {

    @Value("${trace-logger.black-list:null}")
    private Set<String> blackList;

    @Value("${trace-logger.controller-log:true}")
    private Boolean controllerLog;

    @Value("${trace-logger.service-log:true}")
    private Boolean serviceLog;

    @Value("${trace-logger.sql-log:true}")
    private Boolean sqlLog;

    @Value("${trace-logger.sql-params-log:true}")
    private Boolean sqlParamsLog = true;

    public Set<String> getBlackList() {
        return blackList;
    }

    public Boolean getControllerLog() {
        return controllerLog;
    }

    public Boolean getServiceLog() {
        return serviceLog;
    }

    public Boolean getSqlLog() {
        return sqlLog;
    }

    public Boolean getSqlParamsLog() {
        return sqlParamsLog;
    }
}
