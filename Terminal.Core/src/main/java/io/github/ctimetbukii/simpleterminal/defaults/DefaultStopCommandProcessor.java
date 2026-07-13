package io.github.ctimetbukii.simpleterminal.defaults;

import io.github.ctimetbukii.simpleterminal.TerminalOptions;
import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.process.transform.ArgsTypeTransformMethod;
import io.github.ctimetbukii.simpleterminal.GlobalTransmitter;
import io.github.ctimetbukii.simpleterminal.utils.records.RunnableMethod;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

@CommandProcessor(value = "stop", descriptionKey = "simpleterminal.defaults.stop")
public class DefaultStopCommandProcessor {
    @InjectErrPrintStream
    private PrintStream err;

    @InjectOutPrintStream
    private PrintStream out;

    @InjectTerminalOption
    private TerminalOptions options;

    /**
     * 立即关闭terminal
     */
    @CommandExecutor(descriptionKey = "simpleterminal.defaults.stop.normal")
    public void stop() {
        out.println("Bye ~ ");

        int status = -1;

        //首先先关闭所有被ClosableFlag标记的
        HashSet<RunnableMethod> closeMethods = GlobalTransmitter.getCloseResourceMethods();
        for (RunnableMethod closeMethod : closeMethods) {
            try {
                closeMethod.run();
            } catch (Exception e) {
                e.printStackTrace(err);
                status += 10000;
            }
        }

        try {
            //再调用TerminalOption中的
            Runnable stopRunnable = options.getStopRunnable();
            if (stopRunnable != null) {
                stopRunnable.run();
            }
        } catch (Exception e) {
            e.printStackTrace(err);
            status += 300;
        }

        //最后调用
        System.exit(status);
    }

