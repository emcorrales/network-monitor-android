package emc22.networkmonitor;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.applicationList)
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ApplicationAdapter adapter = new ApplicationAdapter(this, filterApplications());
        mListView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, NetworkMonitoringService.class));
    }

    private List<PackageInfo> getPackageInfos() {
        PackageManager pm = getPackageManager();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return pm.getInstalledPackages(0);
        } else {
            String[] permission = {"android.permission.INTERNET"};
            return pm.getPackagesHoldingPermissions(permission, 0);
        }
    }

    private List<PackageInfo> filterApplications() {
        List<PackageInfo> filtered = new ArrayList<>();
        List<PackageInfo> originalPackageInfos = getPackageInfos();
        for (PackageInfo packageInfo : originalPackageInfos) {
            if (TrafficStats.getUidRxBytes(packageInfo.applicationInfo.uid) > 0) {
                filtered.add(packageInfo);
            }
        }
        return filtered;
    }
}
