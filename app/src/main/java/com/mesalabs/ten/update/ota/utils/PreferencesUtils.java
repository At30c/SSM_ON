package com.mesalabs.ten.update.ota.utils;

import android.content.Context;

import java.io.File;

import com.mesalabs.cerberus.utils.SharedPreferencesUtils;

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

public class PreferencesUtils {
    public static String KEY_BGSERVICE = "mesa_bgservice_pref";
    public static String KEY_BGSERVICE_CHECK_FREQ = "mesa_bgservice_freq_pref";
    public static String KEY_BGSERVICE_NOTI_SOUND = "mesa_bgservice_noti_sound_pref";
    public static String KEY_BGSERVICE_NOTI_VIBRATE = "mesa_bgservice_noti_vibrate_pref";
    public static String KEY_IS_ROOT_AVAILABLE = "mesa_is_root_available";
    public static String KEY_IS_APP_UPDATE_AVAILABLE = "mesa_is_app_update_available";
    public static String KEY_MAIN_NOTI_CHANNEL_NAME = "mesa_main_noti_channel_name";
    public static String KEY_NETWORK_TYPE = "mesa_networktype_pref";
    public static String KEY_REBOOT_FOR_INSTALL = "mesa_reboot_for_install";

    private static SharedPreferencesUtils sp = SharedPreferencesUtils.getInstance();


    public static boolean getIsRootAvailable() {
        return sp.getBoolean(KEY_IS_ROOT_AVAILABLE, false);
    }

    public static void setIsRootAvailable(boolean value) {
        sp.put(KEY_IS_ROOT_AVAILABLE, value);
    }

    public static boolean getIsAppUpdateAvailable() {
        return sp.getBoolean(KEY_IS_APP_UPDATE_AVAILABLE, false);
    }

    public static void setIsAppUpdateAvailable(boolean value) {
        sp.put(KEY_IS_APP_UPDATE_AVAILABLE, value);
    }

    public static boolean getRebootForInstall() {
        return sp.getBoolean(KEY_REBOOT_FOR_INSTALL, false);
    }

    public static void setRebootForInstall(boolean value) {
        sp.put(KEY_REBOOT_FOR_INSTALL, value);
    }

    public static String getMainNotiChannelName() {
        return sp.getString(KEY_MAIN_NOTI_CHANNEL_NAME, "");
    }

    public static void setMainNotiChannelName(String value) {
        sp.put(KEY_MAIN_NOTI_CHANNEL_NAME, value);
    }

    public static int getNetworkType() {
        return Integer.parseInt(sp.getString(KEY_NETWORK_TYPE, "1"));
    }

    public static boolean getBgServiceEnabled() {
        return sp.getBoolean(KEY_BGSERVICE, true);
    }

    public static void setBgServiceEnabled(boolean value) {
        sp.put(KEY_BGSERVICE, value);
    }

    public static int getBgServiceCheckFrequency() {
        return Integer.parseInt(sp.getString(KEY_BGSERVICE_CHECK_FREQ, "86400"));
    }

    public static void setBgServiceCheckFrequency(String value) {
        sp.put(KEY_BGSERVICE_CHECK_FREQ, value);
    }

    public static String getBgServiceNotificationSound() {
        return sp.getString(KEY_BGSERVICE_NOTI_SOUND, "content://settings/system/notification_sound");
    }

    public static void setBgServiceNotificationSound(String value) {
        sp.put(KEY_BGSERVICE_NOTI_SOUND, value);
    }

    public static boolean getBgServiceNotificationVibrate() {
        return sp.getBoolean(KEY_BGSERVICE_NOTI_VIBRATE, true);
    }

    public static void setBgServiceNotificationVibrate(boolean value) {
        sp.put(KEY_BGSERVICE_NOTI_VIBRATE, value);
    }


    public static class Download {
        private static final String PREF_NAME = "OnUpdate_DownloadData";

        private static String AVAILABILITY = "update_availability";
        private static final String DOWNLOAD_RUNNING = "download_running";
        private static String DOWNLOAD_ID = "download_id";
        private static String IS_DOWNLOAD_FINISHED = "is_download_finished";

        private static SharedPreferencesUtils sp = SharedPreferencesUtils.getInstance(PREF_NAME);


        public static void clean() {
            sp.put(AVAILABILITY, false);
            sp.put(DOWNLOAD_RUNNING, false);
            sp.put(DOWNLOAD_ID, 0);
            sp.put(IS_DOWNLOAD_FINISHED, false);
        }


        public static boolean getDownloadFinished() {
            return sp.getBoolean(IS_DOWNLOAD_FINISHED, false);
        }

        public static void setDownloadFinished(boolean value) {
            sp.put(IS_DOWNLOAD_FINISHED, value);
        }

