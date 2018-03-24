package com.example.android.pepperpals;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.GoToBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TransformBuilder;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.FreeFrame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.geometry.Transform;

public class RoutineActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = RoutineActivity.class.getSimpleName();

    private static final int CLASSROOM_ROUTINE = 0;

    private static final int RETURN_DELAY_MS = 10000;

    Routine routines[];

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

        routines = new Routine[]{new ClassroomRoutine(), new MathsRoutine()};

        if (globalState.getCurrentRoutine() >= routines.length) {
            globalState.setCurrentRoutine(routines.length - 1);
        }

        routineView = (ImageView) findViewById(R.id.image_routine);
        routineView.setImageResource(routines[globalState.getCurrentRoutine()].getImageResourceId());
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
        Future<Void> future = routines[globalState.getCurrentRoutine()].run(qiContext);

        final Context context = this;
        future.thenConsume(new Consumer<Future<Void>>() {
            @Override
            public void consume(Future<Void> future) throws Throwable {
                if (future.isSuccess()) {
                    Log.i(TAG, "Routine finished with success.");

                } else if (future.hasError()) {
                    Log.i(TAG, "Routine finished with error.");
                }

                // return to Routine after a delay
                Log.d(TAG, "Waiting for " + RETURN_DELAY_MS + " before transitioning to human action");
                final Intent intent = new Intent(context, HumanInteractionActivity.class);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Launching HumanInteractionActivity");
                        startActivity(intent);
                    }
                }, RETURN_DELAY_MS);
            }
        });
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Lost robot focus");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "Robot focus refused");
    }

    public static Say buildSay(QiContext qiContext, String text) {
        return SayBuilder.with(qiContext)
                .withText(text)
                .build();
    }

    public interface Routine {
        int getImageResourceId();

        Future<Void> run(final QiContext qiContext);
    }

    private static class ClassroomRoutine implements Routine {

        private static final String CLASSROOM_SPEECH = "Let's go to the classroom";

        @Override
        public int getImageResourceId() {
            return R.drawable.routine_school;
        }

        @Override
        public Future<Void> run(final QiContext qiContext) {
            Future<Void> sayFuture = RoutineActivity.buildSay(qiContext, CLASSROOM_SPEECH).async().run();

            // Chain a function to the future.
            return sayFuture.andThenCompose(new Function<Void, Future<Void>>() {
                @Override
                public Future<Void> execute(Void ignore) throws Throwable {
                    return move(qiContext);
                }
            });
        }

        private Future<Void> move(QiContext qiContext) {
            // get robot frame
            Actuation actuation = qiContext.getActuation();
            Frame robotFrame = actuation.robotFrame();

            // create transform
            Transform transform = TransformBuilder.create().fromXTranslation(1);

            // create destination
            Mapping mapping = qiContext.getMapping();
            FreeFrame destination = mapping.makeFreeFrame();
            destination.update(robotFrame, transform, System.currentTimeMillis());

            GoTo goTo = GoToBuilder.with(qiContext)
                    .withFrame(destination.frame())
                    .build();

            return goTo.async().run();
        }
    }

    private static class MathsRoutine implements Routine {

        private static final String MATHS_SPEECH = "Maths is fun. Let's listen to the teacher";

        @Override
        public int getImageResourceId() {
            return R.drawable.routine_math;
        }

        @Override
        public Future<Void> run(QiContext qiContext) {
            return RoutineActivity.buildSay(qiContext, MATHS_SPEECH).async().run();
        }
    }
}
