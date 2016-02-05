package com.sss.magicwheel;

import android.app.Fragment;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sss.magicwheel.entity.WheelConfig;
import com.sss.magicwheel.entity.WheelDataItem;
import com.sss.magicwheel.manager.wheel.AbstractWheelLayoutManager;
import com.sss.magicwheel.manager.wheel.BottomWheelLayoutManager;
import com.sss.magicwheel.manager.wheel.TopWheelLayoutManager;
import com.sss.magicwheel.manager.WheelAdapter;
import com.sss.magicwheel.manager.WheelComputationHelper;
import com.sss.magicwheel.manager.decor.WheelFrameItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Kovalev
 * @since 01.02.2016.
 */
public final class FragmentWheelPage extends Fragment {

    private static final int DEFAULT_SECTOR_ANGLE_IN_DEGREE = 20;

    private boolean isWheelContainerInitialized;

    private final Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        // TODO: 03.02.2016 simplify for now considering container has 0 height

        WheelComputationHelper.initialize(createWheelConfig(0));

        final View rootView = inflater.inflate(R.layout.fragment_wheel_page_layout, container, false);
        final RecyclerView wheelContainerView = (RecyclerView) rootView.findViewById(R.id.wheel_container);

        initWheelContainer(wheelContainerView);

/*
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                if (!isWheelContainerInitialized) {
                    isWheelContainerInitialized = true;
                    final int fragmentContainerTopEdge = container.getTop();
                    WheelComputationHelper.initialize(createWheelConfig(fragmentContainerTopEdge));
                    initWheelContainer(wheelContainerView);
                }
            }
        });
*/



        return rootView;
    }

    private void initWheelContainer(RecyclerView wheelContainerView) {
        final BottomWheelLayoutManager layoutManager = new BottomWheelLayoutManager(WheelComputationHelper.getInstance(), null);
        wheelContainerView.setLayoutManager(layoutManager);
        wheelContainerView.setAdapter(createWheelAdapter(createDataSet()));
        addWheelItemDecorations(wheelContainerView);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutManager.setStartLayoutFromAdapterPosition(AbstractWheelLayoutManager.START_LAYOUT_FROM_ADAPTER_POSITION);
            }
        }, 2000);

    }

    private void addWheelItemDecorations(RecyclerView wheelContainerView) {
        wheelContainerView.addItemDecoration(new WheelFrameItemDecoration(getActivity()));
//        wheelContainerView.addItemDecoration(new WheelSectorRayItemDecoration(getActivity()));
//        wheelContainerView.addItemDecoration(new WheelSectorLeftEdgeColorItemDecoration(getActivity()));
    }

    private WheelAdapter createWheelAdapter(List<WheelDataItem> adapterDataSet) {
        return new WheelAdapter(getActivity(), adapterDataSet);
    }

    private List<WheelDataItem> createDataSet() {
        List<WheelDataItem> items = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            items.add(new WheelDataItem("item.Num [" + i + "]"));
        }
        return Collections.unmodifiableList(items);
    }

    private WheelConfig createWheelConfig(int fragmentContainerTopEdge) {
        final int screenHeight = WheelComputationHelper.getScreenDimensions(getActivity()).getHeight();

        final int yWheelCenterPosition = (screenHeight - fragmentContainerTopEdge) / 2 ;
        final PointF circleCenter = new PointF(0, yWheelCenterPosition);

        // TODO: 03.12.2015 Not good hardcoded values
        final int outerRadius =  (screenHeight - fragmentContainerTopEdge) / 2;
        final int innerRadius = outerRadius - 300;

        final double sectorAngleInRad = WheelComputationHelper.degreeToRadian(DEFAULT_SECTOR_ANGLE_IN_DEGREE);
        final WheelConfig.AngularRestrictions angularRestrictions = WheelConfig.AngularRestrictions
                .builder(sectorAngleInRad)
                .wheelEdgesAngularRestrictions(Math.PI / 2, -Math.PI / 2)
                .gapEdgesAngularRestrictions(Math.PI / 6, -Math.PI / 6)
                .build();

        return new WheelConfig(circleCenter, outerRadius, innerRadius, angularRestrictions);
    }

}
