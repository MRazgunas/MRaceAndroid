package lt.razgunas.mraceandroid;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


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

    private ArrayList<ParamValue> parameters;

    private ConcurrentHashMap<String, ParamUpdateInterface> paramUpdateListeners = new ConcurrentHashMap<>();

    private boolean isRaceStarted = false;

    private AppState() {
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        raceResults = new ArrayList<LapResult>();
        parameters = new ArrayList<ParamValue>();
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

    public ArrayList<ParamValue> getAllParams() {
        return parameters;
    }

    public void addParameter(ParamValue param) {
        parameters.add(param);
        notifyParamChanged();
    }

    public ParamValue getParameter(String name) {
        for(ParamValue p: parameters) {
            if(p.getParamName() == name) {
                return p;
            }
        }
        return null;
    }

    public ParamValue getParameter(int index) {
        for(ParamValue p: parameters) {
            if(p.getParamIndex() == index)
                return p;
        }
        return null;
    }

    public void updateParameter(ParamValue param) {
        for(int i = 0; i < parameters.size(); i++) {
            if(parameters.get(i).getParamName().equals(param.getParamName())) {

                param.setParamIndex(parameters.get(i).getParamIndex());
                parameters.set(i, param);
                notifyParamChanged();
                return;
            }
        }
        parameters.add(param);
        notifyParamChanged();
    }

    public void notifyParamChanged() {
        for(ParamUpdateInterface p: paramUpdateListeners.values()) {
            p.paramListUpdated();
        }
    }

    public void registerParamListener(String key, ParamUpdateInterface i) {
        paramUpdateListeners.put(key, i);
    }

    public void unregisterParamListener(String key) {
        paramUpdateListeners.remove(key);
    }

    public interface ParamUpdateInterface {
        void paramListUpdated();
    }
}
