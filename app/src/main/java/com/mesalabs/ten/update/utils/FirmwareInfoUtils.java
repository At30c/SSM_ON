package com.mesalabs.ten.update.utils;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.system.Os;
import android.system.StructUtsname;
import android.text.format.DateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mesalabs.cerberus.utils.PropUtils;
import com.mesalabs.ten.update.R;

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

public class FirmwareInfoUtils {
    private static String TAG = "FirmwareInfoUtils";

    public static String getKernelVersion() {
        StructUtsname uname = Os.uname();

        if (uname == null) {
            return null;
        }

        Matcher m = Pattern.compile("(#\\d+) (?:.*?)?((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)").matcher(uname.version);
        if (!m.matches()) {
            LogUtils.e(TAG, "Regex did not match on uname version " + uname.version);
            return null;
        }
        return uname.release + "\n" + m.group(1) + " " + m.group(2);
    }

    public static String getOneUIVersion() {
        int prop = PropUtils.getInt("ro.build.version.sep", 0);

        if (prop != 0) {
            int oneUIversion = prop - 90000;
            return oneUIversion / 10000 + "." + (oneUIversion % 10000) / 100;
        } else
            return null;
    }

    public static String getSecurityPatchVersion() {
        String patch = Build.VERSION.SECURITY_PATCH;
        if ("".equals(patch)) {
            return null;
        }
        try {
            return DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy"), new SimpleDateFormat("yyyy-MM-dd").parse(patch)).toString();
        } catch (ParseException e) {
            return patch;
        }
    }

    public static String getROMVersion() {
        int prop = PropUtils.getInt("ro.ssm.verild", 0);

        if (prop != 0) {
            return prop / 10000 + "." + (prop % 10000) / 100 + "." + (prop % 1000) / 100;
        } else
            return null;
    }
}
