import java.util.Date;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;
import java.text.SimpleDateFormat;

public class TimeSimulator {
    private final Timer timer;
    public final long timeMultiplier;
    private final AtomicLong simulatedTime;

    public TimeSimulator(long timeMultiplier) {
        this.timer = new Timer();
        this.timeMultiplier = timeMultiplier;
        this.simulatedTime = new AtomicLong(System.currentTimeMillis());
    }

    public void cancel() {
        timer.cancel();
    }

    public void advanceSimulatedTime(long millis) {
        simulatedTime.addAndGet(millis);
    }

    public String getFormattedSimulatedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(simulatedTime.get()));
    }

    public String getFormattedSimulatedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(new Date(simulatedTime.get()));
    }
}
