import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.*

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
    withJansi = true
}

logger "com.kalixia.ha", DEBUG
logger "com.netflix.hystrix", INFO

root(WARN, ["STDOUT"])