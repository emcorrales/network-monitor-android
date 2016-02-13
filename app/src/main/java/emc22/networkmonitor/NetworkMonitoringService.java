package emc22.networkmonitor;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Formatter;

public class NetworkMonitoringService extends Service implements Runnable {
    public static final int SERVICE_ID = 777;
    private static final String UNSUPPORTED = "Unsupported";

    private boolean mIsNotificationEnabled = true;
    private int mDelayTime = 1000;
    private NotificationCompat.Builder mNotifBuilder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mIsNotificationEnabled = false;
        super.onDestroy();
    }

    @Override
    public void run() {
        if (mNotifBuilder == null) {
            mNotifBuilder = new NotificationCompat.Builder(NetworkMonitoringService.this);
        }

        long prevSentBytes = 0;
        long prevReceivedBytes = 0;
        mIsNotificationEnabled = true;

        while (mIsNotificationEnabled) {
            String sendingTxt = null;
            String sentTxt = null;
            String receivingTxt = null;
            String receivedTxt = null;
            int iconResId = R.mipmap.ic_launcher;

            if (isConnected()) {
                long receiving;
                long sending;
                if (isMobile()) {
                    long sentBytes = TrafficStats.getMobileTxBytes();
                    if (sentBytes != TrafficStats.UNSUPPORTED) {
                        sending = sentBytes - prevSentBytes;
                        prevSentBytes = sentBytes;
                        sendingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sending);
                        sentTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sentBytes);
                    } else {
                        sendingTxt = UNSUPPORTED;
                        sentTxt = UNSUPPORTED;
                    }

                    long receivedBytes = TrafficStats.getMobileRxBytes();
                    if (receivedBytes != TrafficStats.UNSUPPORTED) {
                        receiving = receivedBytes - prevReceivedBytes;
                        prevReceivedBytes = receivedBytes;
                        receivingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receiving);
                        receivedTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receivedBytes);
                    } else {
                        receivingTxt = UNSUPPORTED;
                        receivedTxt = UNSUPPORTED;
                    }
                    iconResId = R.drawable.ic_stat_mobile;

                } else if (isWifi()) {
                    if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED
                            && TrafficStats.getMobileTxBytes() != TrafficStats.UNSUPPORTED) {
                        long sentBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                        sending = sentBytes - prevSentBytes;
                        prevSentBytes = sentBytes;
                        sendingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sending);
                        sentTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sentBytes);
                    } else {
                        sendingTxt = UNSUPPORTED;
                        sentTxt = UNSUPPORTED;
                    }

                    if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED
                            && TrafficStats.getMobileRxBytes() != TrafficStats.UNSUPPORTED) {
                        long receivedBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                        receiving = receivedBytes - prevReceivedBytes;
                        prevReceivedBytes = receivedBytes;
                        receivingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receiving);
                        receivedTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receivedBytes);
                    } else {
                        receivingTxt = UNSUPPORTED;
                        receivedTxt = UNSUPPORTED;
                    }
                    iconResId = R.drawable.ic_stat_wifi;
                }

                String text = getString(R.string.downloading) + receivingTxt + "\n" + getString(R.string.upload) + sendingTxt;

                NotificationCompat.Style notifStyle = new NotificationCompat.InboxStyle()
                        .addLine(getString(R.string.downloading) + " " + receivingTxt + "\t" + getString(R.string.total_download) + receivedTxt)
                        .addLine(getString(R.string.upload) + " " + sendingTxt + "\t" + getString(R.string.total_upload) + sentTxt);

                Notification notification = mNotifBuilder
                        .setSmallIcon(iconResId)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(text)
                        .setOngoing(true)
                        .setContentIntent(createPendingIntent())
                        .setStyle(notifStyle)
                        .setPriority(Notification.PRIORITY_LOW)
                        .build();

                startForeground(SERVICE_ID, notification);
                mDelayTime = 1000;
            } else {
                stopForeground(true);
                mDelayTime = 5000;
            }

            try {
                Thread.sleep(mDelayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isConnected();
            }
        }
        return false;
    }

    private boolean isWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    private boolean isMobile() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile != null && mobile.isConnected();
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
