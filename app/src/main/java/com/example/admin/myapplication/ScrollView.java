package com.example.admin.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.lang.ref.WeakReference;

public class ScrollView {

    private static ScrollView instance;

   private final String KEY_TRANSITION_TIME;

    public static void init(Context context) {
        instance = new ScrollView(context);
    }

    private ScrollView(Context context) {
        KEY_TRANSITION_TIME = context.getString(R.string.pref_key_transition_time);
    }

    public static void configureTransitionTime(ScrollView scrollView) {
        final BottomSheetDialog bsd = new BottomSheetDialog(scrollView.getContext());
        final TransitionTimeChangeListener timeChangeListener = new TransitionTimeChangeListener(scrollView);
        bsd.setContentView(R.layout.dialog_transition_time);
        defaultPrefs().registerOnSharedPreferenceChangeListener(timeChangeListener);
        bsd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                defaultPrefs().unregisterOnSharedPreferenceChangeListener(timeChangeListener);
            }
        });
        bsd.findViewById(R.id.dialog_btn_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsd.dismiss();
            }
        });
        bsd.show();
    }

    public static void smoothScrollToUserSelectedPosition(final ScrollView scrollView, View anchor) {
        PopupMenu popupMenu = new PopupMenu(scrollView.getContext(), anchor);
        Menu menu = popupMenu.getMenu();
        final RecyclerView.Adapter adapter = scrollView.getAdapter();
        int itemCount = (adapter instanceof InfiniteScrollAdapter) ?
                ((InfiniteScrollAdapter) adapter).getRealItemCount() :
                adapter.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            menu.add(String.valueOf(i + 1));
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int destination = Integer.parseInt(String.valueOf(item.getTitle())) - 1;
                if (adapter instanceof InfiniteScrollAdapter) {
                    destination = ((InfiniteScrollAdapter) adapter).getClosestPosition(destination);
                }
                scrollView.smoothScrollToPosition(destination);
                return true;
            }
        });
        popupMenu.show();
    }

    public static int getTransitionTime() {
        return defaultPrefs().getInt(instance.KEY_TRANSITION_TIME, 150);
    }

    private static SharedPreferences defaultPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance());
    }

    private static class TransitionTimeChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        private WeakReference<ScrollView> scrollView;

        public TransitionTimeChangeListener(ScrollView scrollView) {
            this.scrollView = new WeakReference<>(scrollView);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(instance.KEY_TRANSITION_TIME)) {
                ScrollView scrollView = this.scrollView.get();
                if (scrollView != null) {
                    scrollView.setItemTransitionTimeMillis(sharedPreferences.getInt(key, 150));
                } else {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                }
            }
        }
    }
}


