package com.example.owner.spreadsheet.model;

import android.util.Log;
import java.util.HashMap;

import com.example.owner.spreadsheet.utils.exception.CellIndexOutOfBoundsException;
import com.example.owner.spreadsheet.utils.listener.ModelDataChangeListener;
import static com.example.owner.spreadsheet.view.WorkBookView.SECTION_SHEET;

public class Sheet {

    private final static String TAG = "Sheet";

    static final int DEFAULT_NUM_COLUMNS = 26;
    static final int DEFAULT_NUM_ROWS = 100;

    private final static int CELL_BLOCK_ROW = 10;
    private final static int CELL_BLOCK_COLUMN = 10;

    private String name;
    private int id;

    // Sheet 에 있는 cell 의 가로,세로 최대수
    private int maxColumn;
    private int maxRow;

    // cell 의 가로.세로를 블락단위로 나눈 column, row.
    private int maxBlockColumn;
    private int maxBlockRow;

    private int lastBlockRow;
    private int lastBlockColumn;

    private static int uniqueID = 0;

    // <BlockNumber, Cell[][]>
    private HashMap<Integer, Cell[][]> sheetBlockMap = new HashMap<>();
    private CellStorage cellStorage = CellStorage.getInstance();

    // 생성자입니다. 인자의 값으로 멤버변수를 초기화해주세요.
    public Sheet(String sheetName, int maxColumn, int maxRow) {
        uniqueID = uniqueID + 1;
        this.id = uniqueID;
        this.name = sheetName;
        this.maxColumn = maxColumn;
        this.maxRow = maxRow;
    }

