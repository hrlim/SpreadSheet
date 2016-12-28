package com.example.owner.spreadsheet.model;

import com.example.owner.spreadsheet.exception.DuplicateSheetNameException;
import com.example.owner.spreadsheet.utils.listener.ModelDataChangeListener;

import static com.example.owner.spreadsheet.model.Sheet.DEFAULT_NUM_COLUMNS;
import static com.example.owner.spreadsheet.model.Sheet.DEFAULT_NUM_ROWS;
import static com.example.owner.spreadsheet.view.WorkBookView.SECTION_ALL;
import static com.example.owner.spreadsheet.view.WorkBookView.SECTION_SHEET_LIST;

public class WorkBook {

    private SheetManager sheetManager = SheetManager.getInstance();

    public WorkBook() {
        sheetManager.clear();
    }

    public Sheet createSheet() {
        return this.createSheet(DEFAULT_NUM_COLUMNS, DEFAULT_NUM_ROWS);
    }

    // sheetManager 에 접근하여 인자의 값을 갖는 sheet 를 생성합니다.
    public Sheet createSheet(int maxColumn, int maxRow) {
        Sheet sheet = sheetManager.createSheet(maxColumn, maxRow);
        // Model 이 변경되었음을 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_ALL);
        return sheet;
    }

    public Sheet createSheet(String sheetName) {
        return this.createSheet(sheetName, DEFAULT_NUM_COLUMNS, DEFAULT_NUM_ROWS);
    }

    // sheetManager 에 접근하여 인자의 값을 갖는 sheet 를 생성합니다.
    public Sheet createSheet(String sheetName, int maxColumn, int maxRow) {
        Sheet sheet = sheetManager.createSheet(sheetName, maxColumn, maxRow);
        // Model 이 변경되었음을 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_ALL);
        return sheet;
    }

    // sheetManager 에 접근하여 인자의 값을 갖는 sheet 를 삭제합니다.
    public Sheet deleteSheet(int sheetId) {
        Sheet sheet = sheetManager.deleteSheet(sheetId);
        // Model 이 변경되었음을 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_ALL);
        return sheet ;
    }

    // sheetManager 에 접근하여 생성된 sheetId 들을 리턴합니다.
    public int[] getAllSheetIds() {
        return sheetManager.getAllSheetId();
    }

    // sheetManager 에 접근하여 인자의 값을 갖는 sheet 를 리턴합니다.
    public Sheet getSheet(int id) {
        return sheetManager.getSheet(id);
    }

    // sheetManager 에 접근하여 인자의 값을 갖는 sheet 를 리턴합니다.
    public Sheet setSheetName(String newSheetName, int sheetId) throws DuplicateSheetNameException {
        Sheet sheet = sheetManager.setName(newSheetName, sheetId);
        // Model 이 변경되었음을 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_SHEET_LIST);
        return sheet;
    }

    // sheetManager 에 접근하여 인자의 값을 가지는 sheet 를 리턴합니다.
    public Sheet changeSheetOrder(int sheetId, int newOrder) {
        Sheet sheet = sheetManager.changeSheetOrder(sheetId, newOrder);
        // Model 이 변경되었음을 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_ALL);
        return sheet;
    }
}
