package emc22.networkmonitor;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;

public class NetworkMonitorService extends Service implements Runnable {
    public static final String TAG = NetworkMonitorService.class.getSimpleName();
    public static final int SERVICE_ID = 777;

    private boolean mIsNotificationEnabled = true;
    private int mDelayTime = 1000;

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
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(NetworkMonitorService.this);

        long prevSentBytes = 0;
        long prevReceivedBytes = 0;
        mIsNotificationEnabled = true;

        while (mIsNotificationEnabled) {
            String text = null;
            String bigText = null;
            long receiving = 0;
            long sending = 0;
            String sendingTxt = null;
            String receivingTxt = null;

            if (isMobile()) {
                long sentBytes = TrafficStats.getMobileTxBytes();
                if (sentBytes != TrafficStats.UNSUPPORTED) {
                    sending = sentBytes - prevSentBytes;
                    prevSentBytes = sentBytes;
                    sendingTxt = Formatter.formatShortFileSize(NetworkMonitorService.this, sending);
                } else {
                    sendingTxt = "Unsupported";
                }

                long receivedBytes = TrafficStats.getMobileRxBytes();
                if (receivedBytes != TrafficStats.UNSUPPORTED) {
                    receiving = receivedBytes - prevReceivedBytes;
                    prevReceivedBytes = receivedBytes;
                    receivingTxt = Formatter.formatShortFileSize(NetworkMonitorService.this, receiving);
                } else {
                    receivingTxt = "Unsupported";
                }

                text = "receiving: " + receivingTxt + "\nsending: " + sendingTxt;

            } else if (isWifi()) {
                if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED
                        && TrafficStats.getMobileTxBytes() != TrafficStats.UNSUPPORTED) {
                    long sentBytes = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
                    sending = sentBytes - prevSentBytes;
                    prevSentBytes = sentBytes;
                    sendingTxt =
                            Formatter.formatShortFileSize(NetworkMonitorService.this, sending);
                } else {
                    sendingTxt = "Unsupported";
                }

                if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED
                        && TrafficStats.getMobileRxBytes() != TrafficStats.UNSUPPORTED) {
                    long receivedBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                    receiving = receivedBytes - prevReceivedBytes;
                    prevReceivedBytes = receivedBytes;
                    receivingTxt =
                            Formatter.formatShortFileSize(NetworkMonitorService.this, receiving);
                } else {
                    receivingTxt = "Unsupported";
                }
                text = "receiving: " + receivingTxt + "\nsending: " + sendingTxt;
            }

            Notification notification = builder
                    .setSmallIcon(R.drawable.ic_stat_mobile)
                    .setContentTitle("Network Monitor")
                    .setContentText(text)
                    .setOngoing(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                    .build();

            startForeground(SERVICE_ID, notification);

            try {
                Thread.sleep(mDelayTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi != null) {
            return wifi.isConnected();
        } else {
            return false;
        }
    }

    public boolean isMobile() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile != null) {
            return mobile.isConnected();
        } else {
            return false;
        }
    }
}
