package me.fullpage.manticlib.utils;

import lombok.Getter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
public class Schedule {

    private final int year;
    private final int month;
    private final int date;
    private final int hour;
    private final int minute;
    private final int second;
    private final TimeZone timeZone;
    private final Calendar end;


    public Schedule(int year, int month, int date, int hour, int minute, int second, TimeZone timeZone) {
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.timeZone = timeZone;
        this.end = Calendar.getInstance(timeZone);
        this.end.set(Calendar.YEAR, year);
        this.end.set(Calendar.MONTH, month - 1);
        this.end.set(Calendar.DAY_OF_MONTH, date);
        this.end.set(Calendar.HOUR_OF_DAY, hour);
        this.end.set(Calendar.MINUTE, minute);
        this.end.set(Calendar.SECOND, second);
    }

    public long millisTill() {
        return this.end.getTimeInMillis() - Calendar.getInstance(timeZone).getTimeInMillis();
    }

    public String formattedTill() {
        return TimeUtil.format(this.millisTill());
    }


}
