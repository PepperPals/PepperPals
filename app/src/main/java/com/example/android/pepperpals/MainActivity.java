package com.example.android.pepperpals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Say;
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
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

/*        // Find humans around when refresh button clicked.
        Button refreshButton = (Button) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (qiContext != null) {
                    findHumansAround();
                }
            }
        });*/
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

    private void greetHuman(Human human) {
        Log.i(TAG, "Greeting human");

        //Intent intent = new Intent(this, HumanInteractionActivity.class);
        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
    }

    private void findHumansAround() {
        // Get the humans around the robot.
        Future<List<Human>> humansAroundFuture = humanAwareness.async().getHumansAround();

        humansAroundFuture.andThenConsume(new Consumer<List<Human>>() {
            @Override
            public void consume(List<Human> humansAround) throws Throwable {
                Log.i(TAG, humansAround.size() + " human(s) around.");
                retrieveCharacteristics(humansAround);
            }
        });
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
