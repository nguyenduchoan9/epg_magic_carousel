package com.sss.magicwheel.wheel.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.sss.magicwheel.wheel.entity.WheelConfig;
import com.sss.magicwheel.wheel.entity.WheelRotationDirection;
import com.sss.magicwheel.wheel.WheelComputationHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Kovalev
 * @since 11.02.2016.
 */
@Deprecated
final class WheelStartupAnimationHelper {

    private static final int TOP_WHEEL_APPEARING_ANIMATION_DURATION = 1000;
    private static final int FROM_TOP_GAP_EDGE_TO_BOTTOM_GAP_EDGE_BOTTOM_WHEEL_ANIMATION_DURATION = 1000;

    public enum WheelStartupAnimationStatus {
        Start, InProgress, Finished
    }

    public interface OnWheelStartupAnimationListener {
        void onAnimationUpdate(WheelStartupAnimationStatus animationStatus);
    }

    private final List<OnWheelStartupAnimationListener> animationListeners = new ArrayList<>();

    private final WheelComputationHelper computationHelper;
    private final WheelConfig.AngularRestrictions angularRestrictions;

    private final AbstractWheelContainerRecyclerView topWheelContainer;
    private final AbstractWheelContainerRecyclerView bottomWheelContainer;

    // TODO: 16.02.2016 has to be false initially
    private boolean isStartupAnimationPlayed = false;

    public WheelStartupAnimationHelper(WheelComputationHelper computationHelper,
                                       AbstractWheelContainerRecyclerView topWheelContainer,
                                       AbstractWheelContainerRecyclerView bottomWheelContainer) {
        this.computationHelper = computationHelper;
        this.angularRestrictions = computationHelper.getWheelConfig().getAngularRestrictions();
        this.topWheelContainer = topWheelContainer;
        this.bottomWheelContainer = bottomWheelContainer;

//        setRotationPivotForContainer(topWheelContainer);
//        setRotationPivotForContainer(bottomWheelContainer);
//        setInitialTopWheelRotation();
    }

    public void playWheelStartupAnimation() {
//        if (!isStartupAnimationPlayed) {
//            isStartupAnimationPlayed = true;
//            AnimatorSet wheelStartupAnimator = new AnimatorSet();
//            wheelStartupAnimator.playTogether(
//                    topWheelContainer.getLayoutManager().createWheelStartupAnimator(),
//                    bottomWheelContainer.getLayoutManager().createWheelStartupAnimator()
//            );
//            wheelStartupAnimator.start();
//        }
    }

    public boolean isStartupAnimationPlayed() {
        return isStartupAnimationPlayed;
    }

    public void addAnimationListener(OnWheelStartupAnimationListener animationListener) {
        animationListeners.add(animationListener);
    }

    public void removeAnimationListener(OnWheelStartupAnimationListener animationListener) {
        animationListeners.remove(animationListener);
    }

    private void setRotationPivotForContainer(AbstractWheelContainerRecyclerView wheelContainer) {
        final PointF circleCenterRelToRecyclerView = computationHelper.getWheelConfig().getCircleCenterRelToRecyclerView();
        wheelContainer.setPivotX(circleCenterRelToRecyclerView.x);
        wheelContainer.setPivotY(circleCenterRelToRecyclerView.y);
    }

    private void setInitialTopWheelRotation() {
        final float rotationAngleInDegree = getInitialTopWheelRotationAngleInDegree();
        setInitialRotationForWheelContainer(topWheelContainer, rotationAngleInDegree, WheelRotationDirection.Anticlockwise);
    }

    private float getInitialTopWheelRotationAngleInDegree() {
        final double rotationAngleInRad = angularRestrictions.getWheelTopEdgeAngleRestrictionInRad()
                - angularRestrictions.getGapAreaTopEdgeAngleRestrictionInRad();
        return (float) WheelComputationHelper.radToDegree(rotationAngleInRad);
    }

    /**
     * @param wheelContainer - either top or bottom wheel part
     */
    private void setInitialRotationForWheelContainer(AbstractWheelContainerRecyclerView wheelContainer,
                                                     float absRotationAngleInDegree,
                                                     WheelRotationDirection rotationDirection) {
        final int rotationSign = rotationDirection == WheelRotationDirection.Clockwise ? +1 : -1;
        final float rotationAngleWithSign = rotationSign * Math.abs(absRotationAngleInDegree);
        wheelContainer.setRotation(rotationAngleWithSign);
    }

