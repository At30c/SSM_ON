package com.mesalabs.ten.update.ota.tasks;

import android.os.Build;

import com.mesalabs.cerberus.utils.PropUtils;
import com.mesalabs.ten.update.ota.utils.Constants;
import com.mesalabs.ten.update.ota.utils.PreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Parses the public incremental-update manifest. */
public final class IncrementalManifestParser {
    public static final class Result {
        public final boolean updateAvailable;

        Result(boolean updateAvailable) {
            this.updateAvailable = updateAvailable;
        }
    }

    public Result parse(InputStream input) throws IOException, JSONException {
        JSONObject manifest = new JSONObject(readAll(input));
        if (manifest.optInt("schema_version", 0) != 1) {
            throw new JSONException("Unsupported manifest schema");
        }

        String device = Build.DEVICE;
        String currentVersion = PropUtils.get(Constants.PROP_ROM_VERSION, "");
        JSONArray updates = manifest.optJSONArray("updates");
        if (updates == null) {
            throw new JSONException("Manifest does not contain updates");
        }

        PreferencesUtils.ROM.clean();
        PreferencesUtils.Download.setUpdateAvailability(false);

        for (int index = 0; index < updates.length(); index++) {
            JSONObject update = updates.getJSONObject(index);
            if (!device.equals(update.optString("device"))
                    || !currentVersion.equals(update.optString("from_version"))) {
                continue;
            }

            String sha256 = update.optString("sha256").toLowerCase();
            long size = update.optLong("size", -1);
            String url = update.optString("url");
            if (!sha256.matches("[0-9a-f]{64}") || size <= 0 || url.isEmpty()) {
                throw new JSONException("Invalid incremental update entry");
            }

            PreferencesUtils.ROM.setRomName(update.optString("rom_name", "ExtremeROM"));
            PreferencesUtils.ROM.setVersionName(update.getString("version"));
            PreferencesUtils.ROM.setBuildNumber(update.optInt("build_date", 0));
            PreferencesUtils.ROM.setDownloadUrl(url);
            PreferencesUtils.ROM.setSha256(sha256);
            PreferencesUtils.ROM.setFileSize(size);
            PreferencesUtils.ROM.setAndroidVersion(update.optString("android_version", ""));
            PreferencesUtils.ROM.setOneUIVersion(update.optString("oneui_version", ""));
            PreferencesUtils.ROM.setChangelogUrl(update.optString("changelog_url", ""));
            PreferencesUtils.Download.setUpdateAvailability(true);
            return new Result(true);
        }

        return new Result(false);
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
}
