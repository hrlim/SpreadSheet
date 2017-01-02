package com.example.owner.spreadsheet.ctrl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.InputStream;

import com.example.owner.spreadsheet.utils.exception.CellIndexOutOfBoundsException;
import com.example.owner.spreadsheet.utils.exception.DuplicateSheetNameException;
import com.example.owner.spreadsheet.model.Cell;
import com.example.owner.spreadsheet.model.Sheet;
import com.example.owner.spreadsheet.model.WorkBook;
import com.example.owner.spreadsheet.utils.Hana;
import com.example.owner.spreadsheet.utils.function.SpreadSheetFunction;
import com.example.owner.spreadsheet.utils.listener.ModelDataChangeListener;
import com.example.owner.spreadsheet.view.WorkBookView;

public class WorkBookController {

    private static final String TAG = "Controller";
    private static final int DEFAULT_SHEET_NUM = 5;
    private WorkBook workBook;
    private String fileName = null;

    public WorkBookController() {
        createWorkBook();
    }

    public void createWorkBook() {
        // sheet 를 만들 때마다 invalidate 를 하지 않기 위해 pause()사용
        ModelDataChangeListener.pause();
        workBook = new WorkBook();
        for (int i = 0; i < DEFAULT_SHEET_NUM; i++) {
            workBook.createSheet();
        }
        // sheet 를 DefaultSheetNum 인 5개를 다 만들고 invalidate 를 하기 위해 run()사용
        ModelDataChangeListener.run(WorkBookView.SECTION_RESET);
    }

    public Sheet getSheet(int sheetNum) {
        int[] allSheetIds = workBook.getAllSheetIds();
        return workBook.getSheet(allSheetIds[sheetNum]);
    }

    public int getSheetCount() {
        return workBook.getAllSheetIds().length;
    }

    public String getSheetName(int sheetNum) {
        return getSheet(sheetNum).getName();
    }

    public void addSheet() {
        workBook.createSheet();
    }

    public void setSheetName(String newSheetNmae, int sheetId) throws DuplicateSheetNameException {
        workBook.setSheetName(newSheetNmae, sheetId);
    }

    public void deleteSheet(int sheetId) {
        workBook.deleteSheet(sheetId);
    }

    public Cell getCell(int sheetNum, int column, int row) throws CellIndexOutOfBoundsException {
        return getSheet(sheetNum).getCell(column, row);
    }

    public Cell deleteCell(int sheetNum, int column, int row) throws CellIndexOutOfBoundsException {
        return getSheet(sheetNum).deleteCell(column, row);
    }


    public void changeSheetOrder(int sheetId, int newOrder) {
        workBook.changeSheetOrder(sheetId, newOrder);
    }

    // view에서 입력받은 값을 cellstorage 에 저장하는 메소드
    // createCell 에서 CellIndexOutOfBoundsException 이 발생할 수 있다.
    public void setCell(int sheetNum, int column, int row, String value) throws CellIndexOutOfBoundsException {
        int type = validateType(value);

        // (===> 값을 비교한 후에 다르면 삭제하는 것이 좋을 것 같음)
        if (value != null) {
            deleteCell(sheetNum, column, row);
            getSheet(sheetNum).createCell(column, row, value, type);
        } else {
            getSheet(sheetNum).deleteCell(column, row);
        }
    }

    // String 타입인 value 를 통해 String, Number, Function 인지에 대한 cellType 을 반환하는 메소드
    private int validateType(String value) {
        if (value.startsWith(SpreadSheetFunction.EXCEL_FUNCTION_START_CHAR)) {
            return Cell.TYPE_FUNCTION;
        } else if (isNumber(value)) {
            return Cell.TYPE_NUMBER;
        } else {
            return Cell.TYPE_STRING;
        }
    }

    // cellValue 가 숫자로만 이루어져 있는지 확인하는 메소드.(===> util?)
    public boolean isNumber(String cellValue) {
        boolean isNumber = true;
        for (int i = 0; i < cellValue.length(); i++) {
            if (!Character.isDigit(cellValue.charAt(i))) {
                isNumber = false;
                break;
            }
        }
        return isNumber;
    }

    public int getMaxRow(int sheetNum) {
        return getSheet(sheetNum).getMaxRow();
    }

    public int getMaxColumn(int sheetNum) {
        return getSheet(sheetNum).getMaxColumn();
    }

    // 하나파일을 불러온다.
    // readHana 메소드에서 Exception 이 발생할 수 있다.
    public void readHana(Uri uri, Context context) throws Exception {
        InputStream inputStream = null;
        ContentResolver contentResolver = context.getContentResolver();

        try {
            inputStream = contentResolver.openInputStream(uri);
            Hana hana = new Hana();

            try {
                // 불러오기를 할 때 모델에 리스너로 인해 계속 invalidate 를 막기위해 pause()를 사용.
                ModelDataChangeListener.pause();
                workBook = hana.readHana(inputStream);
                // 불러오기를 끝내면 한번에 invalidate 를 하기 위해 run()를 사용.
                ModelDataChangeListener.run(WorkBookView.SECTION_RESET);

            } catch (Exception e) {
                e.printStackTrace();
                ModelDataChangeListener.run();
                Log.v(TAG, "Error, Fail to Load.");
            }
            Log.v(TAG, "LoadFileName is " + getFileName(context, uri));

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    // 하나파일을 저장한다.
    // saveHana 메소드에서 Exception 이 발생할 수 있다.
    public void saveHana(String fileName, Context context) throws Exception {
        Hana hana = new Hana();
        hana.saveHana(fileName, workBook, context);
    }

    private String getFileName(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        ContentResolver contentResolver = context.getContentResolver();

        if (uri.getScheme().equals("content") || uri.getScheme().equals("file")) {
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, null, null, null, null);
                Log.v("valie", "uri  kij sldfkj a       " + uri.toString() + "     asdf " + cursor);
                Log.v("valie", "uri  kij sldfkj a       " + uri.toString() + "     asdf " + contentResolver.toString());
                if (cursor != null && cursor.moveToFirst()) {
                    // 파일 이름을 받아옵니다.
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
