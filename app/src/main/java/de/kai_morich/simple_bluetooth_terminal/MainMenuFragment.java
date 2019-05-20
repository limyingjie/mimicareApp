package de.kai_morich.simple_bluetooth_terminal;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

public class MainMenuFragment extends Fragment {

    private String[][] messages = {
            {"The weather\nlooks good~", "Let's go for\na walk shall we?"},
            {"Ahhh...\nI am starving...", "Shall we go\nget some food?"},
            {"Cheese Burger!!!","Control yourself\nplease!"}
    };

    private int minInterval = 10000;
    private int maxInterval = 15000;
    private Handler handler;
    private Random rng = new Random();
    private TextView speechBubbleLeft;
    private TextView speechBubbleRight;

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
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
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
        handler = new Handler();
        handler.postDelayed(Speak, (long) rng.nextFloat()*(maxInterval-minInterval)+5000);
        return view;
    }
}