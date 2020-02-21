package tech.mervyn.logger;

/**
 * @author HaoMaoxiang@126.com
 * @date 2020/2/21
 */
public class TraceContext {

    private String traceId;

    private String spanId;

    private int referCounting = 0;

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setReferCounting(int referCounting) {
        this.referCounting = referCounting;
    }

    public int getReferCounting() {
        return referCounting;
    }

    public TraceContext() {}

}
