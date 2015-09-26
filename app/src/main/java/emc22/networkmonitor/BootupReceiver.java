package emc22.networkmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {
    private static final String TAG = BootupReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NetworkMonitorService.class);
        context.startService(i);
    }
}
