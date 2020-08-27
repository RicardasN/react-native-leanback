package com.rs.leanbacknative.Layout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.RowsFragment;
import androidx.leanback.app.VerticalGridFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.leanback.widget.VerticalGridPresenter;

import com.facebook.react.ReactFragment;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.rs.leanbacknative.CardPresenter;
import com.rs.leanbacknative.DataManager;
import com.rs.leanbacknative.Model.NativeRow;
import com.rs.leanbacknative.Model.NativeRowItem;
import com.rs.leanbacknative.R;

import java.util.Collections;
import java.util.List;


@SuppressLint("ViewConstructor")
public class LeanbackRowLayout extends FrameLayout {
    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 500;
    private static final int GRID_ITEM_HEIGHT = 500;

    private final String LOG_DEBUG_TAG = "RowsFragmentDebug";

    private ThemedReactContext mContext;
//    private CardPresenter mCardPresenter;
    private ArrayObjectAdapter mRowsAdapter;
//    private ArrayObjectAdapter mListRowAdapterWithData;

    private String mRowTitle;

    public LeanbackRowLayout(@NonNull ThemedReactContext context, RowsFragment rowsFragment) {
        super(context);

        mContext = context;
        initializeAdapter(rowsFragment);
        setupEventListeners(rowsFragment);
    }

    private void initializeAdapter(RowsFragment rowsFragment) {
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        FragmentManager fragmentManager = mContext.getCurrentActivity().getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(rowsFragment, "RowsFragment").commit();
        fragmentManager.executePendingTransactions();
        rowsFragment.setAdapter(mRowsAdapter);
    }

    private void setupEventListeners(RowsFragment rowsFragment) {
        rowsFragment.setOnItemViewClickedListener(new ItemViewClickedListener());
        rowsFragment.setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {

            if (item instanceof NativeRowItem) {
                NativeRowItem nativeRowItem = (NativeRowItem) item;
                WritableMap event = Arguments.createMap();
                event.putString("item", nativeRowItem.toJSON());
                mContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onFocus", event);
            }
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof NativeRowItem) {
                NativeRowItem nativeRowItem = (NativeRowItem) item;
                WritableMap event = Arguments.createMap();
                event.putString("item", nativeRowItem.toJSON());
                mContext.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onClick", event);
            }
        }
    }

    public void setDataAndAttributes(ReadableMap dataAndAttributes) {
        ReadableArray data = dataAndAttributes.getArray("data");
        List<NativeRowItem> rows = DataManager.setupData(data);

        ArrayObjectAdapter mListRowAdapterWithData;
        CardPresenter mCardPresenter;

        ReadableMap attributes = dataAndAttributes.getMap("attributes");
        if (attributes != null) {
            mCardPresenter = new CardPresenter(attributes);
        } else {
            mCardPresenter = new CardPresenter();
        }

//        if (mListRowAdapterWithData == null) {
            mListRowAdapterWithData = new ArrayObjectAdapter(mCardPresenter);
//        }

        for (int i = 0; i < rows.size(); i++) {
            mListRowAdapterWithData.add(rows.get(i));
        }

        HeaderItem header = new HeaderItem(0, mRowTitle);
        mRowsAdapter.clear();
        mRowsAdapter.add(new ListRow(header, mListRowAdapterWithData));
    }

    public void setRowTitle(String title) {
        mRowTitle = title;
    }
}