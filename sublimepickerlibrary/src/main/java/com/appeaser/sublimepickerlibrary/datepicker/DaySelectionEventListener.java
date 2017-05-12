package com.appeaser.sublimepickerlibrary.datepicker;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

interface DaySelectionEventListener<T> {
    void onDaySelected(T view, Calendar day);

    void onDateRangeSelectionStarted(@NonNull SelectedDate selectedDate);

    void onDateRangeSelectionEnded(@Nullable SelectedDate selectedDate);

    void onDateRangeSelectionUpdated(@NonNull SelectedDate selectedDate);
}
