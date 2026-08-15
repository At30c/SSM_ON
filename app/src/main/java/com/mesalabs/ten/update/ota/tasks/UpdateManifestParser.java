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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses public full-ROM releases and optional future incremental releases. */
public final class UpdateManifestParser {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:[-+].*)?$");

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

        JSONArray updates = manifest.optJSONArray("updates");
        if (updates == null) {
            throw new JSONException("Manifest does not contain updates");
        }

        PreferencesUtils.ROM.clean();
        PreferencesUtils.Download.setUpdateAvailability(false);

        for (int index = 0; index < updates.length(); index++) {
            JSONObject update = updates.getJSONObject(index);
            if (!isCompatible(update)) {
                continue;
            }

            String sha256 = update.optString("sha256").toLowerCase();
            long size = update.optLong("size", -1);
            String url = update.optString("url");
            if (!sha256.matches("[0-9a-f]{64}") || size <= 0 || url.isEmpty()) {
                throw new JSONException("Invalid update entry");
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

    private static boolean isCompatible(JSONObject update) throws JSONException {
        if (!Build.DEVICE.equals(update.optString("device")) || !hasCurrentModel(update.optJSONArray("models"))) {
            return false;
        }

        String packageType = update.optString("package_type", "full");
        if ("full".equals(packageType)) {
            return isNewerVersion(update.getString("version"), PropUtils.get(Constants.PROP_ROM_VERSION, ""));
        }
        if ("incremental".equals(packageType)) {
            return PropUtils.get(Constants.PROP_ROM_VERSION, "").equals(update.optString("from_version"));
        }
        throw new JSONException("Unsupported package type");
    }

    private static boolean hasCurrentModel(JSONArray models) {
        if (models == null) {
            return false;
        }
        for (int index = 0; index < models.length(); index++) {
            if (Build.MODEL.equals(models.optString(index))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNewerVersion(String candidate, String current) {
        Matcher candidateMatcher = VERSION_PATTERN.matcher(candidate);
        Matcher currentMatcher = VERSION_PATTERN.matcher(current);
        if (!candidateMatcher.matches() || !currentMatcher.matches()) {
            return false;
        }
        for (int index = 1; index <= 3; index++) {
            int candidatePart = Integer.parseInt(candidateMatcher.group(index));
            int currentPart = Integer.parseInt(currentMatcher.group(index));
            if (candidatePart != currentPart) {
                return candidatePart > currentPart;
            }
        }
        return false;
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