        public static int getDownloadID() {
            return sp.getInt(DOWNLOAD_ID, 0);
        }

        public static void setDownloadID(int value) {
            sp.put(DOWNLOAD_ID, value);
        }

        public static boolean getIsDownloadOnGoing() {
            return sp.getBoolean(DOWNLOAD_RUNNING, false);
        }

        public static void setIsDownloadOnGoing(boolean value) {
            sp.put(DOWNLOAD_RUNNING, value);
        }

        public static boolean getUpdateAvailability() {
            return sp.getBoolean(AVAILABILITY, false);
        }

        public static void setUpdateAvailability(boolean value) {
            sp.put(AVAILABILITY, value);
        }

    }

    public static class ROM {
        private static final String PREF_NAME = "OnUpdate_UpdateData";
        private static String DEF_VALUE = "null";
        
        private static String NAME = "rom_name";
        private static String VERSION_NAME = "rom_version_name";
        private static String BUILD_NUMBER = "rom_build_number";
        private static String DOWNLOAD_URL = "rom_download_url";
        private static String MD5 = "rom_md5";
        private static String CHANGELOG_HEADER = "rom_changelog_header_img";
        private static String CHANGELOG = "rom_changelog";
        private static String ANDROID = "rom_android_ver";
        private static String ONEUI = "rom_oneui_ver";
        private static String WEBSITE = "rom_website";
        private static String FILESIZE = "rom_filesize";

        private static SharedPreferencesUtils sp = SharedPreferencesUtils.getInstance(PREF_NAME);


        public static void clean() {
            sp.put(NAME, DEF_VALUE);
            sp.put(VERSION_NAME, DEF_VALUE);
            sp.put(BUILD_NUMBER, 0);
            sp.put(DOWNLOAD_URL, DEF_VALUE);
            sp.put(MD5, DEF_VALUE);
            sp.put(CHANGELOG, DEF_VALUE);
            sp.put(CHANGELOG_HEADER, DEF_VALUE);
            sp.put(ANDROID, DEF_VALUE);
            sp.put(ONEUI, DEF_VALUE);
            sp.put(WEBSITE, DEF_VALUE);
            sp.put(FILESIZE, 0L);
        }


        public static String getRomName() {
            return sp.getString(NAME, DEF_VALUE);
        }

        public static String getVersionName() {
            return sp.getString(VERSION_NAME, DEF_VALUE);
        }

        public static int getBuildNumber() {
            return sp.getInt(BUILD_NUMBER, 0);
        }

        public static String getDownloadUrl() {
            return sp.getString(DOWNLOAD_URL, DEF_VALUE);
        }

        public static String getMd5() {
            return sp.getString(MD5, DEF_VALUE);
        }

        public static String getChangelogHeaderImgUrl() {
            return sp.getString(CHANGELOG_HEADER, DEF_VALUE);
        }

        public static String getChangelogUrl() {
            return sp.getString(CHANGELOG, DEF_VALUE);
        }

        public static String getAndroidVersion() {
            return sp.getString(ANDROID, DEF_VALUE);
        }

        public static String getOneUIVersion() {
            return sp.getString(ONEUI, DEF_VALUE);
        }

        public static String getWebsite() {
            return sp.getString(WEBSITE, DEF_VALUE);
        }

        public static long getFileSize() {
            return sp.getLong(FILESIZE, 0L);
        }

        public static void setRomName(String value) {
            sp.put(NAME, value);
        }

        public static void setVersionName(String value) {
            sp.put(VERSION_NAME, value);
        }

        public static void setBuildNumber(int value) {
            sp.put(BUILD_NUMBER, value);
        }

        public static void setDownloadUrl(String value) {
            sp.put(DOWNLOAD_URL, value);
        }

        public static void setMd5(String value) {
            sp.put(MD5, value);
        }

        public static void setChangelogHeaderImgUrl(String value) {
            sp.put(CHANGELOG_HEADER, value);
        }

        public static void setChangelogUrl(String value) {
            sp.put(CHANGELOG, value);
        }

        public static void setAndroidVersion(String value) {
            sp.put(ANDROID, value);
        }

        public static void setOneUIVersion(String value) {
            sp.put(ONEUI, value);
        }

        public static void setWebsite(String value) {
            sp.put(WEBSITE, value);
        }

        public static void setFileSize(long value) {
            sp.put(FILESIZE, value);
        }

        public static String getFilename() {
            String result = getRomName() + "_OTA_" + getVersionName() + "_" + getBuildNumber();
            return result.replace(" ","-");
        }

        public static String getFullFilePathName(Context context) {
            return context.getExternalFilesDir(null)
                    + File.separator
                    + getFilename()
                    + ".zip";
        }
    }

}
