package com.example.android.pepperpals;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
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

    private static final int RETURN_DELAY_MS = 5000;

    ImageView challengeView;

    private Animate animateDog;

    private Animate animateElephant;

    private Animate animateFeline;

    private Animate animateGorilla;

    private Animate animateMouse;

    private Animate[] animations;

    private int[] logos;

    private int soundsIds[];

    private MediaPlayer mediaPlayer;

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

        soundsIds = new int[]{
                R.raw.dog_sound,
                R.raw.elephant_sound,
                R.raw.cat_sound,
                R.raw.gorilla_sound,
                R.raw.mouse_sound
        };

        // pick a random image and associated animation
        Random random = new Random();
        selectedChallenge = random.nextInt(logos.length);
        Log.d(TAG, "Selected challenge: " + selectedChallenge);
        challengeView.setImageResource(logos[selectedChallenge]);
    }

    @Override
    protected void onDestroy() {
        // Unregister all the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int id = soundsIds[selectedChallenge];
        Log.d(TAG, "onStart - load MediaPlayer, for sound " + selectedChallenge + ", with ID " + id);
        mediaPlayer = MediaPlayer.create(this, id);
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

        Log.d(TAG, "Start challenge animation: " + selectedChallenge);
        Animate animate = animations[selectedChallenge];

        // Set an on started listener to the animate action.
        animate.setOnStartedListener(new Animate.OnStartedListener() {
            @Override
            public void onStarted() {
                Log.i(TAG, "Animated started. Will play audio");
                mediaPlayer.start();
            }
        });

        Future<Void> animateFuture = animate.async().run();

        final Context context = this;
        animateFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    Log.i(TAG, "Animation finished with success.");

                } else if (future.hasError()) {
                    Log.i(TAG, "Animation finished with error.");
                }

                // return to Routine after a delay
                Log.d(TAG, "Waiting for " + RETURN_DELAY_MS + " before returning to Routine");
                final Intent intent = new Intent(context, RoutineActivity.class);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Launching RoutineActivity");
                        startActivity(intent);
                    }
                }, RETURN_DELAY_MS);
            }
        });
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
