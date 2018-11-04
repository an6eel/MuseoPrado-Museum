package com.example.agarc.museoprado;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.github.bassaer.chatmessageview.view.MessageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ai.api.android.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import pl.droidsonroids.gif.GifImageButton;

public class VoiceAgent extends Chat implements RecognitionListener   {

    private static final String LOGTAGSR = "SpeechRecognition";
    private static final String LOGTAGTTS = "TextToSpeech";

    private long startListeningTime = 0;
    private static final String KEY = "028209d144994f75ae0bf618d3413435";
    private Context context;

    private TextToSpeech tts;
    private SpeechRecognizer sr;

    private AIDataService aiData;
    private AIRequest aiReq;

    public VoiceAgent(Context cx,MessageView chat){
        super(chat);
        this.context =cx;

        initTTS();
        initSR();
        final AIConfiguration config = new AIConfiguration(KEY,
                AIConfiguration.SupportedLanguages.Spanish,
                AIConfiguration.RecognitionEngine.System);

        aiData= new AIDataService(context,config);

        aiReq = new AIRequest();
    }

    public void execQuery(String qry){
        aiReq.setQuery(qry);

        new AsyncTask<AIRequest,Void,AIResponse>(){

            @Override
            protected AIResponse doInBackground(AIRequest ... requests){

                final  AIRequest rqst= requests[0];

                try {
                    final AIResponse response = aiData.request(aiReq);
                    return response;
                } catch (AIServiceException e){

                }

                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse){

                if(aiResponse != null){

                    final Result res = aiResponse.getResult();

                    putResponse(res.getFulfillment().getSpeech(), com.github.bassaer.chatmessageview.model.Message.Type.TEXT,null);
                    speak(res.getFulfillment().getSpeech());
                }

            }
        }.execute(aiReq);
    }

    @Override
    public void onReadyForSpeech(Bundle params) { }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) { }

    @Override
    public void onBufferReceived(byte[] buffer) {}

    @Override
    public void onEndOfSpeech() {
        GifImageButton b = (GifImageButton)  ( (Activity) context).findViewById(R.id.listening);
        b.setBackgroundResource(R.drawable.mic);
    }

    @Override
    public void onError(int errorCode) {
        long duration = System.currentTimeMillis() - startListeningTime;
        if (duration < 500 && errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
            Log.e(LOGTAGSR, "Doesn't seem like the system tried to listen at all. duration = " + duration + "ms. Going to ignore the error");
            stopListening();
        }
        else {
            String errorMsg = "";
            switch (errorCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMsg = "Audio recording error";
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMsg = "Unknown client side error";
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMsg = "Insufficient permissions";
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMsg = "Network related error";
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMsg = "Network operation timed out";
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMsg = "No recognition result matched";
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMsg = "RecognitionService busy";
                case SpeechRecognizer.ERROR_SERVER:
                    errorMsg = "Server sends error status";
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMsg = "No speech input";
                default:
                    errorMsg = "";
            }
            if (errorCode == 5 && errorMsg == "") {
                Log.e(LOGTAGSR, "Going to ignore the error");
                //Another frequent error that is not really due to the ASR
            } else {
                Log.e(LOGTAGSR, "Error -> " + errorMsg);
                stopListening();
            }
        }
    }

    @Override
    public void onResults(Bundle results) {
        if(results!=null){
            Log.i(LOGTAGSR,"SR results have been received.");

            ArrayList<String> nBestList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            execQuery(nBestList.get(0));

            putRequest(nBestList.get(0));
        }
        else
            Log.e(LOGTAGSR,"SR results haven't been received");

        stopListening();
    }

    @Override
    public void onPartialResults(Bundle partialResults) { }

    @Override
    public void onEvent(int eventType, Bundle params) {}

    public void initSR(){
        List<ResolveInfo> intActivities = context.getPackageManager().queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0 ) {
            sr = SpeechRecognizer.createSpeechRecognizer(context);
            sr.setRecognitionListener(this);
        }
    }

    public void initTTS(){
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status){
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(new Locale("es","ES"));
                }
            }
        });

    }

    public void listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,new Locale("es","ES"));

        sr.startListening(intent);

    }

    public void speak(String msg){
        tts.speak(msg,TextToSpeech.QUEUE_FLUSH,null,"message");
    }

    public void stopListening(){
        sr.stopListening();
    }

}
