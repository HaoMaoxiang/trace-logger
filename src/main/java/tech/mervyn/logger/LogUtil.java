package tech.mervyn.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import tech.mervyn.properties.TraceLogProperty;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * @author HaoMaoxiang@126.com
 * @since 2020/2/21
 */
@Component
public class LogUtil {

    private final static Logger log = LoggerFactory.getLogger(LogUtil.class);

    @Resource
    private TraceLogProperty logProperty;

    private static TraceLogProperty staticLogProperty;

    @PostConstruct
    public void init() {
        staticLogProperty = logProperty;
    }

    /**
     * <p>
     * 输出格式化的日志
     * </p>
     *
     * @param tag           此条日志的tag
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
            for (int i = 0; i < len; i += 2) {
                try {
                    String key = String.valueOf(keysAndValues[i]);
                    String value = filterLogMsg(key, keysAndValues[i + 1]);
                    logMessage.add(key, value);
                } catch (Exception ex) {
                    log.warn("exception in the function log().");
                }
            }
        }
        return logMessage.toString();
    }

    private static String filterLogMsg(String key, Object value) {
        if (staticLogProperty != null && staticLogProperty.getBlackList() != null
                && staticLogProperty.getBlackList().contains(key)) {
            value = "******";
        } else if (value instanceof String) {
            value = String.valueOf(value);
        } else {
            JsonNode jsonNode = new ObjectMapper().convertValue(value, JsonNode.class);
            value = formatJsonNode(jsonNode).toString();
        }
        return String.valueOf(value);
    }

    private static JsonNode formatJsonNode(JsonNode jsonNode) {
        if (jsonNode instanceof ArrayNode) {
            for (int i = 0; i < jsonNode.size(); i++) {
                ((ArrayNode) jsonNode).set(i, formatJsonNode(jsonNode.get(i)));
            }
        } else {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fileName = fieldNames.next();
                JsonNode node = jsonNode.get(fileName);
                if (node instanceof ObjectNode) {
                    jsonNode = ((ObjectNode) jsonNode).set(fileName, formatJsonNode(node));
                } else {
                    if (staticLogProperty != null && staticLogProperty.getBlackList() != null
                            && staticLogProperty.getBlackList().contains(fileName)) {
                        jsonNode = ((ObjectNode) jsonNode).put(fileName, "********");
                    }
                }
            }
        }
        return jsonNode;
    }

    public static Long procTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        return Duration.between(dateTime, now).toMillis();
    }

}
