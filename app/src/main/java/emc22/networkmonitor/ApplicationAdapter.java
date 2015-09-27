package emc22.networkmonitor;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ApplicationAdapter extends BaseAdapter {

    private Context mContext;
    private PackageManager mPackageManager;
    private LayoutInflater mLayoutInflater;
    private List<PackageInfo> mPackageInfos;

    class ViewHolder {
        @Bind(R.id.appIcon)
        ImageView appIcon;

        @Bind(R.id.appName)
        TextView appName;

        @Bind(R.id.receiving)
        TextView receiving;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ApplicationAdapter(Context context, List<PackageInfo> packageInfos) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mLayoutInflater = LayoutInflater.from(context);
        mPackageInfos = packageInfos;
    }

    @Override
    public int getCount() {
        return mPackageInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return mPackageInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(R.layout.item_applications, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        ApplicationInfo appInfo = mPackageInfos.get(position).applicationInfo;
        holder.appIcon.setImageDrawable(mPackageManager.getApplicationIcon(appInfo));
        holder.appName.setText(mPackageManager.getApplicationLabel(appInfo).toString());
        long receivedBytes = TrafficStats.getUidRxBytes(appInfo.uid);
        String receivedText = Formatter.formatShortFileSize(mContext, receivedBytes);
        holder.receiving.setText(receivedText);
        return convertView;
    }
}
