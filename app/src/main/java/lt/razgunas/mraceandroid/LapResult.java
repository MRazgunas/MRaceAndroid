package lt.razgunas.mraceandroid;

/**
 * Created by matas on 2017-08-24.
 */

public class LapResult {
    private int lapTimeMS;
    private String displayTime;
    private String reportTime;

    public LapResult(int lapTime) {
        this.lapTimeMS = lapTime;
        int m = (int)Math.floor(lapTime/1000/60);
        int s = (int)Math.floor(lapTime/1000)-m*60;
        int msec = lapTime - (int)Math.floor(lapTime/1000)*1000;
        displayTime = String.format("%d : %02d . %03d", m, s, msec);
        //reportTime = String.format("%d:%02d.%03d", m, s, msec);

        //speak 2 higher digits of milliseconds separately, or single zero
        String mills = String.format("%03d", msec);
        mills = mills.substring(0, mills.length()-1);
        if (mills.equals("00")) {
            mills = "0";
        }
        mills = mills.replace(""," ").trim();

        String seconds = Integer.toString(s);

        String text = seconds + " point " + mills + " seconds";

        //only add minutes if lap is longer than 1 minute
        if (m > 0) {
            text = Integer.toString(m) + " minutes " + text;
        }
        reportTime = text;
    }

    public int getLapTimeMS() {
        return lapTimeMS;
    }

    public String getReportTime() {
        return reportTime;
    }

    public String getDisplayLapTime() {
        return displayTime;
    }
}
