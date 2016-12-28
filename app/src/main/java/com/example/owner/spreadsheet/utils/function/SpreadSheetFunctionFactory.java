package com.example.owner.spreadsheet.utils.function;

import static com.example.owner.spreadsheet.utils.function.AVG.EXCEl_FUNCTION_NAME_AVG;
import static com.example.owner.spreadsheet.utils.function.SpreadSheetFunction.EXCEL_FUNCTION_START_CHAR;
import static com.example.owner.spreadsheet.utils.function.SpreadSheetFunction.EXCEL_FUNCTION_OPENER;

public class SpreadSheetFunctionFactory {

    public static SpreadSheetFunction getExcelFunction(String expression) {

        // ex)=SUM 이라고 시작하는 function 에서 '='을 빼는 작업.
        if (expression.startsWith(EXCEL_FUNCTION_START_CHAR)) {
            expression = expression.substring(1);
        }

        final int functionDataStart = expression.indexOf(EXCEL_FUNCTION_OPENER);
        if (functionDataStart > 0) {
            String spreadSheetFunction = expression.substring(0, functionDataStart);

            if (spreadSheetFunction.equals(SUM.EXCEL_FUNCTION_NAME_SUM)) {
                return new SUM(expression);
            } else if (spreadSheetFunction.equals(EXCEl_FUNCTION_NAME_AVG)) {
                return new AVG(expression);
            }

        }
        return null;
    }

    // (===> util?)
    public static boolean isInteger(String cellValue) {
        boolean isInteger = true;
        for (int i = 0; i < cellValue.length(); i++) {
            if (cellValue.contains(".")) {
                isInteger = false;
                break;
            }
        }
        return isInteger;
    }
}
