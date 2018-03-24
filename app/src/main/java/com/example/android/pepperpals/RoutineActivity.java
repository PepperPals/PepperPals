package com.example.android.pepperpals;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.Say;

public class RoutineActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = RoutineActivity.class.getSimpleName();

    int[] routineIds;

    String[] routineWords;

    ImageView routineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        // determine where we are in our routine
        ApplicationState globalState = (ApplicationState) getApplication();
        Log.i(TAG, "Currently at " + globalState.getCurrentRoutine() + " in our routine");

        routineIds = new int[]{R.drawable.routine_school, R.drawable.routine_math};
        routineWords = new String[]{
                "Let's go to the classroom",
                "Maths is fun. Let's listen to the teacher"
        };

        if (globalState.getCurrentRoutine() >= routineIds.length) {
            globalState.setCurrentRoutine(routineIds.length - 1);
        }

        routineView = (ImageView) findViewById(R.id.image_routine);
        routineView.setImageResource(routineIds[globalState.getCurrentRoutine()]);
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
        ApplicationState globalState = (ApplicationState) getApplication();
        doRoutine(qiContext, globalState.getCurrentRoutine());
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Lost robot focus");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "Robot focus refused");
    }

    private void doRoutine(QiContext qiContext, int routineNum) {
        String wordsToSpeak = routineWords[routineNum];
        Say say = SayBuilder.with(qiContext)
                .withText(wordsToSpeak)
                .build();
        say.async().run();
    }
}
