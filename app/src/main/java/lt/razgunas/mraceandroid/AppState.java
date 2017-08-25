package lt.razgunas.mraceandroid;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;


public class AppState {

    public static final int TONE_LAP = ToneGenerator.TONE_DTMF_S;
    public static final int TONE_LAP_DURATION = 400;
    public static final int TONE_PREPARE = ToneGenerator.TONE_DTMF_1;
    public static final int PREPARE_DURATION = 80;
    public static final int TONE_GO = ToneGenerator.TONE_DTMF_D;
    public static final int GO_DURATION = 600;

    private static AppState instance = new AppState();

    public TextSpeaker textSpeaker;
    private ToneGenerator mToneGenerator;

    private ArrayList<LapResult> raceResults;

    private boolean isRaceStarted = false;

    private AppState() {
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        raceResults = new ArrayList<LapResult>();
    }

    public void notifyLapPassed(LapResult lap) {
        raceResults.add(lap);
        String textToSay = "Lap time is " + lap.getReportTime();
        textSpeaker.speak(textToSay);
    }

    public void playTone(int tone, int duration) {
        mToneGenerator.startTone(tone, duration);
    }

    public static AppState getInstance() {
        return instance;
    }

    public ArrayList<LapResult> getAllLaps() {
        return raceResults;
    }

    public int getLapCount() {
        return raceResults.size();
    }

    public void resetRace() {
        raceResults.clear();
    }

    public boolean isRaceStarted() {
        return isRaceStarted;
    }

    public void setRaceStarted(boolean raceStarted) {
        isRaceStarted = raceStarted;
    }
}
