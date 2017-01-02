package com.example.owner.spreadsheet.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.owner.spreadsheet.model.Cell;
import com.example.owner.spreadsheet.model.Sheet;
import com.example.owner.spreadsheet.model.WorkBook;

public class Hana {

    private final static String TAG = "Hana";
    private final static String HEADER_AREA = "header:";
    private final static String CONTENT_AREA = "content:";

    public final static String HANA_EXTENSION = ".hana";
    private final static String HEADER_ATTRIBUTE_CELL_SEPARATER = "cellseparator=";
    private final static String HEADER_ATTRIBUTE_FISRT_ROW = "firstrow=";
    private final static String HEADER_ATTRIBUTE_FISRT_COLUMN = "firstcolumn=";

    private char cellSeparator;
    private int firstRow;
    private int firstColumn;

    public WorkBook readHana(InputStream inputStream) throws IOException {
        WorkBook workBook = new WorkBook();
        Sheet sheet = null;
        String line;

        String cellValue;
        int cellType;


        boolean header = false;
        boolean content = false;

        String sheetName = null;
        try {
            int readColumn;
            int readRow = 0;

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
            while ((line = bufferedReader.readLine()) != null) {

                if (line.length() != 0) {
                    if (line.charAt(line.length() - 1) == '{') {
                        line = line.substring(0, line.length() - 1);
                        sheetName = line;
                        sheet = workBook.createSheet(line);
                        Log.v(TAG, "SheetName :  " + sheetName);
                    }
                    if (line.charAt(0) == '}') {
                        readRow = 0;
                        Log.v(TAG, "SheetName :  " + sheetName + " is finished");
                    }
                }
                if (line.toLowerCase().startsWith(HEADER_AREA)) {
                    header = true;
                    content = false;
                    line = bufferedReader.readLine();
                }

                if (line.toLowerCase().startsWith(CONTENT_AREA)) {
                    header = false;
                    content = true;
                    line = bufferedReader.readLine();
                }

                if (header && !content) {
                    if (line.toLowerCase().startsWith(HEADER_ATTRIBUTE_CELL_SEPARATER)) {
                        cellSeparator = line.charAt((HEADER_ATTRIBUTE_CELL_SEPARATER.length()));
                    }
                    if (line.toLowerCase().startsWith(HEADER_ATTRIBUTE_FISRT_ROW)) {
                        firstRow = Integer.parseInt(line.substring(HEADER_ATTRIBUTE_FISRT_ROW.length()));
                    }
                    if (line.toLowerCase().startsWith(HEADER_ATTRIBUTE_FISRT_COLUMN)) {
                        firstColumn = Integer.parseInt(line.substring(HEADER_ATTRIBUTE_FISRT_COLUMN.length()));
                    }
                    readRow = 0;
                }

                if (!header && content) {
                    String tempCellV;
                    // ,,{1,1},{2,1},{3,1},{1,2},{2,2},{Hello,2},{A,2},{B,2},{C,2},
                    Pattern pattern = Pattern.compile("(\\{(.*?),[1-3]\\})?(,)?");
                    readColumn = 0;
                    Matcher matcher = pattern.matcher(line);
                    while (matcher.find()) {
                        int commaIndex = 0;
                        if (matcher.group().length() > 1) {
                            String eachCell = matcher.group();
                            if (eachCell.charAt(eachCell.length() - 1) == ',') {
                                eachCell = eachCell.substring(0, eachCell.length() - 1);
                            }
                            for (int i = eachCell.length() - 1; i > 0; i--) {
                                if (eachCell.charAt(i) == ',') {
                                    commaIndex = i;
                                    break;
                                }
                            }
                            tempCellV = eachCell.substring(1, commaIndex);
                            if (tempCellV.charAt(0) == '"') {
                                cellValue = eachCell.substring(2, commaIndex - 1);
                            } else {
                                cellValue = tempCellV;
                            }
                            cellType = Integer.parseInt(eachCell.substring(commaIndex + 1, eachCell.length() - 1));

                            sheet.createCell((firstColumn + readColumn), (firstRow + readRow), cellValue, cellType);
                            Log.v(TAG, "[" + (firstColumn + readColumn) + "," + (firstRow + readRow) + "]      (" + cellValue + "," + cellType + ")");
                        }
                        readColumn++;
                    }
                    readRow++;
                }
            }
            bufferedReader.close();


        } catch (Exception e) {
            e.printStackTrace();

        }
        return workBook;
    }

