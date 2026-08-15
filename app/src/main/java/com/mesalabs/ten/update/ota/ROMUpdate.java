package com.mesalabs.ten.update.ota;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mesalabs.cerberus.utils.CerberusException;
import com.mesalabs.ten.update.TenUpdateApp;
import com.mesalabs.ten.update.R;
import com.mesalabs.ten.update.activity.home.MainActivity;
import com.mesalabs.ten.update.ota.noti.FetchOTANotificationManager;
import com.mesalabs.ten.update.ota.tasks.UpdateManifestParser;
import com.mesalabs.ten.update.ota.utils.Constants;
import com.mesalabs.ten.update.ota.utils.GeneralUtils;
import com.mesalabs.ten.update.ota.utils.PreferencesUtils;
import com.mesalabs.ten.update.ui.widget.DownloadProgressView;
import com.mesalabs.ten.update.utils.LogUtils;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.HttpUrlConnectionDownloader;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Downloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

public class ROMUpdate {
    public static final int STATE_NO_UPDATES = 1;
    public static final int STATE_NEW_VERSION_AVAILABLE = 2;
    public static final int STATE_ERROR = 3;
    public static final int STATE_CHECKING = 4;
    public static final int STATE_DOWNLOADED = 5;

    private Context mContext;
    private ROMUpdate.StubListener mStubListener;
    private boolean mIsRunningInApp = true;
    private boolean mNewUpdateAvailable = false;
    private boolean mManifestLoaded = false;

    public ROMUpdate(Context context, ROMUpdate.StubListener stubListener) {
        mContext = context;
        mStubListener = stubListener;
    }

