package ru.onyanov.itschanging.helpers;

import java.util.HashMap;

public class PeriodHelper {

    public static final String TYPE_MONTH = "month";
    public static final String TYPE_WEEK = "week";
    public static final String TYPE_DAY = "day";
    public static final String TYPE_HOUR = "hour";

    public static final int MAX_VALUE_MONTH = 4;
    public static final int MAX_VALUE_WEEK = 7;
    public static final int MAX_VALUE_DAY = 24;
    public static final int MAX_VALUE_HOUR = 24;

    private HashMap<String, Integer> maxValues = new HashMap<>();

    private String type = TYPE_WEEK;
    private int value = 1;

    public PeriodHelper() {
        maxValues.put(TYPE_MONTH, MAX_VALUE_MONTH);
        maxValues.put(TYPE_WEEK, MAX_VALUE_WEEK);
        maxValues.put(TYPE_DAY, MAX_VALUE_DAY);
        maxValues.put(TYPE_HOUR, MAX_VALUE_HOUR);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

        onTypeChanged();
    }

    private void onTypeChanged() {
        if (value > maxValues.get(type)) {
            setValue(maxValues.get(type));
        }
    }

}
