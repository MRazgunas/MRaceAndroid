package lt.razgunas.mraceandroid;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.Locale;
import java.util.Set;

/**
 * Created by matas on 2017-08-24.
 */

public class TextSpeaker implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean isInit = false;

    public TextSpeaker(Context context) {
        tts = new TextToSpeech(context, this);
    }

    public void speak(String text) {
        if(isInit) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    @Override
    public void onInit(int i) {
        if(i != TextToSpeech.ERROR) {
            tts.setLanguage(Locale.ENGLISH);
            isInit = true;
        }
    }
}
