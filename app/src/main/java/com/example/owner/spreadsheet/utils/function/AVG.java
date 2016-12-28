package com.example.owner.spreadsheet.utils.function;

import static com.example.owner.spreadsheet.utils.function.SpreadSheetFunctionFactory.isInteger;

public class AVG extends SpreadSheetFunction {
    static final String EXCEl_FUNCTION_NAME_AVG = "AVG";
    private String expression;

    public AVG(String avgExpression) {
        this.expression = avgExpression;
    }

    @Override
    public String getResult() {
        String firstNumber;
        String secondNumber;

        firstNumber = expression.substring(expression.indexOf(EXCEL_FUNCTION_OPENER) + 1, expression.indexOf(EXCEL_FUNCTION_SEPARATOR));
        secondNumber = expression.substring(expression.indexOf(EXCEL_FUNCTION_SEPARATOR) + 1, expression.length() - 1);

        if(isInteger(firstNumber) || isInteger(secondNumber)) {
            return Double.toString(((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber))) / 2);
        }
        if(!isInteger(firstNumber) && !isInteger(secondNumber)){
            return Double.toString(((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber))) / 2);
        }
        return Integer.toString(((Integer.parseInt(firstNumber) + Integer.parseInt(secondNumber)))/2);
    }
}