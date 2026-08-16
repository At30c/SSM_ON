package com.mesalabs.ten.update.fragment.home;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.mesalabs.cerberus.ui.callback.OnSingleClickListener;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.activity.home.ChangelogActivity;
import com.mesalabs.ten.update.activity.home.FirmwareInfoActivity;
import com.mesalabs.ten.update.activity.home.MainActivity;
import com.mesalabs.ten.update.ota.ROMUpdate;
import com.mesalabs.ten.update.ota.tasks.GenerateRecoveryScript;
import com.mesalabs.ten.update.ota.utils.PreferencesUtils;
import com.mesalabs.ten.update.ui.widget.CardView;
import com.mesalabs.ten.update.ui.widget.ChangelogView;
import com.mesalabs.ten.update.ui.widget.UpdateStatusView;

import org.jetbrains.annotations.NotNull;

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

public class MainCardsFragment extends Fragment {
    private ROMUpdate mROMUpdate;
    private ROMUpdate.StubListener mROMStubListener = this::postROMUpdatesCheck;

    private FragmentActivity mActivity;
    private Context mContext;
    private boolean mIsDownloaded;

    private View mRootView;
    private LinearLayout mContainer;
    private UpdateStatusView updateStatusView;
    private ChangelogView changelogView;
    private TextView preInstallWarningText;

    private CardView unc;
    private CardView fic;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
        mContext = mActivity.getApplicationContext();

        mIsDownloaded = PreferencesUtils.Download.getDownloadFinished();

        //  init ROMUpdate
        mROMUpdate = new ROMUpdate(context, mROMStubListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.mesa_ota_fragment_maincards_mainactivity, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mRootView == null) {
            mRootView = getView();
        }
        if (mContext == null) {
            mContext = mActivity.getApplicationContext();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        mIsDownloaded = PreferencesUtils.Download.getDownloadFinished();
        if (mIsDownloaded) {
            updateStatusView.setUpdateStatus(ROMUpdate.STATE_DOWNLOADED);
            preInstallWarningText.setVisibility(View.VISIBLE);
            unc.setIconDrawable(getResources().getDrawable(R.drawable.mesa_ota_card_ic_install, getContext().getTheme()));
            unc.setTitleText(getString(R.string.mesa_ota_card_inst_title));
            unc.setDescText(getString(R.string.mesa_ota_card_inst_summary));
            unc.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    new GenerateRecoveryScript(mActivity).execute();
                }
            });
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder(mContainer, PropertyValuesHolder.ofFloat("alpha", 1, 0));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder(mContainer, PropertyValuesHolder.ofFloat("alpha", 0, 1));
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setDuration(LayoutTransition.APPEARING, 650);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        itemLayoutTransition.setDuration(LayoutTransition.CHANGE_APPEARING, 150);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGE_APPEARING, new FastOutSlowInInterpolator());
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, scaleDown);
        itemLayoutTransition.setDuration(LayoutTransition.DISAPPEARING, 80);

        mContainer = mRootView.findViewById(R.id.mesa_container_maincards_ota_mainactivity);
        mContainer.setLayoutTransition(itemLayoutTransition);

        updateStatusView = mRootView.findViewById(R.id.mesa_usv_maincards_ota_mainactivity);
        changelogView = mRootView.findViewById(R.id.mesa_cv_maincards_ota_mainactivity);
        changelogView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                startActivity(new Intent(mContext, ChangelogActivity.class));
            }
        });
        changelogView.setClickable(false);
        preInstallWarningText = mRootView.findViewById(R.id.mesa_preinstall_warning_ota_mainactivity);

        unc = mRootView.findViewById(R.id.mesa_card_all_ota_mainactivity);
        unc.setEnabled(false);

        fic = mRootView.findViewById(R.id.mesa_card_fwinfo_ota_mainactivity);
        fic.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.mesalabs.on.workshop");
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Bundle b = new Bundle();
                    b.putString("mesa_tb_category", "about_device");
                    intent.putExtras(b);
                    mContext.startActivity(intent);
                } else {
                    startActivity(new Intent(mContext, FirmwareInfoActivity.class));
                }
            }
        });

        if (mIsDownloaded) {
            updateStatusView.start(ROMUpdate.STATE_DOWNLOADED);
            changelogView.start();
            preInstallWarningText.setVisibility(View.VISIBLE);
            unc.setEnabled(true);
            unc.setIconDrawable(getResources().getDrawable(R.drawable.mesa_ota_card_ic_install, getContext().getTheme()));
            unc.setTitleText(getString(R.string.mesa_ota_card_inst_title));
            unc.setDescText(getString(R.string.mesa_ota_card_inst_summary));
            unc.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    new GenerateRecoveryScript(mActivity).execute();
                }
            });
        } else {
            updateStatusView.start(ROMUpdate.STATE_CHECKING);
            checkForROMUpdates();
        }

    }

    public void checkForROMUpdates() {
        if (updateStatusView.getCheckingStatus() != ROMUpdate.STATE_CHECKING)
            updateStatusView.setUpdateStatus(ROMUpdate.STATE_CHECKING);
        changelogView.stop();
        unc.setEnabled(false);
        unc.setDescText(null);
        unc.setOnClickListener(null);
        mROMUpdate.checkUpdates(true);
    }

    private void postROMUpdatesCheck(int status) {
        ((MainActivity) mActivity).animateRefreshButton(true);
        updateStatusView.setUpdateStatus(status);

        if (PreferencesUtils.Download.getUpdateAvailability()) {
            changelogView.start();
            unc.setEnabled(true);
            unc.setDescText(getString(R.string.mesa_ota_card_dwinst_summary));
            unc.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    ((MainActivity) mActivity).onPreROMUpdateDownload();
                }
            });
        }
    }

}