    public void saveHana(String fileName, WorkBook workBook, Context context) throws Exception {
        String dirPath;
        StringBuilder sb = new StringBuilder();

        for (int i : workBook.getAllSheetIds()) {


            sb.append(workBook.getSheet(i).getName());
            sb.append("{");
            sb.append(System.getProperty("line.separator"));

            sb.append(HEADER_AREA);
            sb.append(System.getProperty("line.separator"));

            sb.append(HEADER_ATTRIBUTE_CELL_SEPARATER);
            sb.append(",");
            sb.append(System.getProperty("line.separator"));

            int firstColumn = workBook.getSheet(i).getMaxColumn();
            int firstRow = workBook.getSheet(i).getMaxRow();
            int lastColumn = 0;
            int lastRow = 0;

            Cell cell;

            Log.v("sheet", "firstBlockColumn : " + firstColumn);
            Log.v("sheet", "firstBlockRow : " + firstRow);
            Log.v("sheet", "lastBlockColumn : " + lastColumn);
            Log.v("sheet", "lastBlockRow : " + lastRow);

            for (int j = 0; j < workBook.getSheet(i).getMaxRow(); j++) {
                for (int z = 0; z < workBook.getSheet(i).getMaxColumn(); z++) {
                    cell = workBook.getSheet(i).getCell(z + 1, j + 1);
                    if (cell != null) {
                        if (firstColumn > z) {
                            firstColumn = z;
                        }
                        if (firstRow > j) {
                            firstRow = j;
                        }
                        if (lastColumn < z) {
                            lastColumn = z;
                        }
                        if (lastRow < j) {
                            lastRow = j;
                        }
                    }
                }
            }

            firstColumn++;
            firstRow++;
            lastColumn++;
            lastRow++;

            Log.v("sheet", "first Block : " + firstColumn + ", " + firstRow);
            Log.v("sheet", "last Block : " + lastColumn + ", " + lastRow);
            Log.v("sheet", "first Block : " + firstRow + ", " + firstColumn);

            sb.append(HEADER_ATTRIBUTE_FISRT_COLUMN);
            sb.append(Integer.toString(firstColumn));
            sb.append(System.getProperty("line.separator"));

            sb.append(HEADER_ATTRIBUTE_FISRT_ROW);
            sb.append(Integer.toString(firstRow));
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));

            sb.append(CONTENT_AREA);
            sb.append(System.getProperty("line.separator"));

            for (int z = firstRow; z <= lastRow; z++) {
                for (int j = firstColumn; j <= lastColumn; j++) {

                    if (workBook.getSheet(i).getCell(j, z) == null) {
                        sb.append(',');
                    } else {
                        sb.append("{");
                        sb.append(workBook.getSheet(i).getCell(j, z).getValue());
                        sb.append(",");
                        sb.append(workBook.getSheet(i).getCell(j, z).getType());
                        sb.append("},");
                    }
                }
                sb.append(System.getProperty("line.separator"));
            }
            sb.append("}");
            sb.append(System.getProperty("line.separator"));
            sb.append(System.getProperty("line.separator"));
        }

        dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath += "/LimHarim";

        // sd 카드에 사용할 폴더있는지 검사후 없으면
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File hana = new File(dir, fileName + HANA_EXTENSION);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(hana);
            fileOutputStream.write(sb.toString().getBytes());
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
}




