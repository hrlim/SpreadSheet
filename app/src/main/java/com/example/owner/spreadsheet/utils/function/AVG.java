package com.example.owner.spreadsheet.utils.function;

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

        // 계산해야할 두 수중 하나의 수가 정수 형태가 아니라면(실수 타입) 출력 또한 실수 형태로 넘긴다.
        if(SpreadSheetFunctionFactory.isInteger(firstNumber) || SpreadSheetFunctionFactory.isInteger(secondNumber)) {
            return Double.toString(((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber))) / 2);
        }
        // 계산해야할 두 수가 모두 정수형태라면 아니라면 실수 타입으로 리턴한다.
        if(!SpreadSheetFunctionFactory.isInteger(firstNumber) && !SpreadSheetFunctionFactory.isInteger(secondNumber)){
            return Double.toString(((Double.parseDouble(firstNumber) + Double.parseDouble(secondNumber))) / 2);
        }
        // 계산해야할 두수가 모두 정수형태라면 정수타입을 리턴.
        return Integer.toString(((Integer.parseInt(firstNumber) + Integer.parseInt(secondNumber)))/2);
    }
}