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

        // 계산해야할 두 수중 하나의 수가 정수 형태가 아니라면(실수 타입) 출력 또한 실수 형태로 넘긴다.
        if(!isInteger(firstNumber) || !isInteger(secondNumber)) {
            return Double.toString((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber)));
        }
        // 계산해야할 두 수가 모두 정수형태라면 아니라면 실수 타입으로 리턴한다.
        if(!isInteger(firstNumber) && !isInteger(secondNumber)){
            return Double.toString((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber)));
        }
        // 계산해야할 두수가 모두 정수형태라면 정수타입을 리턴.
        return Integer.toString((Integer.parseInt(firstNumber) + Integer.parseInt(secondNumber)));
    }
}
