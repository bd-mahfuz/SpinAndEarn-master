package com.kcirqueit.spinandearn.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {


    private static final String DATE_FORMAT = "dd-MM-yyyy";

    public static String getCurrentDateString() {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(new Date());

    }

    public static Date getDateToString(String dateString) {

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date date = dateFormat.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();

            return null;
        }

    }





}
