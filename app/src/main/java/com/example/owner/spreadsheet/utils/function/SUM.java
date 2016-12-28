package com.example.owner.spreadsheet.utils.function;

import android.util.Log;

import static com.example.owner.spreadsheet.utils.function.SpreadSheetFunctionFactory.isInteger;

public class SUM extends SpreadSheetFunction {
    static final String EXCEL_FUNCTION_NAME_SUM = "SUM";
    private String expression;

    public SUM(String sumExpression) {
        this.expression = sumExpression;
    }

    @Override
    public String getResult() {
        String firstNumber;
        String secondNumber;

        firstNumber = expression.substring(expression.indexOf(EXCEL_FUNCTION_OPENER) + 1, expression.indexOf(EXCEL_FUNCTION_SEPARATOR));
        secondNumber = expression.substring(expression.indexOf(EXCEL_FUNCTION_SEPARATOR) + 1, expression.length() - 1);

        if(!isInteger(firstNumber) || !isInteger(secondNumber)) {
            return Double.toString((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber)));
        }
        if(!isInteger(firstNumber) && !isInteger(secondNumber)){
            return Double.toString((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber)));
        }
        return Integer.toString((Integer.parseInt(firstNumber) + Integer.parseInt(secondNumber)));
    }
}
