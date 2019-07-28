package com.project.miniCare.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.project.miniCare.LiveActivity;
import com.project.miniCare.MainActivity;
import com.project.miniCare.R;
import com.project.miniCare.RecordActivity;
import com.project.miniCare.Utils.changeHandler;

import java.util.Random;

public class MainMenuFragment extends Fragment implements changeHandler {
    private static final String TAG = "MainMenuFragment";
    private String[][] messages = {
            {"The weather\nlooks good~", "Let's go for\na walk shall we?"},
            {"Ahhh...\nI am starving...", "Shall we go\nget some food?"},
            {"Cheese Burger!!!","Control yourself\nplease!"}
    };

    private int minInterval = 10000;
    private int maxInterval = 15000;
    public Handler handler;
    private Random rng = new Random();
    private TextView speechBubbleLeft;
    private TextView speechBubbleRight;
    private Button record_button;
    private Button golive_button;

    @Override
    public void onStartHandler() {
        handler = new Handler();
        handler.postDelayed(Speak, (long) rng.nextFloat()*(maxInterval-minInterval)+5000);
    }

    @Override
    public void onStopHandler() {
        handler.removeCallbacksAndMessages(null);
    }

    class ShowSpeech implements Runnable {
        private TextView speechBubble;
        private String message;
        public ShowSpeech(TextView speechBubble, String message){
            this.speechBubble = speechBubble;
            this.message = message;
        }
        public void run() {
            speechBubble.setText(message);
            speechBubble.setVisibility(View.VISIBLE);
        }
    };
    class HideSpeech implements Runnable {
        private TextView speechBubble;
        public HideSpeech(TextView speechBubble){
            this.speechBubble = speechBubble;
        }
        public void run() {
            speechBubble.setVisibility(View.INVISIBLE);
        }
    };

    private Runnable Speak = new Runnable() {
        @Override
        public void run() {
            try {
                int i = rng.nextInt(messages.length);
                handler.postDelayed(new ShowSpeech(speechBubbleLeft, messages[i][0]), 0);
                handler.postDelayed(new ShowSpeech(speechBubbleRight, messages[i][1]), 1000);
                handler.postDelayed(new HideSpeech(speechBubbleLeft), 6000);
                handler.postDelayed(new HideSpeech(speechBubbleRight), 6000);
            } finally {
                handler.postDelayed(Speak, (long) rng.nextFloat()*(maxInterval-minInterval)+minInterval);
            }
        }
    };

    public MainMenuFragment() {

    }

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: Called");
        super.onPause();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: Called");
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Called");
        onStopHandler();
        super.onDestroy();
    }
    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mainmenu, container, false);
        speechBubbleLeft = view.findViewById(R.id.speechleft);
        speechBubbleRight = view.findViewById(R.id.speechright);
        record_button = view.findViewById(R.id.record_button);
        golive_button = view.findViewById(R.id.golive);
        record_button.setOnClickListener((View v)-> startActivity(new Intent(getActivity(), RecordActivity.class)));
        golive_button.setOnClickListener((View v)-> {
            Intent intent = new Intent(getActivity(), LiveActivity.class);
            intent.putExtra("device",((MainActivity)getActivity()).device);
            startActivity(intent);
        });
        onStartHandler();
        return view;
    }

}