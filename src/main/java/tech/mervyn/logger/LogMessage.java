package tech.mervyn.logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

/**
 * @author HaoMaoxiang@126.com
 * @date 2020/2/21
 */
public class LogMessage {

    private final static String DEFAULT_SPAN_ID = "span-id.0";
    private final static Integer MIN_AUTO_NUMBER = 1000;
    private final static Integer MAX_AUTO_NUMBER = 10000;
    private static Integer autoIncreaseNumber = MIN_AUTO_NUMBER;
    private String tag = "_def";

    private static ThreadLocal<TraceContext> traceContext = new InheritableThreadLocal<TraceContext>() {
        @Override
        protected TraceContext initialValue() {
            return new TraceContext();
        }
    };

    public static void remove() {
        traceContext.remove();
    }

    private StringBuffer logMsg = new StringBuffer();

    public LogMessage add(String key, Object value) {
        logMsg.append("||").append(key).append("=").append(value);
        return this;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public static void setSpanId(String spanId) {
        traceContext.get().setSpanId(spanId);
    }

    public static String getSpanId() {
        String spanId = traceContext.get().getSpanId();
        if (spanId == null) {
            traceContext.get().setSpanId(DEFAULT_SPAN_ID);
            return DEFAULT_SPAN_ID;
        }
        return spanId;
    }

    public static String generateSpanId() {
        String spanId = getSpanId();
        int count = traceContext.get().getReferCounting();
        spanId = spanId + "." + count;
        traceContext.get().setReferCounting(count + 1);
        return spanId;
    }

    public static void setTraceId(String traceId) {
        traceContext.get().setTraceId(traceId);
    }

    public static String getTraceId() {
        String traceId = traceContext.get().getTraceId();
        if (traceId == null) {
            traceId = generateTraceId();
        }
        return traceId;
    }

    /**
     * <p>
     *     生成64位traceId，规则是 服务器 IP + 产生ID时的时间 + 自增序列 + 当前进程号
     *     IP 8位：10.209.52.143 -> 0ad1348f
     *     产生ID时的时间 13位： 毫秒时间戳 -> 1403169275002
     *     自增序列 4位： 1000-9999循环
     *     当前进程号 5位： PID
     * </p>
     * @return 0ad1348f1403169275002100056696
     */
    public static String generateTraceId() {
        StringBuilder traceId = new StringBuilder();
        try {
            // 1. IP
            InetAddress ip = InetAddress.getLocalHost();
            traceId.append(convertIp(ip.getHostAddress()));
            // 2. 时间戳
            traceId.append(Instant.now().toEpochMilli());
            // 3. 自增序列
            traceId.append(getAutoIncreaseNumber());
            // 4. 当前进程号
            traceId.append(getProcessId());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return traceId.toString();
    }

    /**
     * <p>IP转换</p>
     * @param ip 10.209.52.143
     * @return 0ad1348f
     */
    private static String convertIp(String ip) {
        String[] split = ip.split("\\.");
        StringBuilder res = new StringBuilder();
        for (String str : split) {
            res.append(String.format("%02x", Integer.valueOf(str)));
        }
        return res.toString();
    }

    /**
     * <p>使得自增序列在1000-9999之间循环</p>
     * @return 自增序列号
     */
    private static int getAutoIncreaseNumber() {
        if (autoIncreaseNumber.equals(MAX_AUTO_NUMBER)) {
            autoIncreaseNumber = MIN_AUTO_NUMBER;
            return autoIncreaseNumber;
        } else {
            return autoIncreaseNumber++;
        }
    }

    /**
     * @return 5位当前进程号
     */
    public static String getProcessId() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        return String.format("%05d", Integer.parseInt(runtime.getName().split("@")[0]));
    }

    @Override
    public String toString() {
        return tag + "||trace_id=" + getTraceId() +
                "||span_id=" + getSpanId() +
                logMsg.toString();
    }

}
