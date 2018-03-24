package com.example.android.firstpepper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

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

public class MainActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    // Store the Animate action.
    private Animate animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext) // Create the builder with the context.
                .withResources(R.raw.raise_right_hand_b002) // Set the animation resource.
                .build(); // Build the animation.

        // Create an animate action.
        animate = AnimateBuilder.with(qiContext) // Create the builder with the context.
                .withAnimation(animation) // Set the animation.
                .build(); // Build the animate action.

        Future<Void> animateFuture = animate.async().run();
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
}
