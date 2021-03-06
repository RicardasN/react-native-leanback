package com.rs.leanbacknative;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.PixelUtil;
import com.rs.leanbacknative.Model.NativeRowItem;
import com.rs.leanbacknative.Widget.NativeImageCardView;

import java.util.Objects;


public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static final int DEFAULT_CARD_WIDTH = 313;
    private static final int DEFAULT_CARD_HEIGHT = 176;

    private final String FOCUS_DIRECTION_UP = "up";
    private final String FOCUS_DIRECTION_DOWN = "down";

    private Drawable mDefaultCardImage;

    private Integer mCardWidth = DEFAULT_CARD_WIDTH;
    private Integer mCardHeight = DEFAULT_CARD_HEIGHT;
    private boolean mHasImageOnly = false;
    private boolean mHasTitle = true;
    private boolean mHasContent = true;
    private boolean mHasIconRight = false;
    private boolean mHasIconLeft = false;
    private ReadableArray mForbiddenFocusDirections;
    private int nextFocusUpId = -1;
    private int nextFocusDownId = -1;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private String mCardShape = "square";


    public CardPresenter(ReadableMap attributes) {
        mCardWidth = Math.round(PixelUtil.toPixelFromDIP(attributes.getInt("width")));
        mCardHeight = Math.round(PixelUtil.toPixelFromDIP(attributes.getInt("height")));
        mHasImageOnly = attributes.getBoolean("hasImageOnly");
        mForbiddenFocusDirections = attributes.hasKey("forbiddenFocusDirections") ? attributes.getArray("forbiddenFocusDirections") : null;
        mHasTitle = attributes.getBoolean("hasTitle");
        mHasContent = attributes.getBoolean("hasContent");
        mHasIconRight = attributes.getBoolean("hasIconRight");
        mHasIconLeft = attributes.getBoolean("hasIconLeft");
        nextFocusUpId = attributes.getInt("nextFocusUpId");
        nextFocusDownId = attributes.getInt("nextFocusDownId");
        mCardShape = attributes.getString("cardShape");
    }

    public CardPresenter() {
    }

    private static void updateCardBackgroundColor(NativeImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        sSelectedBackgroundColor =
            ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        sDefaultBackgroundColor = !mCardShape.equals("round") && mHasImageOnly ? ContextCompat.getColor(parent.getContext(), R.color.default_background) : Color.TRANSPARENT;
        mDefaultCardImage = ContextCompat.getDrawable(parent.getContext(), R.drawable.lb_ic_sad_cloud);

        NativeImageCardView cardView =
            new NativeImageCardView(parent.getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    if (!mHasImageOnly && !mCardShape.equals("round")) {
                        updateCardBackgroundColor(this, selected);
                    }
                    super.setSelected(selected);
                }
            };
            
        cardView.buildImageCardView(mHasImageOnly, mHasTitle, mHasContent, mHasIconRight, mHasIconLeft);



        cardView.setBackgroundColor(sDefaultBackgroundColor);
        if (!mHasImageOnly)  cardView.findViewById(R.id.info_field).setBackgroundColor(sDefaultBackgroundColor);

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        NativeRowItem rowItem = (NativeRowItem) item;
        NativeImageCardView cardView = (NativeImageCardView) viewHolder.view;

        cardView.setTitleText(rowItem.getTitle());
        cardView.setContentText(rowItem.getDescription());

        if (cardView.getId() == -1) {
            cardView.setId(rowItem.getViewId());
        }

        if (mForbiddenFocusDirections != null) {
            for (int i = 0; i < mForbiddenFocusDirections.size(); i++) {
                if (Objects.equals(mForbiddenFocusDirections.getString(i), FOCUS_DIRECTION_UP)) {
                    cardView.setNextFocusUpId(cardView.getId());
                }
                if (Objects.equals(mForbiddenFocusDirections.getString(i), FOCUS_DIRECTION_DOWN)) {
                    cardView.setNextFocusDownId(cardView.getId());
                }
            }
        }

        if (nextFocusUpId != -1) {
            cardView.setNextFocusUpId(nextFocusUpId);
        }

        if (nextFocusDownId != -1) {
            cardView.setNextFocusDownId(nextFocusDownId);
        }

        if (rowItem.getCardImageUrl() != null) {
            cardView.setMainImageDimensions(mCardWidth, mCardHeight);

            RequestOptions requestOptions = mCardShape.equals("round") ? RequestOptions.circleCropTransform() : RequestOptions.centerCropTransform();

            Glide.with(viewHolder.view.getContext())
                .load(rowItem.getCardImageUrl())
                .apply(requestOptions)
                .error(mDefaultCardImage)
                .into(cardView.getMainImageView());


        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        NativeImageCardView cardView = (NativeImageCardView) viewHolder.view;
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}
