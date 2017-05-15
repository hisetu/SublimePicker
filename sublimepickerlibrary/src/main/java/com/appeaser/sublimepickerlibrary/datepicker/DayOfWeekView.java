package com.appeaser.sublimepickerlibrary.datepicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.appeaser.sublimepickerlibrary.R;
import com.appeaser.sublimepickerlibrary.utilities.SUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.appeaser.sublimepickerlibrary.datepicker.SimpleMonthView.DAYS_IN_WEEK;

class DayOfWeekView extends View {

    private static final String DAY_OF_WEEK_FORMAT;

    static {
        // Deals with the change in usage of `EEEEE` pattern.
        // See method `SimpleDateFormat#appendDayOfWeek(...)` for more details.
        if (SUtils.isApi_18_OrHigher()) {
            DAY_OF_WEEK_FORMAT = "EEEEE";
        } else {
            DAY_OF_WEEK_FORMAT = "E";
        }
    }

    // Desired dimensions.
    private int mDesiredDayOfWeekHeight;
    private int mDesiredCellWidth;

    private final TextPaint mDayOfWeekPaint = new TextPaint();
    private final Calendar mDayOfWeekLabelCalendar = Calendar.getInstance();
    private SimpleDateFormat mDayOfWeekFormatter;

    private int mDayOfWeekHeight;
    private int mPaddedWidth;

    private Context mContext;
    private int mCellWidth;

    public DayOfWeekView(Context context) {
        this(context, null);
    }

    public DayOfWeekView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.spMonthViewStyle);
    }

    public DayOfWeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context.getResources());
        this.mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DayOfWeekView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context.getResources());
    }

    private void init(Resources res) {
        final String dayOfWeekTypeface = res
                .getString(R.string.sp_date_picker_day_of_week_typeface);
        final int dayOfWeekTextSize = res.getDimensionPixelSize(
                R.dimen.sp_date_picker_day_of_week_text_size);

        final Locale locale = res.getConfiguration().locale;

        mDayOfWeekFormatter = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, locale);

        mDayOfWeekPaint.setAntiAlias(true);
        mDayOfWeekPaint.setTextSize(dayOfWeekTextSize);
        mDayOfWeekPaint.setTypeface(Typeface.create(dayOfWeekTypeface, 0));
        mDayOfWeekPaint.setTextAlign(Paint.Align.CENTER);
        mDayOfWeekPaint.setStyle(Paint.Style.FILL);

        mDesiredDayOfWeekHeight = res.getDimensionPixelSize(R.dimen.sp_date_picker_day_of_week_height);

        mDesiredCellWidth = res.getDimensionPixelSize(R.dimen.sp_date_picker_day_width);
    }

    public void setTextAppearance(int resId) {
        applyTextAppearance(mDayOfWeekPaint, resId);
        invalidate();
    }

    void setDayOfWeekTextColor(ColorStateList dayOfWeekTextColor) {
        final int enabledColor = dayOfWeekTextColor.getColorForState(ENABLED_STATE_SET, 0);
        mDayOfWeekPaint.setColor(enabledColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int preferredHeight = mDesiredDayOfWeekHeight
                + getPaddingTop() + getPaddingBottom();

        final int preferredWidth = mDesiredCellWidth * DAYS_IN_WEEK
                + (SUtils.isApi_17_OrHigher() ? getPaddingStart() : getPaddingLeft())
                + (SUtils.isApi_17_OrHigher() ? getPaddingEnd() : getPaddingRight());
        final int resolvedWidth = resolveSize(preferredWidth, widthMeasureSpec);
        final int resolvedHeight = resolveSize(preferredHeight, heightMeasureSpec);
        setMeasuredDimension(resolvedWidth, resolvedHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        final int w = right - left;
        final int h = bottom - top;
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        final int paddingRight = getPaddingRight();
        final int paddingBottom = getPaddingBottom();
        final int paddedRight = w - paddingRight;
        final int paddedBottom = h - paddingBottom;
        final int paddedWidth = paddedRight - paddingLeft;
        final int paddedHeight = paddedBottom - paddingTop;
        mPaddedWidth = paddedWidth;

        final int measuredPaddedHeight = getMeasuredHeight() - paddingTop - paddingBottom;
        final float scaleH = paddedHeight / (float) measuredPaddedHeight;
        mDayOfWeekHeight = (int) (mDesiredDayOfWeekHeight * scaleH);

        mCellWidth = mPaddedWidth / DAYS_IN_WEEK;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();
        canvas.translate(paddingLeft, paddingTop);

        final TextPaint p = mDayOfWeekPaint;
        final int rowHeight = mDayOfWeekHeight;
        final int colWidth = mCellWidth;

        // Text is vertically centered within the day of week height.
        final float halfLineHeight = (p.ascent() + p.descent()) / 2f;
        final int rowCenter = rowHeight / 2;

        for (int col = 0; col < DAYS_IN_WEEK; col++) {
            final int colCenter = colWidth * col + colWidth / 2;
            final int colCenterRtl;
            if (SUtils.isLayoutRtlCompat(this)) {
                colCenterRtl = mPaddedWidth - colCenter;
            } else {
                colCenterRtl = colCenter;
            }

            int mWeekStart = SimpleMonthView.DEFAULT_WEEK_START;
            final int dayOfWeek = (col + mWeekStart) % DAYS_IN_WEEK;
            final String label = getDayOfWeekLabel(dayOfWeek);
            canvas.drawText(label, colCenterRtl, rowCenter - halfLineHeight, p);
        }

        canvas.translate(-paddingLeft, -paddingTop);
    }

    private String getDayOfWeekLabel(int dayOfWeek) {
        mDayOfWeekLabelCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return mDayOfWeekFormatter.format(mDayOfWeekLabelCalendar.getTime());
    }

    private ColorStateList applyTextAppearance(Paint p, int resId) {
        // Workaround for inaccessible R.styleable.TextAppearance_*
        TextView tv = new TextView(mContext);
        if (SUtils.isApi_23_OrHigher()) {
            tv.setTextAppearance(resId);
        } else {
            //noinspection deprecation
            tv.setTextAppearance(mContext, resId);
        }

        p.setTypeface(tv.getTypeface());
        p.setTextSize(tv.getTextSize());

        final ColorStateList textColor = tv.getTextColors();

        if (textColor != null) {
            final int enabledColor = textColor.getColorForState(ENABLED_STATE_SET, 0);
            p.setColor(enabledColor);
        }

        return textColor;
    }
}
