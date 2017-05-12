/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appeaser.sublimepickerlibrary.datepicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appeaser.sublimepickerlibrary.R;
import com.appeaser.sublimepickerlibrary.utilities.Config;

import java.util.Calendar;

/**
 * An adapter for a list of {@link SimpleMonthView} items.
 */
class DayPickerPagerAdapter extends PagerAdapter {

    private static final String TAG = DayPickerPagerAdapter.class.getSimpleName();

    private final SparseArray<ViewHolder> mItems = new SparseArray<>();

    private final LayoutInflater mInflater;
    private final int mLayoutResId;
    private final int mCalendarViewId;

    private int mMonthTextAppearance;
    private int mDayOfWeekTextAppearance;
    private int mDayTextAppearance;

    private ColorStateList mCalendarTextColor;
    private ColorStateList mDaySelectorColor;
    private final ColorStateList mDayHighlightColor;

    private DaySelectionEventListener<DayPickerPagerAdapter> mDaySelectionEventListener;

    private final DayPicker mDayPicker;

    // used in resolving start/end dates during range selection
    private final SelectedDate mTempSelectedDay = new SelectedDate(Calendar.getInstance());

    public DayPickerPagerAdapter(@NonNull Context context, @LayoutRes int layoutResId,
                                 @IdRes int calendarViewId) {
        mInflater = LayoutInflater.from(context);
        mLayoutResId = layoutResId;
        mCalendarViewId = calendarViewId;

        final TypedArray ta = context.obtainStyledAttributes(new int[]{
                R.attr.colorControlHighlight});
        mDayHighlightColor = ta.getColorStateList(0);
        ta.recycle();
        mDayPicker = new DayPicker();
    }

