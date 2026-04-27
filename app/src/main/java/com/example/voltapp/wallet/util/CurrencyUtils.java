package com.example.voltapp.wallet.util;

import com.example.voltapp.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class CurrencyUtils {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        FORMATTER = new DecimalFormat("#,###", symbols);
    }

    private CurrencyUtils() {
        // Utility class
    }

    public static String formatVnd(long amount) {
        return FORMATTER.format(amount) + " đ";
    }
}
