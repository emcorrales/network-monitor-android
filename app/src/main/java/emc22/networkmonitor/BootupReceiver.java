package emc22.networkmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NetworkMonitoringService.class);
        context.startService(i);
    }
}
