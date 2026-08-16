package com.mesalabs.ten.update.ota.utils;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.net.Uri;
import java.io.File;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.mesalabs.cerberus.utils.PropUtils;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.activity.home.MainActivity;
import com.mesalabs.ten.update.ota.receivers.AppReceiver;
import com.mesalabs.ten.update.utils.LogUtils;

/*
 * 십 Update
 *
 * Coded by BlackMesa123 @2021
 * Code snippets by MatthewBooth.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */

public class GeneralUtils {
    public final static String TAG = "GeneralUtils";

    public static void deleteFile(File file) {
        SystemUtils.shell("rm -f " + file.getAbsolutePath(), false);
    }

    public static boolean isNotificationAlarmSet(Context context) {
        Intent intent = new Intent(context, AppReceiver.class);
        intent.setAction(Constants.INTENT_START_UPDATE_CHECK);
        return PendingIntent.getBroadcast(context, 1673, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE) != null;
    }

    public static void setBackgroundCheck(Context context, boolean set) {
        scheduleNotification(context, set);
    }

    public static void scheduleNotification(Context context, boolean schedule) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AppReceiver.class);
        intent.setAction(Constants.INTENT_START_UPDATE_CHECK);
        int intentId = 1673;
        int intentFlag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        if (schedule) {
            int requestedInterval = PreferencesUtils.getBgServiceCheckFrequency();

            LogUtils.d(TAG, "Setting alarm for " + requestedInterval + " seconds");
            Calendar calendar = Calendar.getInstance();
            long time = calendar.getTimeInMillis() + requestedInterval * 1000;
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, intentId, intent, intentFlag));
        } else {
            if (alarmManager != null) {
                LogUtils.d(TAG, "Cancelling alarm");
                alarmManager.cancel(PendingIntent.getBroadcast(context, intentId, intent, intentFlag));
            }
        }
    }

    public static void setUpdateAvailability() {
        int currentVer = PropUtils.getInt(Constants.PROP_ROM_BUILD, 0);
        int manifestVer = PreferencesUtils.ROM.getBuildNumber();

        boolean available = currentVer < manifestVer;

        PreferencesUtils.Download.setUpdateAvailability(available);
        LogUtils.d(TAG, "Update Availability is " + available);
    }

    public static void setupDownloadCompletedNotification(Context context, boolean md5pass) {
        LogUtils.d(TAG, "Showing download completed notification");

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PreferencesUtils.getMainNotiChannelName());
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE // ✅
        );

        mBuilder.setContentTitle(context.getString(md5pass ? R.string.mesa_noti_download_complete : R.string.mesa_noti_download_failed))
                .setContentText(context.getString(md5pass ? R.string.mesa_noti_download_complete_desc : R.string.mesa_noti_download_failed_md5_desc))
                .setSmallIcon(md5pass ? R.drawable.mesa_ota_ic_noti_alert : R.drawable.mesa_ota_ic_noti_error)
                .setColor(context.getColor(R.color.mesa_ota_control_activated_color))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(Uri.parse(PreferencesUtils.getBgServiceNotificationSound()));

        if (PreferencesUtils.getBgServiceNotificationVibrate()) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE);
        } else {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        }

        mNotifyManager.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    public static void setupUpdateAvailableNotification(Context context) {
        LogUtils.d(TAG, "Showing update available notification");

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PreferencesUtils.getMainNotiChannelName());
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        mBuilder.setContentTitle(context.getString(R.string.mesa_noti_new_update))
                .setContentText(context.getString(R.string.mesa_noti_new_update_desc))
                .setSmallIcon(R.drawable.mesa_ota_ic_noti_new_update)
                .setColor(context.getColor(R.color.mesa_ota_control_activated_color))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(Uri.parse(PreferencesUtils.getBgServiceNotificationSound()));

        if (PreferencesUtils.getBgServiceNotificationVibrate()) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE);
        } else {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        }

        mNotifyManager.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    public static void setupDownloadInstallNotification(Context context, boolean installed) {
        LogUtils.d(TAG, "Showing install completed notification");

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, PreferencesUtils.getMainNotiChannelName());
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        mBuilder.setContentTitle(context.getString(installed ? R.string.mesa_noti_update_installed : R.string.mesa_noti_update_install_fail))
                .setContentText(installed ? context.getString(R.string.mesa_noti_update_installed_desc, PreferencesUtils.ROM.getVersionName()) : context.getString(R.string.mesa_noti_update_install_fail_desc))
                .setSmallIcon(installed ? R.drawable.mesa_ota_ic_noti_done : R.drawable.mesa_ota_ic_noti_error)
                .setColor(context.getColor(R.color.mesa_ota_control_activated_color))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(Uri.parse(PreferencesUtils.getBgServiceNotificationSound()));

        if (PreferencesUtils.getBgServiceNotificationVibrate()) {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_VIBRATE);
        } else {
            mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        }

        mNotifyManager.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

    public static void dismissNotifications(Context context) {
        LogUtils.d(TAG, "Dismissing notifications");

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancel(Constants.NOTIFICATION_ID);
    }
}
