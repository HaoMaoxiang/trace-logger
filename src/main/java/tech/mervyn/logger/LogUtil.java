package tech.mervyn.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author HaoMaoxiang@126.com
 * @date 2020/2/21
 */
public class LogUtil {

    private final static Logger log = LoggerFactory.getLogger(LogUtil.class);

    /**
     * 输出格式化的日志
     * @param tag 此条日志的tag
     * @param keysAndValues 日志内容，key与value必须成对出现
     * @return 格式为：_tag||trace_id=xxx||span_id=xxx||key1=value1||key2=value2
     */
    public static String logMsg(String tag, Object... keysAndValues) {
        LogMessage logMessage = new LogMessage();
        logMessage.setTag(tag);
        int len = keysAndValues.length;
        if ((len & 1) == 1) {
            log.error("the number of parameters of function log() must be odd.");
        } else {
            for (int i = 0; i < len; i+=2) {
                try {
                    logMessage.add(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
                } catch (Exception ex) {
                    log.error("exception in the function log().", ex);
                }
            }
        }
        return logMessage.toString();
    }

    public static Long procTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(dateTime, now).toMillis();
    }

}
