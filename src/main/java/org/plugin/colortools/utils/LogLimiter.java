package org.plugin.colortools.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 限频日志工具
 * 热路径（数据包拦截、聊天/进出/死亡事件）上的异常打印若不加限制，
 * 异常会以事件频率反复刷栈，直接造成 TPS 尖刺。
 * 本工具按 key 做 10 秒窗口限频：窗口内同一 key 只输出一次完整堆栈，
 * 解除限频时汇报期间被抑制的次数，避免日志静默丢失信息。
 */
public final class LogLimiter {

    private static final long WINDOW_MS = 10_000L;
    private static final Map<String, Long> LAST_LOG = new ConcurrentHashMap<>();
    private static final Map<String, Long> SUPPRESSED = new ConcurrentHashMap<>();

    private LogLimiter() {
    }

    /**
     * 限频告警：同一 key 10 秒内只输出一次完整堆栈
     *
     * @param logger  目标日志器
     * @param key     限频键（建议「类名.场景」，如 packet.systemChat）
     * @param message 告警消息
     * @param t       异常（完整堆栈仅窗口首次输出时打印）
     */
    public static void warn(Logger logger, String key, String message, Throwable t) {
        long now = System.currentTimeMillis();
        Long last = LAST_LOG.get(key);
        if (last != null && now - last < WINDOW_MS) {
            SUPPRESSED.merge(key, 1L, Long::sum);
            return;
        }
        LAST_LOG.put(key, now);

        Long suppressed = SUPPRESSED.remove(key);
        if (suppressed != null && suppressed > 0) {
            logger.log(Level.WARNING, "[限频] 以下异常在 10 秒内被抑制了 " + suppressed + " 次: " + key);
        }
        logger.log(Level.WARNING, message, t);
    }
}
