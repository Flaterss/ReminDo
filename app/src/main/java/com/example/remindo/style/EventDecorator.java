package com.example.remindo.style;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> datesWithEvents = new HashSet<>();
    private final Drawable drawable;

    public EventDecorator(Collection<CalendarDay> dates, int color) {
        this.datesWithEvents.addAll(dates);
        this.drawable = DrawableHelper.getCircleDrawable(color);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return datesWithEvents.contains(day);
    }

    @Override
    public void decorate(@NonNull DayViewFacade view) {
        view.setBackgroundDrawable(drawable);
    }
}
