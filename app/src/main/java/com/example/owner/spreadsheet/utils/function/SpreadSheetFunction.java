package com.example.owner.spreadsheet.utils.function;

public abstract class SpreadSheetFunction {
    public static final String EXCEL_FUNCTION_START_CHAR = "=";
    static final String EXCEL_FUNCTION_OPENER = "(";
    static final String EXCEL_FUNCTION_SEPARATOR = ",";
    static final String EXCEL_FUNCTION_CLOSER = ")";

    public abstract String getResult();
}
