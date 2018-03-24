package com.example.android.pepperpals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.TransformTime;
import com.aldebaran.qi.sdk.object.human.AttentionState;
import com.aldebaran.qi.sdk.object.human.ExcitementState;
import com.aldebaran.qi.sdk.object.human.Gender;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.human.PleasureState;
import com.aldebaran.qi.sdk.object.human.SmileState;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Store the HumanAwareness service.
    private HumanAwareness humanAwareness;

    // The QiContext provided by the QiSDK.
    private QiContext qiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApplicationState globalState = (ApplicationState) getApplication();
        if (!globalState.isInitialised()) {
            Log.i(TAG, "Initialising application state");
            globalState.setCurrentRoutine(0);
            globalState.setInitialised(true);
        }

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
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

        // Store the provided QiContext.
        this.qiContext = qiContext;

        startObserving(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Lost robot focus");

        stopObserving();

        // Remove the QiContext.
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "Robot focus refused");
    }

    /**
     * Start the observation.
     *
     * @param qiContext the qiContext
     */
    public void startObserving(QiContext qiContext) {
        // Get the HumanAwareness service.
        humanAwareness = qiContext.getHumanAwareness();

        List<Human> humansAround = humanAwareness.getHumansAround();
        retrieveCharacteristics(humansAround);

        // Update the observed human when the humans around change.
        humanAwareness.setOnHumansAroundChangedListener(new HumanAwareness.OnHumansAroundChangedListener() {
            @Override
            public void onHumansAroundChanged(List<Human> humansAround) {
                Log.i(TAG, humansAround.size() + " human(s) around.");
                retrieveCharacteristics(humansAround);

                if (humansAround.size() > 0) {
                    greetHuman(humansAround.get(0));
                }
            }
        });
    }

    /**
     * Stop the observation.
     */
    public void stopObserving() {
        // Remove listener on HumanAwareness.
        if (humanAwareness != null) {
            humanAwareness.setOnHumansAroundChangedListener(null);
            humanAwareness = null;
        }
    }

    private void greetHuman(final Human human) {
        Log.i(TAG, "Greeting human");

        final AttentionState attentionState = human.getAttention();
        Log.d(TAG, "Human attention state: " + attentionState);

        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext)
                //.withResources(R.raw.raise_right_hand_b002)
                .withResources(R.raw.raise_right_hand_b003)
                .build();

        // Create an animate action.
        Animate animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();

        Say greeting = SayBuilder.with(qiContext)
                .withText("Hello Codor")
                .build();

        Log.d(TAG, "do greeting");
        Future<Void> animateFuture = animate.async().run();
        Future<Void> greetingFuture = greeting.async().run();

        greetingFuture.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                Future<Void> walkFuture = walkToHuman(human.getHeadFrame());
                walkFuture.thenConsume(new Consumer<Future<Void>>() {
                    @Override
                    public void consume(Future<Void> future) throws Throwable {
                        Intent nextActivity;
                        //nextActivity = new Intent(this, RoutineActivity.class);
                        if (AttentionState.LOOKING_AT_ROBOT.equals(attentionState)) {
                            Log.d(TAG, "Switch to routine");
                            nextActivity = new Intent(MainActivity.this, HumanInteractionActivity.class);
                        } else {
                            Log.d(TAG, "Switch to challenge to attract attention");
                            nextActivity = new Intent(MainActivity.this, ChallengeActivity.class);
                        }

                        startActivity(nextActivity);
                    }
                });
            }
        });
    }

    private Future<Void> walkToHuman(Frame humanFrame) {
        // get robot frame
        Actuation actuation = qiContext.getActuation();
        Frame robotFrame = actuation.robotFrame();

        // Get the TransformTime between the human frame and the robot frame.
        TransformTime transformTime = humanFrame.computeTransform(robotFrame);
        // Get the transform.
        Transform transform = transformTime.getTransform();


        // create destination
        Mapping mapping = qiContext.getMapping();
        FreeFrame destination = mapping.makeFreeFrame();
        destination.update(robotFrame, transform, System.currentTimeMillis());

        GoTo goTo = GoToBuilder.with(qiContext)
                .withFrame(destination.frame())
                .build();

        return goTo.async().run();
    }

    private void retrieveCharacteristics(final List<Human> humans) {
        for (int i = 0; i < humans.size(); i++) {
            // Get the human.
            Human human = humans.get(i);

            // Get the characteristics.
            Integer age = human.getEstimatedAge().getYears();
            Gender gender = human.getEstimatedGender();
            PleasureState pleasureState = human.getEmotion().getPleasure();
            ExcitementState excitementState = human.getEmotion().getExcitement();
            SmileState smileState = human.getFacialExpressions().getSmile();
            AttentionState attentionState = human.getAttention();

            // Display the characteristics.
            Log.i(TAG, "----- Human " + i + " -----");
            Log.i(TAG, "Age: " + age + " year(s)");
            Log.i(TAG, "Gender: " + gender);
            Log.i(TAG, "Pleasure state: " + pleasureState);
            Log.i(TAG, "Excitement state: " + excitementState);
            Log.i(TAG, "Smile state: " + smileState);
            Log.i(TAG, "Attention state: " + attentionState);
        }
    }
}
