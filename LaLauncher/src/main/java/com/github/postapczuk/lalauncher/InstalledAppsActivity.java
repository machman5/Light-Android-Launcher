package com.github.postapczuk.lalauncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Window.FEATURE_ACTIVITY_TRANSITIONS;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

public class InstalledAppsActivity extends Activity {

    private List<String> packageNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().requestFeature(
                FEATURE_ACTIVITY_TRANSITIONS);
        this.getWindow().setFlags(
                FLAG_LAYOUT_NO_LIMITS,
                FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed);
        AttitudeHelper.applyPadding(fetchAppList(createNewAdapter()), ScreenUtils.getDisplay(getApplicationContext()));
    }

    private ArrayAdapter<String> createNewAdapter() {
        return new ArrayAdapter<String>(
                this,
                R.layout.activity_listview,
                new ArrayList<>()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                setTextColoring(view);
                return view;
            }
        };
    }

    private ListView fetchAppList(ArrayAdapter adapter) {
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        packageNames.clear();
        adapter.clear();
        for (ResolveInfo resolver : getActivities(getPackageManager())) {
            String appName = (String) resolver.loadLabel(getPackageManager());
            if (appName.equals("Light Android Launcher"))
                continue;
            adapter.add(appName);
            packageNames.add(resolver.activityInfo.packageName);
        }
        listView.setAdapter(adapter);
        listView.setBackgroundColor(getResources().getColor(R.color.colorBackgroundPrimary));
        return setActions(listView);
    }

    private List<ResolveInfo> getActivities(PackageManager packageManager) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        return activities;
    }

    private void setTextColoring(TextView text) {
        text.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        text.setHighlightColor(getResources().getColor(R.color.colorTextPrimary));
    }

    private ListView setActions(ListView listView) {
        onClickHandler(listView);
        onLongPressHandler(listView);
        onSwipeHandler(listView);
        return listView;
    }

    private void onClickHandler(ListView listView) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 100L);

            String packageName = packageNames.get(position);
            try {
                startActivity(getPackageManager().getLaunchIntentForPackage(packageName));
            } catch (Exception e) {
                Toast.makeText(
                        InstalledAppsActivity.this,
                        String.format("Couldn't launch %s", packageName),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void onLongPressHandler(ListView listView) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            toggleTextviewBackground(view, 350L);

            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageNames.get(position)));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                fetchAppList((ArrayAdapter) listView.getAdapter());
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onSwipeHandler(ListView listView) {
        listView.setOnTouchListener(new OnSwipeTouchListenerAllApps(this, listView) {
            public void onSwipeBottom() {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        this.finish();
    }

    private void toggleTextviewBackground(View selectedItem, Long millis) {
        selectedItem.setBackgroundColor(getResources().getColor(R.color.colorBackgroundFavorite));
        new Handler().postDelayed(() -> selectedItem.setBackgroundColor(getResources().getColor(R.color.colorTransparent)), millis);
    }
}