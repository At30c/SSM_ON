package com.mesalabs.ten.update.activity.aboutpage;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import com.mesalabs.cerberus.base.BaseAppBarActivity;
import com.mesalabs.cerberus.ui.callback.OnSingleClickListener;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.ui.creditspage.CreditsListViewModel;
import com.mesalabs.ten.update.ui.creditspage.CreditsPageListAdapter;
import com.mesalabs.ten.update.ui.creditspage.CreditsPageListItemDecoration;
import com.samsung.android.ui.recyclerview.widget.SeslLinearLayoutManager;
import com.samsung.android.ui.recyclerview.widget.SeslRecyclerView;

/*
 * 십 Update
 *
 * Coded by BlackMesa123 @2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

public class CreditsActivity extends BaseAppBarActivity {
    private TextView mSubheaderText;
    private SeslRecyclerView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mesa_layout_creditsactivity);

        appBar.setTitleText(getString(R.string.mesa_credits));
        appBar.setHomeAsUpButton(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                onBackPressed();
            }
        });

        initListView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mSubheaderText.setPadding(mSubheaderText.getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.sesl_list_divider_inset) - appBar.getAppBarLayout().getPaddingBottom(), mSubheaderText.getPaddingRight(), mSubheaderText.getPaddingBottom());
    }

    @Override
    protected boolean getIsAppBarExpanded() {
        return false;
    }

    private void initListView() {
        mListView = findViewById(R.id.mesa_recyclerview_creditsactivity);

        SeslRecyclerView.Adapter adapter = new CreditsPageListAdapter(this, getCreditsList());
        CreditsPageListItemDecoration decoration = new CreditsPageListItemDecoration(this);
        TypedValue divider = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.listDivider, divider, true);

        mListView.setLayoutManager(new SeslLinearLayoutManager(mContext));
        mListView.setAdapter(adapter);

        mListView.addItemDecoration(decoration);
        decoration.setDivider(mContext.getDrawable(divider.resourceId));
        mListView.setItemAnimator(null);
        mListView.seslSetFillBottomEnabled(true);
        mListView.seslSetLastOutlineStrokeEnabled(false);
    }

    private List<CreditsListViewModel> getCreditsList() {
        List<CreditsListViewModel> modelList = new ArrayList<>();

        TypedArray libIcons = getResources().obtainTypedArray(R.array.mesa_creditspage_lib_icon);
        String [] libNames = getResources().getStringArray(R.array.mesa_creditspage_lib_title);
        String [] libDescs = getResources().getStringArray(R.array.mesa_creditspage_lib_desc);
        String [] libWebLinks = getResources().getStringArray(R.array.mesa_creditspage_lib_web);

        for (int i = 0; i < libNames.length; i++) {
            modelList.add(new CreditsListViewModel(getResources().getDrawable(libIcons.getResourceId(i, 0), null),
                    libNames[i],
                    libDescs[i],
                    libWebLinks[i]));
        }
        modelList.add(new CreditsListViewModel());

        return modelList;
    }

    public Context getContext() {
        return mContext;
    }

    public SeslRecyclerView getListView() {
        return mListView;
    }

}