    public void setRange(@NonNull Calendar min, @NonNull Calendar max) {
        mDayPicker.setRange(min, max);
        // Positions are now invalid, clear everything and start over.
        notifyDataSetChanged();
    }

    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are
     *                  {@link Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     */
    public void setFirstDayOfWeek(int weekStart) {
        mDayPicker.setFirstDayOfWeek(weekStart);

        // Update displayed views.
        final int count = mItems.size();
        for (int i = 0; i < count; i++) {
            final SimpleMonthView monthView = mItems.valueAt(i).calendar;
            monthView.setFirstDayOfWeek(weekStart);
        }
    }


    public int getFirstDayOfWeek() {
        return mDayPicker.getFirstDayOfWeek();
    }

    /**
     * Sets the selected day.
     *
     * @param day the selected day
     */
    public void setSelectedDay(@Nullable SelectedDate day) {
        final int[] oldPosition = mDayPicker.getPositionsForDay(mDayPicker.getSelectedDay());
        final int[] newPosition = mDayPicker.getPositionsForDay(day);

        boolean shouldClearOldPosition = oldPosition != null;

        // Clear the old position if necessary.
        if (shouldClearOldPosition) {
            for (int i = oldPosition[0]; i <= oldPosition[oldPosition.length - 1]; i++) {
                final ViewHolder oldMonthView = mItems.get(i, null);
                if (oldMonthView != null) {
                    oldMonthView.calendar.setSelectedDays(-1, -1, SelectedDate.Type.SINGLE);

                }
            }
        }

        // Set the new position.
        if (newPosition != null) {
            if (newPosition.length == 1) {
                final ViewHolder newMonthView = mItems.get(newPosition[0], null);
                if (newMonthView != null) {
                    final int dayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                    newMonthView.calendar.setSelectedDays(dayOfMonth, dayOfMonth, SelectedDate.Type.SINGLE);
                }
            } else if (newPosition.length == 2) {
                boolean rangeIsInSameMonth = newPosition[0] == newPosition[1];

                if (rangeIsInSameMonth) {
                    final ViewHolder newMonthView = mItems.get(newPosition[0], null);
                    if (newMonthView != null) {
                        final int startDayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                        final int endDayOfMonth = day.getSecondDate().get(Calendar.DAY_OF_MONTH);

                        newMonthView.calendar.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }
                } else {
                    // Deal with starting month
                    final ViewHolder newMonthViewStart = mItems.get(newPosition[0], null);
                    if (newMonthViewStart != null) {
                        final int startDayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                        // TODO: Check this
                        final int endDayOfMonth = day.getFirstDate().getActualMaximum(Calendar.DATE);

                        newMonthViewStart.calendar.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }

                    for (int i = newPosition[0] + 1; i < newPosition[1]; i++) {
                        final ViewHolder newMonthView = mItems.get(i, null);
                        if (newMonthView != null) {
                            newMonthView.calendar.selectAllDays();
                        }
                    }

                    // Deal with ending month
                    final ViewHolder newMonthViewEnd = mItems.get(newPosition[1], null);
                    if (newMonthViewEnd != null) {
                        final int startDayOfMonth = day.getSecondDate().getMinimum(Calendar.DATE);
                        // TODO: Check this
                        final int endDayOfMonth = day.getSecondDate().get(Calendar.DAY_OF_MONTH);

                        newMonthViewEnd.calendar.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }
                }
            }
        }

        mDayPicker.setSelectedDay(day);
    }

    /**
     * Sets the listener to call when the user selects a day.
     *
     * @param listener The listener to call.
     */
    public void setDaySelectionEventListener(DaySelectionEventListener listener) {
        mDaySelectionEventListener = listener;
    }

    @SuppressWarnings("unused")
    void setCalendarTextColor(ColorStateList calendarTextColor) {
        mCalendarTextColor = calendarTextColor;
    }

    void setDaySelectorColor(ColorStateList selectorColor) {
        mDaySelectorColor = selectorColor;
    }

    void setMonthTextAppearance(int resId) {
        mMonthTextAppearance = resId;
    }

    void setDayOfWeekTextAppearance(int resId) {
        mDayOfWeekTextAppearance = resId;
    }

    int getDayOfWeekTextAppearance() {
        return mDayOfWeekTextAppearance;
    }

    void setDayTextAppearance(int resId) {
        mDayTextAppearance = resId;
    }

    int getDayTextAppearance() {
        return mDayTextAppearance;
    }

    @Override
    public int getCount() {
        return mDayPicker.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        final ViewHolder holder = (ViewHolder) object;
        return view == holder.container;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View itemView = mInflater.inflate(mLayoutResId, container, false);

        final SimpleMonthView v = (SimpleMonthView) itemView.findViewById(mCalendarViewId);
        v.setOnDayClickListener(mOnDayClickListener);
        v.setMonthTextAppearance(mMonthTextAppearance);
        v.setDayTextAppearance(mDayTextAppearance);

        if (mDaySelectorColor != null) {
            v.setDaySelectorColor(mDaySelectorColor);
        }

        if (mDayHighlightColor != null) {
            v.setDayHighlightColor(mDayHighlightColor);
        }

        if (mCalendarTextColor != null) {
            v.setMonthTextColor(mCalendarTextColor);
            v.setDayTextColor(mCalendarTextColor);
        }

        final int month = mDayPicker.getMonthForPosition(position);
        final int year = mDayPicker.getYearForPosition(position);

        final int[] selectedDay = mDayPicker.resolveSelectedDayBasedOnType(month, year);

        final int enabledDayRangeStart = mDayPicker.getEnabledDayRangeStart(month, year);

        final int enabledDayRangeEnd = mDayPicker.getEnabledDayRangeEnd(month, year);

        if (Config.DEBUG) {
            Log.i(TAG, "mSelectedDay.getType(): " + (mDayPicker.getSelectedDay() != null ? mDayPicker.getSelectedDay().getType() : null));
        }

        v.setMonthParams(month, year, mDayPicker.getFirstDayOfWeek(),
                enabledDayRangeStart, enabledDayRangeEnd, selectedDay[0], selectedDay[1],
                mDayPicker.getSelectedDay() != null ? mDayPicker.getSelectedDay().getType() : null);

        final ViewHolder holder = new ViewHolder(position, itemView, v);
        mItems.put(position, holder);

        container.addView(itemView);

        return holder;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        final ViewHolder holder = (ViewHolder) object;
        container.removeView(holder.container);

        mItems.remove(position);
    }

    @Override
    public int getItemPosition(Object object) {
        final ViewHolder holder = (ViewHolder) object;
        return holder.position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final SimpleMonthView v = mItems.get(position).calendar;
        if (v != null) {
            return v.getTitle();
        }
        return null;
    }

    private final SimpleMonthView.OnDayClickListener mOnDayClickListener = new SimpleMonthView.OnDayClickListener() {
        @Override
        public void onDayClick(SimpleMonthView view, Calendar day) {
            if (day != null) {
                if (mDaySelectionEventListener != null) {
                    mDaySelectionEventListener.onDaySelected(DayPickerPagerAdapter.this, day);
                }
            }
        }
    };

    private static class ViewHolder {
        public final int position;
        public final View container;
        public final SimpleMonthView calendar;

        public ViewHolder(int position, View container, SimpleMonthView calendar) {
            this.position = position;
            this.container = container;
            this.calendar = calendar;
        }
    }

    public SelectedDate resolveStartDateForRange(int x, int y, int position) {
        if (position >= 0) {
            final ViewHolder newMonthView = mItems.get(position, null);
            if (newMonthView != null) {
                final int dayOfMonth = newMonthView.calendar.getDayAtLocation(x, y);
                Calendar selectedDayStart = newMonthView.calendar.composeDate(dayOfMonth);
                if (selectedDayStart != null) {
                    mTempSelectedDay.setDate(selectedDayStart);
                    return mTempSelectedDay;
                }
            }
        }

        return null;
    }

    public SelectedDate resolveEndDateForRange(int x, int y, int position, boolean updateIfNecessary) {
        if (position >= 0) {
            final ViewHolder newMonthView = mItems.get(position, null);
            if (newMonthView != null) {
                final int dayOfMonth = newMonthView.calendar.getDayAtLocation(x, y);
                Calendar selectedDayEnd = newMonthView.calendar.composeDate(dayOfMonth);

                if (selectedDayEnd != null && (!updateIfNecessary
                        || mDayPicker.getSelectedDay().getSecondDate().getTimeInMillis() != selectedDayEnd.getTimeInMillis())) {
                    mTempSelectedDay.setSecondDate(selectedDayEnd);
                    return mTempSelectedDay;
                }
            }
        }

        return null;
    }


    public void onDateRangeSelectionStarted(SelectedDate selectedDate) {
        if (mDaySelectionEventListener != null) {
            mDaySelectionEventListener.onDateRangeSelectionStarted(selectedDate);
        }
    }

    public void onDateRangeSelectionEnded(SelectedDate selectedDate) {
        if (mDaySelectionEventListener != null) {
            mDaySelectionEventListener.onDateRangeSelectionEnded(selectedDate);
        }
    }

    public void onDateRangeSelectionUpdated(SelectedDate selectedDate) {
        if (mDaySelectionEventListener != null) {
            mDaySelectionEventListener.onDateRangeSelectionUpdated(selectedDate);
        }
    }

}
