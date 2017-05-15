package com.appeaser.sublimepickerlibrary.datepicker;

import android.content.res.ColorStateList;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

class DayPickerRecyclerViewAdapter extends
        RecyclerView.Adapter<DayPickerRecyclerViewAdapter.ViewHolder> {
    private final DayPicker mDayPicker;
    private final int mLayoutResId;
    private final int mCalendarViewId;

    private DaySelectionEventListener<DayPickerRecyclerViewAdapter> mDaySelectionEventListener;
    private final SimpleMonthView.OnDayClickListener mOnDayClickListener = new SimpleMonthView.OnDayClickListener() {
        @Override
        public void onDayClick(SimpleMonthView view, Calendar day) {
            if (day != null) {
                if (mDaySelectionEventListener != null) {
                    mDaySelectionEventListener.onDaySelected(DayPickerRecyclerViewAdapter.this, day);
                    if (mTempSelectedDay.getType() == SelectedDate.Type.RANGE) {
                        mTempSelectedDay.setDate(day);
                        onDateRangeSelectionStarted(mTempSelectedDay);
                    }
                    mTempSelectedDay.setSecondDate(day);
                    onDateRangeSelectionEnded(mTempSelectedDay);
                }
            }
        }
    };
    private LayoutInflater mInflater;
    private int mDayTextAppearance;
    private int mMonthTextAppearance;
    private ColorStateList mDaySelectorColor;

    // used in resolving start/end dates during range selection
    private final SelectedDate mTempSelectedDay = new SelectedDate(Calendar.getInstance());
    private RecyclerView mRecyclerView;

    DayPickerRecyclerViewAdapter(@LayoutRes int mLayoutResId, @IdRes int mCalendarViewId) {
        this.mLayoutResId = mLayoutResId;
        this.mCalendarViewId = mCalendarViewId;
        mDayPicker = new DayPicker();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
        mInflater = LayoutInflater.from(recyclerView.getContext());
    }

    @Override
    public DayPickerRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(mLayoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(DayPickerRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.monthView.setOnDayClickListener(mOnDayClickListener);

        final int month = mDayPicker.getMonthForPosition(position);
        final int year = mDayPicker.getYearForPosition(position);

        final int[] selectedDay = mDayPicker.resolveSelectedDayBasedOnType(month, year);

        final int enabledDayRangeStart = mDayPicker.getEnabledDayRangeStart(month, year);
        final int enabledDayRangeEnd = mDayPicker.getEnabledDayRangeEnd(month, year);

        holder.monthView.setMonthParams(month, year, mDayPicker.getFirstDayOfWeek(),
                enabledDayRangeStart, enabledDayRangeEnd, selectedDay[0], selectedDay[1],
                mDayPicker.getSelectedDay() != null ? mDayPicker.getSelectedDay().getType() : null);
        holder.monthView.setMonthTextAppearance(mMonthTextAppearance);
        holder.monthView.setDayTextAppearance(mDayTextAppearance);

        if (mDaySelectorColor != null) {
            holder.monthView.setDaySelectorColor(mDaySelectorColor);
        }
    }

    void setRange(@NonNull Calendar min, @NonNull Calendar max) {
        mDayPicker.setRange(min, max);
        notifyDataSetChanged();
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
                final ViewHolder oldMonthView = getViewHolderByPosition(i);
                if (oldMonthView != null) {
                    oldMonthView.monthView.setSelectedDays(-1, -1, SelectedDate.Type.SINGLE);

                }
            }
        }

        // Set the new position.
        if (newPosition != null) {
            if (newPosition.length == 1) {
                final ViewHolder newMonthView = getViewHolderByPosition(newPosition[0]);
                if (newMonthView != null) {
                    final int dayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                    newMonthView.monthView.setSelectedDays(dayOfMonth, dayOfMonth, SelectedDate.Type.SINGLE);
                }
            } else if (newPosition.length == 2) {
                boolean rangeIsInSameMonth = newPosition[0] == newPosition[1];

                if (rangeIsInSameMonth) {
                    final ViewHolder newMonthView = getViewHolderByPosition(newPosition[0]);
                    if (newMonthView != null) {
                        final int startDayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                        final int endDayOfMonth = day.getSecondDate().get(Calendar.DAY_OF_MONTH);

                        newMonthView.monthView.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }
                } else {
                    // Deal with starting month
                    final ViewHolder newMonthViewStart = getViewHolderByPosition(newPosition[0]);
                    if (newMonthViewStart != null) {
                        final int startDayOfMonth = day.getFirstDate().get(Calendar.DAY_OF_MONTH);
                        // TODO: Check this
                        final int endDayOfMonth = day.getFirstDate().getActualMaximum(Calendar.DATE);

                        newMonthViewStart.monthView.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }

                    for (int i = newPosition[0] + 1; i < newPosition[1]; i++) {
                        final ViewHolder newMonthView = getViewHolderByPosition(i);
                        if (newMonthView != null) {
                            newMonthView.monthView.selectAllDays();
                        }
                    }

                    // Deal with ending month
                    final ViewHolder newMonthViewEnd = getViewHolderByPosition(newPosition[1]);
                    if (newMonthViewEnd != null) {
                        final int startDayOfMonth = day.getSecondDate().getMinimum(Calendar.DATE);
                        // TODO: Check this
                        final int endDayOfMonth = day.getSecondDate().get(Calendar.DAY_OF_MONTH);

                        newMonthViewEnd.monthView.setSelectedDays(startDayOfMonth, endDayOfMonth, SelectedDate.Type.RANGE);
                    }
                }
            }
        }

        mDayPicker.setSelectedDay(day);
    }

    @Override
    public int getItemCount() {
        return mDayPicker.getCount();
    }

    public void setDaySelectionEventListener(DaySelectionEventListener<DayPickerRecyclerViewAdapter> mDaySelectionEventListener) {
        this.mDaySelectionEventListener = mDaySelectionEventListener;
    }

    public int getFirstDayOfWeek() {
        return mDayPicker.getFirstDayOfWeek();
    }

    /**
     * Sets the first day of the week.
     *
     * @param weekStart which day the week should start on, valid values are
     *                  {@link Calendar#SUNDAY} through {@link Calendar#SATURDAY}
     */
    void setFirstDayOfWeek(int weekStart) {
        mDayPicker.setFirstDayOfWeek(weekStart);
        notifyDataSetChanged();
    }

    void setDayTextAppearance(int resId) {
        mDayTextAppearance = resId;
    }

    int getDayTextAppearance() {
        return mDayTextAppearance;
    }

    public void setMonthTextAppearance(int monthTextAppearance) {
        this.mMonthTextAppearance = monthTextAppearance;
    }

    void setDaySelectorColor(ColorStateList selectorColor) {
        mDaySelectorColor = selectorColor;
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

    private ViewHolder getViewHolderByPosition(int position) {
        return (ViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SimpleMonthView monthView;

        ViewHolder(View itemView) {
            super(itemView);
            monthView = (SimpleMonthView) itemView.findViewById(mCalendarViewId);
        }
    }
}
