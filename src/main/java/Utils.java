import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collector;

public class Utils {
    public static final Collector<Point2D, Point2DSummary, PointsInfo> POINT_COLLECTOR =
            Collector.of(
                    () -> new Point2DSummary(),          // supplier
                    (j, p) -> j.accept(p),  // accumulator
                    (j1, j2) -> {
                        Point2DSummary v = new Point2DSummary();
                        v.accept(j1.average());
                        v.accept(j2.average());
                        return v;
                    },               // combiner
                    summary -> new PointsInfo(summary));

    static final String LOG_MOVING = "MOVING";
    public static String WARN = "WARN";
    private static LinkedBlockingQueue<String> queueToLog;
    private static volatile PrintWriter logPrintWriter;
    private static volatile PrintStream logPrintStream;

    public static String format(double v) {
        return String.format("%.2f", v);
    }

    static double distanceTo(int dx, int dy) {
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void log(String s) {
        if (!Main.isLocalRun) {
             return;
        }
        if (queueToLog == null) {
            queueToLog = new LinkedBlockingQueue<>();
            new Thread(() -> {
                try {
                 //   PrintWriter out = getPrintWriter();
                    PrintStream outStream = getPrintStream();
                    for (; ; ) {
                        String take = queueToLog.take();
                        outStream.append(take + "\n");
                        outStream.flush();
                      /*  out.println(take);
                        out.flush();*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        queueToLog.add(s);

    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static PrintWriter getPrintWriter() {
        if (logPrintWriter == null) {
            FileWriter fw = null;
            try {
                String filePath = "/Users/fox/projects/miniaicup2/log.txt";
                new File(filePath).delete();
                fw = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            logPrintWriter = new PrintWriter(new BufferedWriter(fw));
        }
        return logPrintWriter;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public static PrintStream getPrintStream() {
        if (logPrintStream == null) {
            try {
                String filePath = "/Users/fox/projects/miniaicup2/log.txt";
                new File(filePath).delete();
                logPrintStream = new PrintStream(new FileOutputStream(filePath, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logPrintStream;
    }

    public static void print(Exception e) {
        if (Main.isLocalRun) {
            e.printStackTrace();
        }
    }

    public static double normalizeAngle(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    public static double normalizeAngleFast(double a, double center) {
        return a - (Math.PI * 2) * Math.floor((a + Math.PI - center) / (Math.PI * 2));
    }

    public static double mod(double a, double n) {
        return a - Math.floor(a/n) * n;
    }
}
