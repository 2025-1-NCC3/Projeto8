package br.fecap.pi.ubersafestart.utils;

import android.content.Context;
import android.view.View;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class ConfettiManager {

    public static void showConfetti(KonfettiView konfettiView) {
        konfettiView.build()
                .addColors(0xFF2979FF)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(1000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(konfettiView.getWidth() / 2f, konfettiView.getHeight() / 3f)
                .burst(100);
    }

    public static void showSuccessConfetti(KonfettiView konfettiView) {
        konfettiView.build()
                .addColors(0xFF2979FF, 0xFF2962FF, 0xFF448AFF)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(konfettiView.getWidth() / 2f, konfettiView.getHeight() / 3f)
                .burst(200);
    }
}