    // cellStorage 에 접근하여 cell 을 얻고,
    // 이를 sheetBlockMap 의 적절한 위치에서 참조하도록 합니다.
    public Cell createCell(int column, int row, String value, int type) throws CellIndexOutOfBoundsException {
        Cell[][] cells;
        boolean exist = false;

        Integer blockNum = getBlockNum(column, row);

        // 순환하며 입력받은 row, column 을 가지고 있는 BlockNum 이 있는지 검사합니다.
        for (Integer eachBlockNum : sheetBlockMap.keySet()) {
            if (eachBlockNum.equals(blockNum)) {
                exist = true;
                Log.v(TAG, "BlockNum("+ blockNum + ") is already exist. ");

                cells = sheetBlockMap.get(blockNum);

                // column,row 는 (1,1)부터 시작, [][]배열은 (0,0)부터 시작하므로 상황에 맞춰
                // 입력받은 column, row 를 조작한다.
                if (column % CELL_BLOCK_COLUMN != 0 && row % CELL_BLOCK_ROW != 0) {
                    cells[column % CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = new Cell(value, type);
                } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW == 0) {
                    cells[CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = new Cell(value, type);
                } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW != 0) {
                    cells[CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = new Cell(value, type);
                } else if (row % CELL_BLOCK_ROW == 0 && column % CELL_BLOCK_COLUMN != 0) {
                    cells[column % CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = new Cell(value, type);
                }

                sheetBlockMap.put(blockNum, cells);
                break;
            }
        }

        // 기존에 입력받은 row, column 을 가지고 있는 BlockNum 이 없다면 새로 생성합니다.
        if (!exist) {
            // BlockNum 에 따라 배열의 크기가 다르다.
            // 예를 들어, 26 * 100 인 sheet 를 10 * 10으로 나눈다고 가정하자.
            // - - - - - - -
            // | 10 | 10 | 6 |
            // | 10 | 10 | 6 |
            // |     ....    |
            if (blockNum % maxBlockColumn == 0) {
                cells = new Cell[lastBlockColumn][CELL_BLOCK_ROW];
            } else if (blockNum > maxBlockColumn * (maxBlockRow - 1)) {
                cells = new Cell[CELL_BLOCK_COLUMN][lastBlockRow];
            } else if (blockNum == maxBlockRow * maxBlockColumn) {
                cells = new Cell[lastBlockColumn][lastBlockRow];
            } else {
                cells = new Cell[CELL_BLOCK_COLUMN][CELL_BLOCK_ROW];
            }
            Log.v(TAG, "New blockNum : " + blockNum);

            sheetBlockMap.put(blockNum, cells);
            if (column % CELL_BLOCK_COLUMN != 0 && row % CELL_BLOCK_ROW != 0) {
                sheetBlockMap.get(blockNum)[column % CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = new Cell(value, type);
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW == 0) {
                sheetBlockMap.get(blockNum)[CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = new Cell(value, type);
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW != 0) {
                sheetBlockMap.get(blockNum)[CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = new Cell(value, type);
            } else if (row % CELL_BLOCK_ROW == 0 && column % CELL_BLOCK_COLUMN != 0) {
                sheetBlockMap.get(blockNum)[column % CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = new Cell(value, type);
            }
            Log.v(TAG, "SheetBlock size : [" + cells.length + "][" + cells[0].length + "]");
        }
        // cellStorage 에서 cell 을 만든다.
        Cell cell = cellStorage.createCell(this.getSheetId(), value, type);
        // 추가된 cell 로 인해 Model 이 변경되었음을 view 에게 알려주기 위해 listener 를 호출.
        ModelDataChangeListener.notifyDataChanged(SECTION_SHEET);

        return cell;
    }

    // sheetBlockMap 이 더 이상 해당 cell 을 참조하지 않도록 합니다.
    public Cell deleteCell(int column, int row) throws CellIndexOutOfBoundsException {

        Cell[][] cells;
        Cell cell = null;
        int count = 0;

        Integer blockNum = getBlockNum(column, row);

        if (sheetBlockMap.containsKey(blockNum)) {
            cells = sheetBlockMap.get(blockNum);

            // column,row 는 (1,1)부터 시작, [][]배열은 (0,0)부터 시작하므로 상황에 맞춰
            // 입력받은 column, row 를 조작하여 저장한다.
            if (column % CELL_BLOCK_COLUMN != 0 && row % CELL_BLOCK_ROW != 0) {
                cell = cells[column % CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1];
                cells[column % CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = null;
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW == 0) {
                cell = cells[CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1];
                cells[CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = null;
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW != 0) {
                cell = cells[CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1];
                cells[CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1] = null;
            } else if (row % CELL_BLOCK_ROW == 0 && column % CELL_BLOCK_COLUMN != 0) {
                cell = cells[column % CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1];
                cells[column % CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1] = null;
            }

            for (int i = 0; i < cells.length; i++) {
                for (int j = 0; j < cells[i].length; j++) {
                    if (cells[i][j] != null) {
                        count++;
                    }
                }
            }

            if (count == 0) {
                sheetBlockMap.remove(blockNum);
                Log.v(TAG, "Delete blockNum : " + blockNum);
            }
        }
        // cellStorage 에서 sheet 에서 사용하는 cell 을 삭제한다.
        cellStorage.deleteCell(this.getSheetId(), cell);
        // cell 이 변경되었기 때문에 cell 이 삭제된 view 를 보여주기위해 listener 를 호출한다.
        ModelDataChangeListener.notifyDataChanged(SECTION_SHEET);

        return cell;
    }


    public void deleteAllCell(int sheetId) {
        cellStorage.deleteAllCellInSheet(sheetId);
    }

    // sheetBlockMap 에 접근하여 특정 위치의 cell 을 리턴합니다.
    public Cell getCell(int column, int row) throws CellIndexOutOfBoundsException{

        Cell[][] cells;
        Cell cell = null;

        Integer result = getBlockNum(column, row);

        if (sheetBlockMap.containsKey(result)) {
            cells = sheetBlockMap.get(result);

            // column,row 는 (1,1)부터 시작, [][]배열은 (0,0)부터 시작하므로 상황에 맞춰
            // 입력받은 column, row 를 조작한다.
            if (column % CELL_BLOCK_COLUMN != 0 && row % CELL_BLOCK_ROW != 0) {
                cell = cells[column % CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1];
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW == 0) {
                cell = cells[CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1];
            } else if (column % CELL_BLOCK_COLUMN == 0 && row % CELL_BLOCK_ROW != 0) {
                cell = cells[CELL_BLOCK_COLUMN - 1][row % CELL_BLOCK_ROW - 1];
            } else if (row % CELL_BLOCK_ROW == 0 && column % CELL_BLOCK_COLUMN != 0) {
                cell = cells[column % CELL_BLOCK_COLUMN - 1][CELL_BLOCK_ROW - 1];
            }
        }

        return cell;
    }

    // column 과 row 의 값이 가르키고 있는 blcok 의 위치를 반환하는 메소드.
    private Integer getBlockNum(int column, int row)  {
        if((column <= 0 && row <= 0 )|| (column > getMaxColumn() && row > getMaxRow()))
        {
            throw new CellIndexOutOfBoundsException();
        }

        Integer blockNum;
        if (maxColumn % CELL_BLOCK_COLUMN == 0) {
            lastBlockColumn = CELL_BLOCK_COLUMN;
        } else {
            lastBlockColumn = maxColumn % CELL_BLOCK_COLUMN;
        }

        if (maxRow % CELL_BLOCK_ROW == 0) {
            lastBlockRow = CELL_BLOCK_ROW;
        } else {
            lastBlockRow = maxRow % CELL_BLOCK_ROW;
        }


        if (maxColumn % CELL_BLOCK_COLUMN == 0) {
            maxBlockColumn = maxColumn / CELL_BLOCK_COLUMN;
        } else {
            maxBlockColumn = maxColumn / CELL_BLOCK_COLUMN + 1;
        }

        if (maxRow % CELL_BLOCK_ROW == 0) {
            maxBlockRow = maxRow / CELL_BLOCK_ROW;
        } else {
            maxBlockRow = maxRow / CELL_BLOCK_ROW + 1;
        }

        if (row % CELL_BLOCK_ROW == 0) {
            if (column % CELL_BLOCK_COLUMN == 0) {
                blockNum = (row / CELL_BLOCK_ROW - 1) * maxBlockColumn + (column / CELL_BLOCK_COLUMN);
            } else {
                blockNum = (row / CELL_BLOCK_ROW - 1) * maxBlockColumn + (column / CELL_BLOCK_COLUMN + 1);
            }
        } else {
            if (column % CELL_BLOCK_COLUMN == 0) {
                blockNum = (row / CELL_BLOCK_ROW) * maxBlockColumn + (column / CELL_BLOCK_COLUMN);
            } else {
                blockNum = (row / CELL_BLOCK_ROW) * maxBlockColumn + (column / CELL_BLOCK_COLUMN + 1);
            }
        }
        return blockNum;
    }

    // name 값을 리턴합니다.
    public String getName() {
        return this.name;
    }

    // id 값을 리턴합니다.
    public int getSheetId() {
        return this.id;
    }

    // name 값을 변경합니다.
    public void setName(String sheetName) {
        this.name = sheetName;
    }

    // Sheet 에 있는 Column 의 최대수
    public int getMaxColumn() {
        return maxColumn;
    }

    // Sheet 에 있는 Row 의 최대수
    public int getMaxRow() {
        return maxRow;
    }

}
