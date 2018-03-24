package com.example.android.pepperpals;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;

import java.util.Random;

public class ChallengeActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = ChallengeActivity.class.getSimpleName();

    ImageView challengeView;

    private Animate animateDog;

    private Animate animateElephant;

    private Animate animateFeline;

    private Animate animateGorilla;

    private Animate animateMouse;

    private Animate[] animations;

    private int[] logos;

    private int selectedChallenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        challengeView = (ImageView) findViewById(R.id.image_challenge);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        logos = new int[]{
                R.drawable.challenge_dog,
                R.drawable.challenge_elephant,
                R.drawable.challenge_cat,
                R.drawable.challenge_gorilla,
                R.drawable.challenge_mouse};

        // pick a random image and associated animation
        Random random = new Random();
        selectedChallenge = random.nextInt(logos.length);
        Log.d(TAG, "Selected challenge: "+selectedChallenge);
        challengeView.setImageResource(logos[selectedChallenge]);
    }

    @Override
    protected void onDestroy() {
        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        Log.d(TAG, "Gained robot focus");

        animateDog = loadAnimation(qiContext, R.raw.dog_a001);
        animateElephant = loadAnimation(qiContext, R.raw.elephant_a001);
        animateFeline = loadAnimation(qiContext, R.raw.feline_a001);
        animateGorilla = loadAnimation(qiContext, R.raw.gorilla_a001);
        animateMouse = loadAnimation(qiContext, R.raw.mouse_a001);

        animations = new Animate[]{animateDog, animateElephant, animateFeline, animateGorilla, animateMouse};

        Log.d(TAG, "Start challenge animation: "+selectedChallenge);
        animations[selectedChallenge].async().run();
    }

    private Animate loadAnimation(QiContext qiContext, int id) {
        Animation animation = AnimationBuilder.with(qiContext)
                .withResources(id)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();
        return animate;
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Lost robot focus");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "Robot focus refused");
    }
}