    public void checkUpdates(boolean inApp) {
        if (!PreferencesUtils.Download.getDownloadFinished()) {
            PreferencesUtils.ROM.clean();
            mIsRunningInApp = inApp;
            new LoadUpdateManifest(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void postCheckUpdates() {
        int newStatus = STATE_ERROR;

        if (mManifestLoaded) {
            newStatus = mNewUpdateAvailable ? STATE_NEW_VERSION_AVAILABLE : STATE_NO_UPDATES;

            GeneralUtils.dismissNotifications(mContext);

            if (!mIsRunningInApp) {
                if (mNewUpdateAvailable) {
                    GeneralUtils.setupUpdateAvailableNotification(mContext);
                }
                GeneralUtils.scheduleNotification(mContext, PreferencesUtils.getBgServiceEnabled());
            }
        }

        if (mStubListener != null)
            mStubListener.onUpdateCheckCompleted(newStatus);
    }


    public interface StubListener {
        void onUpdateCheckCompleted(int status);
    }


    class LoadUpdateManifest extends AsyncTask<Void, Void, Boolean> {
        private final String TAG = "LoadUpdateManifest";

        private Context mContext;
        private boolean mUpdateAvailable;


        public LoadUpdateManifest(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            if (!mIsRunningInApp) {
                GeneralUtils.dismissNotifications(mContext);
            }

        }

        @Override
        protected Boolean doInBackground(Void... v) {
            try {
                URL url = new URL(Constants.OTA_MANIFEST_URL);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.connect();

                try (InputStream input = connection.getInputStream()) {
                    UpdateManifestParser.Result result = new UpdateManifestParser().parse(input);
                    mUpdateAvailable = result.updateAvailable;
                }
            } catch (Exception e) {
                LogUtils.d(TAG, "Exception: " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mManifestLoaded = result;
            mNewUpdateAvailable = result && mUpdateAvailable;
            Intent intent;
            if (!mIsRunningInApp) {
                intent = new Intent(Constants.INTENT_MANIFEST_CHECK_BACKGROUND);
            } else {
                intent = new Intent(Constants.INTENT_MANIFEST_LOADED);

            }

            mContext.sendBroadcast(intent);
            super.onPostExecute(result);

            postCheckUpdates();
        }
    }



    public static class Download {
        private final static String TAG = "ROMUpdate.Download";

        private MainActivity mActivity;
        private Fetch mFetch;

        public Download(MainActivity activity) {
            mActivity = activity;
        }

        public void startDownload() {
            String url = PreferencesUtils.ROM.getDownloadUrl();
            String file = PreferencesUtils.ROM.getFullFilePathName(mActivity);

            PreferencesUtils.Download.setIsDownloadOnGoing(true);

            FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(mActivity)
                    .enableLogging(TenUpdateApp.isDebugBuild())
                    .setDownloadConcurrentLimit(1)
                    .setHttpDownloader(new HttpUrlConnectionDownloader(Downloader.FileDownloaderType.SEQUENTIAL))
                    .setNotificationManager(new FetchOTANotificationManager(mActivity) {
                        @NotNull
                        @Override
                        public Fetch getFetchInstanceForNamespace(@NotNull String s) {
                            return mFetch;
                        }
                    })
                    .build();
            mFetch = Fetch.Impl.getInstance(fetchConfiguration);

            final Request request = new Request(url, file);
            request.setDownloadOnEnqueue(true);
            request.setPriority(Priority.HIGH);
            request.setNetworkType(PreferencesUtils.getNetworkType() == 0 ? NetworkType.ALL : NetworkType.WIFI_ONLY);

            mFetch.enqueue(request, updatedRequest -> {
                FetchListener fetchListener = new FetchListener() {
                    @Override
                    public void onWaitingNetwork(@NotNull com.tonyodev.fetch2.Download download) {
                        DownloadProgressView dpv = mActivity.getDownloadFragment().getDownloadProgressView();
                        dpv.setWaitingForNetworkStatus(true);
                        mActivity.animateBottomDownloadButton(false, false);
                    }

                    @Override
                    public void onStarted(@NotNull com.tonyodev.fetch2.Download download, @NotNull List<? extends DownloadBlock> downloadBlocks, int totalBlocks) {
                        DownloadProgressView dpv = mActivity.getDownloadFragment().getDownloadProgressView();
                        dpv.setWaitingForNetworkStatus(false);
                        mActivity.animateBottomDownloadButton(true, false);
                    }

                    @Override
                    public void onResumed(@NotNull com.tonyodev.fetch2.Download download) {
                        DownloadProgressView dpv = mActivity.getDownloadFragment().getDownloadProgressView();
                        dpv.setPausedStatus(false);
                        mActivity.animateBottomDownloadButton(true, false);
                    }

                    @Override
                    public void onRemoved(@NotNull com.tonyodev.fetch2.Download download) { }

                    @Override
                    public void onQueued(@NotNull com.tonyodev.fetch2.Download download, boolean waitingOnNetwork) { }

                    @Override
                    public void onProgress(@NotNull com.tonyodev.fetch2.Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
                        if (PreferencesUtils.Download.getDownloadID() == download.getId()) {
                            DownloadProgressView dpv = mActivity.getDownloadFragment().getDownloadProgressView();

                            int progress = download.getProgress();
                            if (progress < 0) progress = 0;
                            if (progress < dpv.getProgress()) {
                                Toast.makeText(mActivity, mActivity.getString(R.string.mesa_download_failed), Toast.LENGTH_LONG).show();
                            }
                            dpv.setProgress(progress);

                            String hms = String.format(mActivity.getResources().getConfiguration().getLocales().get(0), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(etaInMilliSeconds),
                                    TimeUnit.MILLISECONDS.toMinutes(etaInMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                                    TimeUnit.MILLISECONDS.toSeconds(etaInMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
                            dpv.setTimeLeftText(hms);
                        }
                    }

                    @Override
                    public void onPaused(@NotNull com.tonyodev.fetch2.Download download) {
                        DownloadProgressView dpv = mActivity.getDownloadFragment().getDownloadProgressView();
                        dpv.setPausedStatus(true);
                        mActivity.animateBottomDownloadButton(true, true);
                    }

                    @Override
                    public void onError(@NotNull com.tonyodev.fetch2.Download download, @NotNull Error error, @Nullable Throwable throwable) {
                        mActivity.onErrorROMUpdateDownload();
                    }

                    @Override
                    public void onDownloadBlockUpdated(@NotNull com.tonyodev.fetch2.Download download, @NotNull DownloadBlock downloadBlock, int totalBlocks) { }

                    @Override
                    public void onDeleted(@NotNull com.tonyodev.fetch2.Download download) { }

                    @Override
                    public void onCompleted(@NotNull com.tonyodev.fetch2.Download download) {
                        new SHA256Check(mActivity).execute();
                    }

                    @Override
                    public void onCancelled(@NotNull com.tonyodev.fetch2.Download download) {
                        PreferencesUtils.Download.clean();
                    }

                    @Override
                    public void onAdded(@NotNull com.tonyodev.fetch2.Download download) { }
                };
                mFetch.addListener(fetchListener);
            }, error -> {
                throw new CerberusException(error.toString());
            });

            PreferencesUtils.Download.setDownloadID(request.getId());

            mActivity.onPostROMUpdateDownload();
            mActivity.animateBottomDownloadButton(false, false);
        }

        public void cancelDownload() {
            String file = PreferencesUtils.ROM.getFullFilePathName(mActivity);
            int mDownloadID = PreferencesUtils.Download.getDownloadID();
            GeneralUtils.deleteFile(new File(file));
            mFetch.remove(mDownloadID);
            mFetch.close();
            PreferencesUtils.Download.clean();
        }

        public void pauseDownload() {
            int mDownloadID = PreferencesUtils.Download.getDownloadID();
            mFetch.pause(mDownloadID);
        }

        public void resumeDownload() {
            int mDownloadID = PreferencesUtils.Download.getDownloadID();
            mFetch.resume(mDownloadID);
        }
    }

    static class SHA256Check extends AsyncTask<Object, Boolean, Boolean>{
        private final String TAG = "SHA256Check";
        private MainActivity mActivity;
        private DownloadProgressView mDPV;
        private File mUpdatePkg;

        private SHA256Check(MainActivity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mDPV = mActivity.getDownloadFragment().getDownloadProgressView();
            mDPV.setCheckingMD5Status();
            mActivity.animateBottomDownloadButton(false, false);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            mUpdatePkg = new File(PreferencesUtils.ROM.getFullFilePathName(mActivity.getApplicationContext()));
            String sha256Remote = PreferencesUtils.ROM.getSha256().trim();
            return checkSHA256(sha256Remote, mUpdatePkg);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            PreferencesUtils.Download.setIsDownloadOnGoing(false);
            PreferencesUtils.Download.setDownloadFinished(result);

            if (result) {
                if (TenUpdateApp.isAppInBackground()) {
                    GeneralUtils.setupDownloadCompletedNotification(mActivity, true);
                    mActivity.switchToFragment(MainActivity.MAIN_PAGE_FRAGMENT);
                } else {
                    mDPV.setDownloadCompleteStatus();
                    mActivity.getDownloadFragment().getPreInstallWarningTextView().setVisibility(View.VISIBLE);
                    mActivity.animateBottomInstallButton(true);
                }
            } else {
                if (TenUpdateApp.isAppInBackground()) {
                    GeneralUtils.setupDownloadCompletedNotification(mActivity, false);
                } else {
                    Toast.makeText(mActivity, mActivity.getString(R.string.mesa_download_failed_md5), Toast.LENGTH_LONG).show();
                }
                GeneralUtils.deleteFile(mUpdatePkg);
                mActivity.switchToFragment(MainActivity.MAIN_PAGE_FRAGMENT);
            }

            super.onPostExecute(result);
        }

        private boolean checkSHA256(String sha256, File file) {
            if (TextUtils.isEmpty(sha256) || file == null) {
                LogUtils.e(TAG, "SHA-256 string empty or updateFile null");
                return false;
            }

            String calculatedDigest = calculateSHA256(file);
            if (calculatedDigest == null) {
                LogUtils.e(TAG, "calculatedDigest null");
                return false;
            }

            LogUtils.v(TAG, "Calculated digest: " + calculatedDigest + ", Manifest digest: " + sha256);

            return calculatedDigest.equalsIgnoreCase(sha256);
        }

        String calculateSHA256(File updateFile) {
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                LogUtils.e(TAG, e.toString());
                return null;
            }

            InputStream is;
            try {
                is = new FileInputStream(updateFile);
            } catch (FileNotFoundException e) {
                LogUtils.e(TAG, e.toString());
                return null;
            }

            byte[] buffer = new byte[8192];
            int read;
            try {
                while ((read = is.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
                byte[] hash = digest.digest();
                StringBuilder output = new StringBuilder(hash.length * 2);
                for (byte value : hash) {
                    output.append(String.format("%02x", value));
                }
                return output.toString();
            } catch (IOException e) {
                throw new CerberusException(e.toString());
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, e.toString());
                }
            }
        }
    }
}