    private Animator createWheelStartupAnimator() {
        final AnimatorSet startupWheelAnimator = new AnimatorSet();

        final TimeInterpolator timeInterpolator = new AccelerateDecelerateInterpolator();
        final Animator bottomWheelAnimatorFromTopWheelEdgeToTopGapEdge = createBottomWheelAnimatorFromTopWheelEdgeToTopGapEdge(timeInterpolator);
        final Animator topWheelStartupAnimator = createTopWheelStartupAnimator(timeInterpolator);

        final ObjectAnimator bottomWheelAnimatorFromTopGapEdgeToBottomGapEdge = createBottomWheelAnimatorFromTopGapEdgeToBottomGapEdge();
        startupWheelAnimator.playSequentially(
                bottomWheelAnimatorFromTopWheelEdgeToTopGapEdge,
                bottomWheelAnimatorFromTopGapEdgeToBottomGapEdge
        );
        startupWheelAnimator.playTogether(topWheelStartupAnimator, bottomWheelAnimatorFromTopWheelEdgeToTopGapEdge);

        startupWheelAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                listenersFireOnAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                topWheelContainer.setIsCutGapAreaActivated(true);
                listenersFireOnAnimationEnd();
            }
        });

        bottomWheelAnimatorFromTopGapEdgeToBottomGapEdge.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                listenersFireOnAnimationInProgress();
            }
        });

        return startupWheelAnimator;
    }

    private void listenersFireOnAnimationStart() {
        for (OnWheelStartupAnimationListener animationListener : animationListeners) {
            animationListener.onAnimationUpdate(WheelStartupAnimationStatus.Start);
        }
    }

    private void listenersFireOnAnimationInProgress() {
        for (OnWheelStartupAnimationListener animationListener : animationListeners) {
            animationListener.onAnimationUpdate(WheelStartupAnimationStatus.InProgress);
        }
    }

    private void listenersFireOnAnimationEnd() {
        for (OnWheelStartupAnimationListener animationListener : animationListeners) {
            animationListener.onAnimationUpdate(WheelStartupAnimationStatus.Finished);
        }
    }

    private Animator createTopWheelStartupAnimator(TimeInterpolator timeInterpolator) {
        // top wheel part rotation
        final float topWheelStartRotationAngle = topWheelContainer.getRotation();
        final float topWheelEndRotationAngle = topWheelStartRotationAngle + getInitialTopWheelRotationAngleInDegree();
        final ObjectAnimator topWheelRotateAnimator = ObjectAnimator.ofFloat(
                topWheelContainer,
                View.ROTATION,
                topWheelStartRotationAngle, topWheelEndRotationAngle
        );

        topWheelRotateAnimator.setInterpolator(timeInterpolator);
        topWheelRotateAnimator.setDuration(TOP_WHEEL_APPEARING_ANIMATION_DURATION);

        return topWheelRotateAnimator;
    }

    private Animator createBottomWheelAnimatorFromTopWheelEdgeToTopGapEdge(TimeInterpolator timeInterpolator) {
        float endRotationAngleInDegree = getRotationAngleInDegreeForFirstStageBottomWheelAnimation();

        final ObjectAnimator res = ObjectAnimator.ofFloat(
                bottomWheelContainer,
                View.ROTATION,
                bottomWheelContainer.getRotation(),
                endRotationAngleInDegree
        );

        res.setInterpolator(timeInterpolator);
        res.setDuration(TOP_WHEEL_APPEARING_ANIMATION_DURATION);

        return res;
    }

    private ObjectAnimator createBottomWheelAnimatorFromTopGapEdgeToBottomGapEdge() {
        float startRotationAngleInDegree = getRotationAngleInDegreeForFirstStageBottomWheelAnimation();
        final double rotationAngleInRad = angularRestrictions.getGapAreaTopEdgeAngleRestrictionInRad()
                - angularRestrictions.getGapAreaBottomEdgeAngleRestrictionInRad();
        final float endRotationAngleInDegree = startRotationAngleInDegree
                + (float) WheelComputationHelper.radToDegree(rotationAngleInRad);

        final ObjectAnimator res = ObjectAnimator.ofFloat(
                bottomWheelContainer,
                View.ROTATION,
                startRotationAngleInDegree,
                endRotationAngleInDegree
        );

        res.setInterpolator(new LinearInterpolator());
        res.setDuration(FROM_TOP_GAP_EDGE_TO_BOTTOM_GAP_EDGE_BOTTOM_WHEEL_ANIMATION_DURATION);

        return res;
    }

    /**
     * First stage is animation from top wheel angular restriction to top gap edge
     * angular restriction.
     */
    private float getRotationAngleInDegreeForFirstStageBottomWheelAnimation() {
        final double rotationAngleInRad = angularRestrictions.getWheelTopEdgeAngleRestrictionInRad()
                - angularRestrictions.getGapAreaTopEdgeAngleRestrictionInRad();
        return (float) WheelComputationHelper.radToDegree(rotationAngleInRad);
    }

}