package com.example.android.pepperpals;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

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

public class HumanInteractionActivity extends AppCompatActivity implements RobotLifecycleCallbacks {

    private static final String TAG = HumanInteractionActivity.class.getSimpleName();

    // Store the Animate action.
    private Animate animate;

    private ImageButton questionButton;

    private ImageButton bathroomButton;

    private ImageButton drinkButton;

    private QiContext qiContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human_interaction);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        questionButton = (ImageButton) findViewById(R.id.request_question);
        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAndSay("We haven't understood.");
                Toast.makeText(HumanInteractionActivity.this, "Question",
                        Toast.LENGTH_SHORT).show();
            }
        });

        bathroomButton = (ImageButton) findViewById(R.id.request_bathroom);
        bathroomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAndSay("Can we go to the bathroom?");
                Toast.makeText(HumanInteractionActivity.this, "Bathroom",
                        Toast.LENGTH_SHORT).show();
            }
        });

        drinkButton = (ImageButton) findViewById(R.id.request_drink);
        drinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAndSay("Can we have a drink of water");
                Toast.makeText(HumanInteractionActivity.this, "Drink",
                        Toast.LENGTH_SHORT).show();
            }
        });
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
        this.qiContext = qiContext;

        // Create an animation.
        Animation animation = AnimationBuilder.with(qiContext)
                //.withResources(R.raw.raise_right_hand_b002)
                .withResources(R.raw.raise_right_hand_b003)
                .build();

        // Create an animate action.
        animate = AnimateBuilder.with(qiContext)
                .withAnimation(animation)
                .build();
    }

    @Override
    public void onRobotFocusLost() {
        Log.d(TAG, "Lost robot focus");
        this.qiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.d(TAG, "Robot focus refused");
    }

    private void playAndSay(final String text) {
        if (null != qiContext) {
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

                    Say say = SayBuilder.with(qiContext)
                            .withText(text)
                            .build();
                    say.async().run();
                }
            });
        }
    }
}
