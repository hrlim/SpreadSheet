package com.example.owner.spreadsheet.model;

import android.util.Log;
import java.util.LinkedList;

import com.example.owner.spreadsheet.utils.exception.DuplicateSheetNameException;

class SheetManager {

    private static final String TAG = "Sheet";
    // Sheet 의 순서가 중요하므로 LinkedList 로 구현하였음.
    public LinkedList<Sheet> sheets = new LinkedList<>();

    private static class SheetManagerHolder {
        private static final SheetManager INSTANCE = new SheetManager();
    }

    // 해당 객체는 여러 개 생성되어선 안 되므로 생성자에 직접 접근할 수 없어야 합니다.
    // SingleTon 으로 구현
    private SheetManager() {
    }

    // 해당 메서드를 통해 인스턴스를 가질 수 있습니다.
    public static SheetManager getInstance() {
        return SheetManagerHolder.INSTANCE;
    }

    // 인자의 값을 갖는 sheet 를 생성합니다.
    public Sheet createSheet(int maxColumn, int maxRow) {
        Sheet sheet;
        int sheetNum = 1;

        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getName().equals("sheet" + sheetNum)) {
                ++sheetNum;
                i = 0;
            }
        }
        sheet = new Sheet("sheet" + sheetNum, maxColumn, maxRow);
        sheets.addLast(sheet);
        Log.v(TAG, "New created Sheet : " + sheet.getName());
        return sheet;
    }

    // 인자의 값을 갖는 sheet 를 생성합니다.
    public Sheet createSheet(String sheetName, int maxColumn, int maxRow) {
        Sheet sheet;

        sheet = new Sheet(sheetName, maxColumn, maxRow);
        sheets.addLast(sheet);
        Log.v(TAG, "New created Sheet : " + sheet.getName());

        return sheet;
    }

    // 인자의 값을 갖는 sheet 를 삭제합니다.
    public Sheet deleteSheet(int sheetId) {

        Sheet sheet;
        for (int i = 0; i < sheets.size(); i++) {
            sheet = sheets.get(i);
            if (sheet.getSheetId() == sheetId) {
                sheet.deleteAllCell(sheetId);
                sheet = sheets.remove(i);

                Log.v(TAG, "Delete Sheet : " + sheet.getName());
                return sheet;
            }
        }

        Log.v(TAG, "Fail to delete Sheet");
        return null;
    }

    // 인자의 값을 갖는 sheet 를 리턴합니다.
    public Sheet getSheet(int sheetId) {
        Sheet sheet = null;
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getSheetId() == sheetId) {
                sheet = sheets.get(i);
            }
        }
        return sheet;
    }

    // SheetId 의 순서를 newOrder 의 파라미터 값으로 순서를 바꿔준다.
    public Sheet changeSheetOrder(int sheetId, int newOrder) {
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getSheetId() == sheetId) {
                Sheet sheet = sheets.remove(i);
                sheets.add(newOrder - 1, sheet);
                return sheet;
            }
        }
        return null;
    }

    // 생성된 sheet 들의 Id 들을 리턴합니다.
    public int[] getAllSheetId() {
        int[] allSheetId = new int[sheets.size()];
        for (int i = 0; i < sheets.size(); i++) {
            allSheetId[i] = sheets.get(i).getSheetId();
        }
        return allSheetId;
    }

    // Sheet 의 이름을 변경시켜준다.
    public Sheet setName(String sheetNewName, int sheetId) throws DuplicateSheetNameException {

        // 변경시키고자하는 이름이 기존의 WorkBook 의 다른 sheet 의 이름과 동일하면
        // DuplicateSheetNameException 을 던진다.
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getName().equals(sheetNewName)) {
                throw new DuplicateSheetNameException();
            }
        }

        // 시트의 위치를 변경시킨다.
        for (int i = 0; i < sheets.size(); i++) {
            if (sheets.get(i).getSheetId() == sheetId) {
                Sheet sheet = sheets.remove(i);
                sheet.setName(sheetNewName);
                sheets.add(i, sheet);
                return sheet;
            }
        }
        return null;
    }

    public void clear() {
        sheets.clear();
    }
}