    /**
     * 在几毫秒后关闭。e.g.
     * <code>stop -m 200</code>
     * 即为在200毫秒后关闭
     * @param millisDelay 延迟几毫秒
     */
    @SubCommandExecutor(value = "-ms", descriptionKey = "simpleterminal.defaults.stop.millis")
    public void stopAfterMillis(long millisDelay) {
        new Timer()
                .schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stop();
                    }
                }, millisDelay);
        out.println("Stop after " + millisDelay + "ms");
    }

    /**
     * 在几秒后关闭。e.g.
     * <code>stop -s 2</code>
     * 即为在2秒后关闭
     * @param secsDelay 延迟几秒
     */
    @SubCommandExecutor(value = "-s", descriptionKey = "simpleterminal.defaults.stop.secs")
    public void stopAfterSecs(long secsDelay) {
        stopAfterMillis(secsDelay * 1000);
    }

    /**
     * 在几分钟后关闭。e.g.
     * <code>stop -min 2</code>
     * 即为在2分钟后关闭
     * @param minsDelay 延迟几分钟
     */
    @SubCommandExecutor(value = "-min", descriptionKey = "simpleterminal.defaults.stop.mins")
    public void stopAfterMins(long minsDelay) {
        stopAfterSecs(minsDelay * 60);
    }

    /**
     * 在几小时后关闭。e.g.
     * <code>stop -h 2</code>
     * 即为在2小时后关闭
     * @param hoursDelay 延迟几小时
     */
    @SubCommandExecutor(value = "-h", descriptionKey = "simpleterminal.defaults.stop.hours")
    public void stopAfterHours(long hoursDelay) {
        stopAfterMins(hoursDelay * 60);
    }

    /**
     * 在几天后关闭。e.g.
     * <code>stop -d 2</code>
     * 即为在2天后关闭
     * @param daysDelay 延迟几天
     */
    @SubCommandExecutor(value = "-d", descriptionKey = "simpleterminal.defaults.stop.days")
    public void stopAfterDays(long daysDelay) {
        stopAfterHours(daysDelay * 24);
    }

    /**
     * 在几个月后关闭。e.g.
     * <code>stop -mo 2</code>
     * 即为在2个月后关闭。注意，这里的延迟时间，是直接将月数乘以30算出延迟的天数，没有考虑现实中不同月份时长不一致的问题。
     * 如果想要指定在几个月后的同一天关闭，请使用 stop -r 指令
     * @param monthsDelay 延迟几个月
     */
    @SubCommandExecutor(value = "-mo", descriptionKey = "simpleterminal.defaults.stop.months")
    public void stopAfterMonths(long monthsDelay) {
        stopAfterDays(monthsDelay * 30);
    }

    /**
     * 在几年后关闭。e.g.
     * <code>stop -y 2</code>
     * 即为在2年后关闭。注意，这里的延迟时间，是直接将年数乘以365算出延迟的天数，没有考虑现实中闰年时长为366天的问题。
     * 如果想要指定在几年后的同一天关闭，请使用stop -r 指令
     */
    @SubCommandExecutor(value = "-y", descriptionKey = "simpleterminal.defaults.stop.years")
    public void stopAfterYears(long yearsDelay) {
        stopAfterDays(yearsDelay * 365);
    }

    /**
     * 指定在某一天关闭。e.g.<br/>
     * <code>stop -r 2030 2 17</code>
     * 即为在2030年的2月17日的00时00分00秒关闭。<br/>
     * <code>stop -r 2030 2 17 13</code>
     * 即为在2030年的2月17日的13时00分00秒关闭。<br/>
     * <code>stop -r 2030 2 17 13 14</code>
     * 即为在2030年的2月17日的13时14分00秒关闭。<br/>
     * <code>stop -r 2030 2 17 13 14 15</code>
     * 即为在2030年的2月17日的13时14分15秒关闭<br/>
     * <code>stop -r 2030 2 17 13 14 15 888</code>
     * 即为在2030年的2月17日的13时14分15秒888毫秒关闭。最大时间精细度只支持到毫秒<br/>
     */
    @SubCommandExecutor(value = "-r",
            paraTransformMethod = ArgsTypeTransformMethod.COERCE_WITH_INTERPRET,
            patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$"},
            descriptionKey = "simpleterminal.defaults.stop.at")
    public void stopAtRealTime(int targetYear,
                               int targetMon,
                               int targetDay) {
        stopAtRealTime(targetYear, targetMon, targetDay, 0);
    }

    @SubCommandExecutor(value = "-r", paraTransformMethod = ArgsTypeTransformMethod.COERCE_WITH_INTERPRET,
            patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$", "^([01][0-9]|2[0-3])$"},
            descriptionKey = "simpleterminal.defaults.stop.at")
    public void stopAtRealTime(int targetYear,
                               int targetMon,
                               int targetDay,
                               int targetHour) {
        stopAtRealTime(targetYear, targetMon, targetDay, targetHour, 0);
    }

    @SubCommandExecutor(value = "-r", paraTransformMethod = ArgsTypeTransformMethod.COERCE_WITH_INTERPRET,
            patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$", "^([01][0-9]|2[0-3])$", "^([0-5][0-9])$"},
            descriptionKey = "simpleterminal.defaults.stop.at")
    public void stopAtRealTime(int targetYear,
                               int targetMon,
                               int targetDay,
                               int targetHour,
                               int targetMin) {
        stopAtRealTime(targetYear, targetMon, targetDay, targetHour, targetMin, 0);
    }

    @SubCommandExecutor(value = "-r", paraTransformMethod = ArgsTypeTransformMethod.COERCE_WITH_INTERPRET,
            patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$", "^([01][0-9]|2[0-3])$", "^([0-5][0-9])$", "^([0-5][0-9])$"},
            descriptionKey = "simpleterminal.defaults.stop.at")
    public void stopAtRealTime(int targetYear,
                               int targetMon,
                               int targetDay,
                               int targetHour,
                               int targetMin,
                               int targetSec) {
        stopAtRealTime(targetYear, targetMon, targetDay, targetHour, targetMin, targetSec, 0);
    }

    @SubCommandExecutor(value = "-r", paraTransformMethod = ArgsTypeTransformMethod.COERCE_WITH_INTERPRET,
                        patterns = {"-?\\d+", "^(0[1-9]|1[0-2])$", "^(0[1-9]|[12][0-9]|3[01])$", "^([01][0-9]|2[0-3])$", "^([0-5][0-9])$", "^([0-5][0-9])$", "^\\d{1,3}$"},
            descriptionKey = "simpleterminal.defaults.stop.at")
    public void stopAtRealTime(int targetYear,
                               int targetMon,
                               int targetDay,
                               int targetHour,
                               int targetMin,
                               int targetSec,
                               int targetMill) {
        long millisDiff = millisUntil(targetYear, targetMon, targetDay, targetHour, targetMin, targetSec, targetMill);
        if (millisDiff <= 0) {
            err.println("Target time invalid!");
            return;
        }

        stopAfterMillis(millisDiff);
    }

    private static long millisUntil(int targetYear, int targetMon, int targetDay,
                                   int targetHour, int targetMin, int targetSec,
                                   int targetMill) {

        // 1. 构建目标 LocalDateTime（注意月份从1开始）
        LocalDateTime targetLdt = LocalDateTime.of(targetYear, targetMon, targetDay,
                targetHour, targetMin, targetSec, targetMill * 1_000_000); // 毫秒转纳秒

        // 2. 获取当前时间（系统默认时区）
        ZonedDateTime nowZdt = ZonedDateTime.now();
        LocalDateTime nowLdt = nowZdt.toLocalDateTime();

        // 3. 将目标时间也转为同样的时区（此处直接使用系统默认时区）
        ZonedDateTime targetZdt = targetLdt.atZone(ZoneId.systemDefault());

        // 4. 判断是否在未来（isAfter 严格大于）
        boolean isFuture = targetZdt.isAfter(nowZdt);

        // 5. 计算毫秒差（精确到毫秒）

        return ChronoUnit.MILLIS.between(nowZdt, targetZdt);
    }
}
