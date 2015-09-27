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
    public static final String TAG = NetworkMonitoringService.class.getSimpleName();
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
                new NotificationCompat.Builder(NetworkMonitoringService.this);

        long prevSentBytes = 0;
        long prevReceivedBytes = 0;
        mIsNotificationEnabled = true;

        while (mIsNotificationEnabled) {

            String text = null;
            String bigText = null;
            long receiving;
            long sending;
            String sendingTxt;
            String sentTxt;
            String receivingTxt;
            String receivedTxt;
            int iconResId = R.mipmap.ic_launcher;

            if (isMobile()) {
                long sentBytes = TrafficStats.getMobileTxBytes();
                if (sentBytes != TrafficStats.UNSUPPORTED) {
                    sending = sentBytes - prevSentBytes;
                    prevSentBytes = sentBytes;
                    sendingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sending);
                    sentTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, sentBytes);
                } else {
                    sendingTxt = "Unsupported";
                    sentTxt = "Unsupported";
                }

                long receivedBytes = TrafficStats.getMobileRxBytes();
                if (receivedBytes != TrafficStats.UNSUPPORTED) {
                    receiving = receivedBytes - prevReceivedBytes;
                    prevReceivedBytes = receivedBytes;
                    receivingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receiving);
                    receivedTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receivedBytes);
                } else {
                    receivingTxt = "Unsupported";
                    receivedTxt = "Unsupported";
                }
                text = "receiving: " + receivingTxt + "\nsending: " + sendingTxt;
                bigText = text + "\nreceived: " + receivedTxt + "\nsent: " + sentTxt;
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
                    sendingTxt = "Unsupported";
                    sentTxt = "Unsupported";
                }

                if (TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED
                        && TrafficStats.getMobileRxBytes() != TrafficStats.UNSUPPORTED) {
                    long receivedBytes = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
                    receiving = receivedBytes - prevReceivedBytes;
                    prevReceivedBytes = receivedBytes;
                    receivingTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receiving);
                    receivedTxt = Formatter.formatShortFileSize(NetworkMonitoringService.this, receivedBytes);
                } else {
                    receivingTxt = "Unsupported";
                    receivedTxt = "Unsupported";
                }
                text = "receiving: " + receivingTxt + "\nsending: " + sendingTxt;
                bigText = text + "\nreceived: " + receivedTxt + "\nsent: " + sentTxt;
                iconResId = R.drawable.ic_stat_wifi;
            }

            Intent intent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = builder
                    .setSmallIcon(iconResId)
                    .setContentTitle("Network Monitor")
                    .setContentText(text)
                    .setOngoing(true)
                    .setContentIntent(resultPendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
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
        return wifi != null && wifi.isConnected();
    }

    public boolean isMobile() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobile != null && mobile.isConnected();
    }
}
