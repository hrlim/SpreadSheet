package com.example.owner.spreadsheet.model;

import android.util.Log;
import java.util.HashMap;

class CellStorage {

    private static final String TAG = "Cell";

    // HashMap<CellInstance, HashMap< SheetID , Count>>
    private HashMap<Cell, HashMap<Integer, Integer>> cellMap = new HashMap<>();

    private static class CellStorageHolder {
        private static final CellStorage INSTANCE = new CellStorage();
    }

    // 해당 객체는 여러 개 생성되어선 안 되므로 생성자에 직접 접근할 수 없어야 합니다.
    // SingleTon 구현
    private CellStorage() {

    }

    // 해당 메서드를 통해 인스턴스를 가질 수 있습니다.
    public static CellStorage getInstance() {
        return CellStorageHolder.INSTANCE;
    }

    // 인자의 값을 갖는 셀을 생성합니다.
    // 인자의 값을 갖는 Cell 이 이미 생성되어 있다면 인스턴스를 새로 생성하지 않고 기존의 것을 사용합니다.
    Cell createCell(int sheetId, String cellValue, int cellType) {
        Cell cell = null;
        HashMap<Integer, Integer> usdCountHashMap;
        boolean isCellExist = false;

        // Cell 과 동일한 값이있는지 for 문 통해 확인합니다.
        for (Cell eachCell : cellMap.keySet()) {
            if (eachCell.getValue().equals(cellValue) && eachCell.getType() == cellType) {
                isCellExist = true;
                usdCountHashMap = cellMap.get(eachCell);
                boolean isUsedSheet = false;

                // Cell 과 동일한 값이 있으며 SheetId 있는지 for 문을 통해 확인합니다.
                for (Integer useCountSheetId : cellMap.get(eachCell).keySet()) {
                    if (useCountSheetId == sheetId) {
                        isUsedSheet = true;

                        // SheetId 에서 참조되는 count 를 늘려줍니다.
                        Integer useCount = cellMap.get(eachCell).get(useCountSheetId);
                        usdCountHashMap.put(useCountSheetId, ++useCount);
                        cellMap.put(eachCell, usdCountHashMap);
                        Log.v(TAG, "This cell's useCount per sheet is up.");
                        break;
                    }
                }
                if (!isUsedSheet) {
                    cellMap.get(eachCell).put(sheetId, 1);
                    Log.v(TAG, "This cell's useSheet is up.");
                }
                break;
            }
        }

        // Cell 과 동일한 값이 없을 때
        if (!isCellExist) {
            usdCountHashMap = new HashMap<>();
            usdCountHashMap.put(sheetId, 1);
            cell = new Cell(cellValue, cellType);
            cellMap.put(cell, usdCountHashMap);

            Log.v(TAG, "Created new cell. (cellValue : " + cellValue + ", " + "cellType : " + cellType + ")");
        }

        return cell;
    }

    // 셀을 삭제합니다.
    // 모든 Sheet 의 Count 값이 0 이면 해당 Cell 을 더 이상 갖고 있지 않습니다.
    Cell deleteCell(int sheetId, Cell cell) {
        if (cell != null) {
            HashMap<Integer, Integer> usdCountHashMap;
            for (Cell eachCell : cellMap.keySet()) {
                if (eachCell.getValue().equals(cell.getValue()) && eachCell.getType() == cell.getType()) {
                    usdCountHashMap = cellMap.get(eachCell);

                    for (Integer useCountSheetId : cellMap.get(eachCell).keySet()) {
                        if (useCountSheetId == sheetId) {
                            // SheetId 에서 참조되는 count 를 감소시킨다..
                            if (cellMap.get(eachCell).get(sheetId) > 0) {
                                Integer useCount = cellMap.get(eachCell).get(sheetId);
                                usdCountHashMap.put(sheetId, --useCount);
                                Log.v(TAG, "This cell's useCount is down.");

                                cellMap.put(eachCell, usdCountHashMap);

                                // 만일 참조되는 cellInstance 에 대해서 참조하는 count 가 0인 SheetId 를 제거한다.
                                if (cellMap.get(eachCell).get(sheetId) == 0) {
                                    cellMap.get(eachCell).remove(sheetId);
                                    Log.v(TAG, "Because this sheet is never used this cell, sheet completely deleted.");
                                    // cellInstance 의 크기가 0일 경우 cellInstance 를 삭제한다.
                                    if (usdCountHashMap.size() == 0) {
                                        cellMap.remove(eachCell);
                                        Log.v(TAG, "Because this cell is never used, cell completely deleted.");
                                    }
                                    return eachCell;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    // Sheet 에서 사용하는 cellInstance 를 삭제하는 메소드
    // sheet 를 삭제할 때 사용된다.
    void deleteAllCellInSheet(int sheetId) {
        HashMap<Integer, Integer> usdCountHashMap;
        for (Cell eachCell : cellMap.keySet()) {
            for (Integer useCountSheetId : cellMap.get(eachCell).keySet()) {
                if (useCountSheetId == sheetId) {
                    usdCountHashMap = cellMap.get(eachCell);
                    cellMap.get(eachCell).remove(sheetId);
                    Log.v(TAG, "Because this sheet is never used, sheet completely deleted.");
                    // cellInstance 의 크기가 0일 경우 cellInstance 를 삭제한다.
                    if (usdCountHashMap.size() == 0){
                        cellMap.remove(eachCell);
                        Log.v(TAG, "Because this cell is never used, cell completely deleted.");
                    }
                }
            }
        }
    }
}
