package com.appeaser.sublimepickerlibrary.datepicker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.appeaser.sublimepickerlibrary.utilities.SUtils;

import java.util.Calendar;

class DayPicker {

    private static final int MONTHS_IN_YEAR = 12;

    private final Calendar mMinDate = Calendar.getInstance();
    private final Calendar mMaxDate = Calendar.getInstance();

    private SelectedDate mSelectedDay = null;

    private int mCount;
    private int mFirstDayOfWeek;

    void setRange(@NonNull Calendar min, @NonNull Calendar max) {
        mMinDate.setTimeInMillis(min.getTimeInMillis());
        mMaxDate.setTimeInMillis(max.getTimeInMillis());

        final int diffYear = mMaxDate.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int diffMonth = mMaxDate.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        mCount = diffMonth + MONTHS_IN_YEAR * diffYear + 1;
    }

    int getFirstDayOfWeek() {
        return mFirstDayOfWeek;
    }

    public int getCount() {
        return mCount;
    }

    int getMonthForPosition(int position) {
        return (position + mMinDate.get(Calendar.MONTH)) % MONTHS_IN_YEAR;
    }

    int getYearForPosition(int position) {
        final int yearOffset = (position + mMinDate.get(Calendar.MONTH)) / MONTHS_IN_YEAR;
        return yearOffset + mMinDate.get(Calendar.YEAR);
    }

    @SuppressWarnings("unused")
    private int getPositionForDay(@Nullable Calendar day) {
        if (day == null) {
            return -1;
        }

        final int yearOffset = day.get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
        final int monthOffset = day.get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
        return (yearOffset * MONTHS_IN_YEAR + monthOffset);
    }

    int[] getPositionsForDay(@Nullable SelectedDate day) {
        if (day == null) {
            return null;
        }

        SelectedDate.Type typeOfDay = day.getType();
        int[] positions = null;

        if (typeOfDay == SelectedDate.Type.SINGLE) {
            positions = new int[1];
            final int yearOffset = day.getFirstDate().get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
            final int monthOffset = day.getFirstDate().get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
            positions[0] = yearOffset * MONTHS_IN_YEAR + monthOffset;
        } else if (typeOfDay == SelectedDate.Type.RANGE) {
            positions = new int[2];
            final int yearOffsetFirstDate = day.getFirstDate().get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
            final int monthOffsetFirstDate = day.getFirstDate().get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
            positions[0] = yearOffsetFirstDate * MONTHS_IN_YEAR + monthOffsetFirstDate;

            final int yearOffsetSecondDate = day.getSecondDate().get(Calendar.YEAR) - mMinDate.get(Calendar.YEAR);
            final int monthOffsetSecondDate = day.getSecondDate().get(Calendar.MONTH) - mMinDate.get(Calendar.MONTH);
            positions[1] = yearOffsetSecondDate * MONTHS_IN_YEAR + monthOffsetSecondDate;
        }

        return positions;
    }

    private int[] resolveSelectedDayForTypeRange(int month, int year) {
        // Quan: "year.month" Eg: Feb, 2015 ==> 2015.02, Dec, 2000 ==> 2000.12
        float startDateQuan = mSelectedDay.getStartDate().get(Calendar.YEAR)
                + (mSelectedDay.getStartDate().get(Calendar.MONTH) + 1) / 100f;
        float endDateQuan = mSelectedDay.getEndDate().get(Calendar.YEAR)
                + (mSelectedDay.getEndDate().get(Calendar.MONTH) + 1) / 100f;

        float dateQuan = year + (month + 1) / 100f;

        if (dateQuan >= startDateQuan && dateQuan <= endDateQuan) {
            int startDay, endDay;
            if (dateQuan == startDateQuan) {
                startDay = mSelectedDay.getStartDate().get(Calendar.DAY_OF_MONTH);
            } else {
                startDay = 1;
            }

            if (dateQuan == endDateQuan) {
                endDay = mSelectedDay.getEndDate().get(Calendar.DAY_OF_MONTH);
            } else {
                endDay = SUtils.getDaysInMonth(month, year);
            }

            return new int[]{startDay, endDay};
        }

        return new int[]{-1, -1};
    }

    int[] resolveSelectedDayBasedOnType(int month, int year) {
        if (mSelectedDay == null) {
            return new int[]{-1, -1};
        }

        if (mSelectedDay.getType() == SelectedDate.Type.SINGLE) {
            return resolveSelectedDayForTypeSingle(month, year);
        } else if (mSelectedDay.getType() == SelectedDate.Type.RANGE) {
            return resolveSelectedDayForTypeRange(month, year);
        }

        return new int[]{-1, -1};
    }

    private int[] resolveSelectedDayForTypeSingle(int month, int year) {
        if (mSelectedDay.getFirstDate().get(Calendar.MONTH) == month
                && mSelectedDay.getFirstDate().get(Calendar.YEAR) == year) {
            int resolvedDay = mSelectedDay.getFirstDate().get(Calendar.DAY_OF_MONTH);
            return new int[]{resolvedDay, resolvedDay};
        }

        return new int[]{-1, -1};
    }

    int getEnabledDayRangeStart(double month, double year) {
        if (mMinDate.get(Calendar.MONTH) == month && mMinDate.get(Calendar.YEAR) == year) {
            return mMinDate.get(Calendar.DAY_OF_MONTH);
        } else {
            return 1;
        }
    }

    int getEnabledDayRangeEnd(double month, double year) {
        if (mMaxDate.get(Calendar.MONTH) == month && mMaxDate.get(Calendar.YEAR) == year) {
            return mMaxDate.get(Calendar.DAY_OF_MONTH);
        } else {
            return 31;
        }
    }

    void setFirstDayOfWeek(int mFirstDayOfWeek) {
        this.mFirstDayOfWeek = mFirstDayOfWeek;
    }

    SelectedDate getSelectedDay() {
        return mSelectedDay;
    }

    void setSelectedDay(SelectedDate selectedDay) {
        mSelectedDay = selectedDay;
    }
}
