package util

import org.slf4j.*
import org.slf4j.helpers.BasicMDCAdapter
import org.slf4j.spi.MDCAdapter
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

object Logging : Logger {

    private val severity = Level.Trace

    override fun getName(): String = "Logger"

    override fun isTraceEnabled(): Boolean = true

    override fun isTraceEnabled(marker: Marker?): Boolean = true

    override fun trace(msg: String?) = println(msg)
    override fun trace(format: String?, arg: Any?) = println(format?.let { String.format(format, arg) })
    override fun trace(format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Trace, format?.let { String.format(format, arg1, arg2) })

    override fun trace(format: String?, vararg arguments: Any?) =
        printLog(Level.Trace, format?.let { String.format(format, arguments) })

    override fun trace(msg: String?, t: Throwable?) {
        printLog(Level.Trace, msg)
        t?.printStackTrace(System.out)
    }

    override fun trace(marker: Marker?, msg: String?) = printLog(Level.Trace, msg)

    override fun trace(marker: Marker?, format: String?, arg: Any?) =
        printLog(Level.Trace, format?.let { String.format(format, arg) })

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Trace, format?.let { String.format(format, arg1, arg2) })

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) =
        printLog(Level.Trace, format?.let { String.format(format, argArray) })

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        printLog(Level.Trace, msg)
        t?.printStackTrace(System.out)
    }

    override fun isDebugEnabled(): Boolean = true

    override fun isDebugEnabled(marker: Marker?): Boolean = true

    override fun debug(msg: String?) = printLog(Level.Debug, msg)

    override fun debug(format: String?, arg: Any?) = printLog(Level.Debug, format?.let { String.format(format, arg) })

    override fun debug(format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Debug, format?.let { String.format(format, arg1, arg2) })

    override fun debug(format: String?, vararg arguments: Any?) =
        printLog(Level.Debug, format?.let { String.format(format, arguments) })

    override fun debug(msg: String?, t: Throwable?) {
        printLog(Level.Debug, msg)
        t?.printStackTrace(System.out)
    }

    override fun debug(marker: Marker?, msg: String?) = printLog(Level.Debug, msg)

    override fun debug(marker: Marker?, format: String?, arg: Any?) =
        printLog(Level.Debug, format?.let { String.format(format, arg) })

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Debug, format?.let { String.format(format, arg1, arg2) })

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) =
        printLog(Level.Debug, format?.let { String.format(format, arguments) })

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        printLog(Level.Debug, msg)
        t?.printStackTrace(System.out)
    }

    override fun isInfoEnabled(): Boolean = true

    override fun isInfoEnabled(marker: Marker?): Boolean = true

    override fun info(msg: String?) = printLog(Level.Info, msg)

    override fun info(format: String?, arg: Any?) = printLog(Level.Info, format?.let { String.format(format, arg) })

    override fun info(format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Info, format?.let { String.format(format, arg1, arg2) })

    override fun info(format: String?, vararg arguments: Any?) =
        printLog(Level.Info, format?.let { String.format(format, arguments) })

    override fun info(msg: String?, t: Throwable?) {
        printLog(Level.Info, msg)
        t?.printStackTrace(System.out)
    }

    override fun info(marker: Marker?, msg: String?) = printLog(Level.Info, msg)

    override fun info(marker: Marker?, format: String?, arg: Any?) = printLog(Level.Info, format?.let { String.format(format, arg) })

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Info, format?.let { String.format(format, arg1, arg2) })

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) =
        printLog(Level.Info, format?.let { String.format(format, arguments) })

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        printLog(Level.Info, msg)
        t?.printStackTrace(System.out)
    }

    override fun isWarnEnabled(): Boolean = true

    override fun isWarnEnabled(marker: Marker?): Boolean = true

    override fun warn(msg: String?) = printLog(Level.Warn, msg)

    override fun warn(format: String?, arg: Any?) = printLog(Level.Warn, format?.let { String.format(format, arg) })

    override fun warn(format: String?, vararg arguments: Any?) =
        printLog(Level.Warn, format?.let { String.format(format, arguments) })

    override fun warn(format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Warn, format?.let { String.format(format, arg1, arg2) })

    override fun warn(msg: String?, t: Throwable?) {
        printLog(Level.Warn, msg)
        t?.printStackTrace(System.err)
    }

    override fun warn(marker: Marker?, msg: String?) = printLog(Level.Warn, msg)

    override fun warn(marker: Marker?, format: String?, arg: Any?) = printLog(Level.Warn, format?.let { String.format(format, arg) })

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        printLog(Level.Warn, format?.let { String.format(format, arg1, arg2) })

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) =
        printLog(Level.Warn, format?.let { String.format(format, arguments) })

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        printLog(Level.Warn, msg)
        t?.printStackTrace(System.err)
    }

    override fun isErrorEnabled(): Boolean = true

    override fun isErrorEnabled(marker: Marker?): Boolean = true

    override fun error(msg: String?) {
        printLog(Level.Error, msg, where = System.err)
    }

    override fun error(format: String?, arg: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arg) }, where = System.err)
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arg1, arg2) }, where = System.err)
    }

    override fun error(format: String?, vararg arguments: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arguments) }, where = System.err)
    }

    override fun error(msg: String?, t: Throwable?) {
        printLog(Level.Error, msg, where = System.err)
        t?.printStackTrace(System.err)
    }

    override fun error(marker: Marker?, msg: String?) {
        printLog(Level.Error, msg, where = System.err)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arg) }, where = System.err)
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arg1, arg2) }, where = System.err)
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        printLog(Level.Error, format?.let { String.format(format, arguments) }, where = System.err)
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        printLog(Level.Error, msg, where = System.err)
        t?.printStackTrace(System.err)
    }

    private fun printLog(level: Level, message: String?, where: PrintStream = System.out) {
        if (severity.value >= level.value) {
            where.println(
                "${localDateFormat.get().format(Date())} [${level.label}]: $message"
            )
        }
    }

    private val localDateFormat =
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("HH:mm:ss:SSS")
            }
        }

    private enum class Level(val label: String, val value: Int) {
        Trace("TRACE", 4),
        Info("INFO", 3),
        Debug("DEBUG", 2),
        Warn("WARN", 1),
        Error("ERROR", 0)
    }
}

object LoggerFactory : ILoggerFactory {
    override fun getLogger(name: String?): Logger = Logging
}

class LoggingBinder : org.slf4j.spi.SLF4JServiceProvider {
    override fun getLoggerFactory(): ILoggerFactory = LoggerFactory

    override fun getMarkerFactory(): IMarkerFactory = MarkerFactory.getIMarkerFactory()

    override fun getMDCAdapter(): MDCAdapter = BasicMDCAdapter()

    override fun getRequestedApiVersion(): String = "2.0.0"

    override fun initialize() {
    }
}
