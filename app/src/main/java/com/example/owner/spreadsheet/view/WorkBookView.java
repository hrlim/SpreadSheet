package com.example.owner.spreadsheet.view;

import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import android.util.AttributeSet;
import android.util.Log;

import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.owner.spreadsheet.R;
import com.example.owner.spreadsheet.ctrl.WorkBookController;
import com.example.owner.spreadsheet.utils.exception.CellIndexOutOfBoundsException;
import com.example.owner.spreadsheet.utils.exception.DuplicateSheetNameException;
import com.example.owner.spreadsheet.model.Cell;
import com.example.owner.spreadsheet.utils.DrawText;
import com.example.owner.spreadsheet.utils.function.SpreadSheetFunction;
import com.example.owner.spreadsheet.utils.listener.ModelDataChangeListener;

import static com.example.owner.spreadsheet.model.Cell.TYPE_NUMBER;
import static com.example.owner.spreadsheet.model.Cell.TYPE_FUNCTION;
import static com.example.owner.spreadsheet.model.Cell.TYPE_STRING;
import static com.example.owner.spreadsheet.utils.DrawText.ALIGNMENT_RIGHT;
import static com.example.owner.spreadsheet.utils.DrawText.ALIGNMENT_LEFT;
import static com.example.owner.spreadsheet.utils.function.SpreadSheetFunctionFactory.getExcelFunction;

public class WorkBookView extends View implements ModelDataChangeListener.IDataChangeable {

    private static final String TAG = "view";

    // invalidate 하고자 하는 영역을 나눔.
    public static final int SECTION_RESET = 0;
    public static final int SECTION_ALL = 1;
    public static final int SECTION_SHEET = 2;
    public static final int SECTION_SHEET_LIST = 3;

    //WorkBook Default
    private static final Typeface DEFAULT_FONT = Typeface.SANS_SERIF;

    //Cell Default
    private static final float DEFAULT_CELL_HEIGHT = 80.0f;
    private static final float DEFAULT_CELL_WIDTH = 120.0f;
    private static final float DEFAULT_CELL_FONT_SIZE = 50.0f;

    //Header Default
    private static final float DEFAULT_HEADER_ROW_WIDTH = 120.0f;
    private static final float DEFAULT_HEADER_COLUMN_HEIGHT = 80.0f;

    //sheet Default
    private static final int DEFAULT_NUMBER_OF_ROWS = 100;
    private static final int DEFAULT_NUMBER_OF_COLUMNS = 26;

    //SheetList Default
    private static final float DEFAULT_SHEET_LIST_WIDTH = 200.0f;
    private static final float DEFAULT_SHEET_LIST_HEIGHT = 80.0f;
    private static final float DEFAULT_SHEET_LIST_FONT_SIZE = DEFAULT_SHEET_LIST_HEIGHT * 1 / 2;
    private static final float DEFAULT_SHEET_LIST_LEFT_MARGIN = 50.0f;
    private static final float DEFAULT_SHEET_LIST_RIGHT_MARGIN = 50.0f;
    private static final float DEFAULT_ADD_NEW_SHEET_WIDTH = DEFAULT_SHEET_LIST_WIDTH / 3;
    private static final String ADD_NEW_SHEET = "+";

    // Scale
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;
    private float scaleFactor = 1.0f;

    //Gesture
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;

    //Paint
    private final Paint paint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint linePaint = new Paint();
    private final Paint selectedPaint = new Paint();
    private final Paint cellDataPaint = new Paint();

    //RectF
    private RectF sheetRectF = new RectF();
    private RectF cellsRectF = new RectF();
    private RectF sheetListRectF = new RectF();
    private RectF headerColumnRectF = new RectF();
    private RectF headerRowRectF = new RectF();
    private RectF acrossHeaderF = new RectF();
    private RectF addSheetListRectF = new RectF();

    // Selected Range
    private int startColumn;
    private int startRow;
    private int endColumn;
    private int endRow;

    //Translate
    private float distanceMoveX;
    private float distanceMoveY;

    // Current
    private float currentSheetX;
    private float currentSheetY;
    private float currentSheetListX;

    // View
    private int width;
    private int height;

    // Mutable CellSize,
    private float cellWidth;
    private float cellHeight;

    // Mutable HeaderSize
    private float headerRowWidth;
    private float headerColumnHeight;

    // Mutable Font
    private Typeface fontType;
    private float headerFontSize;
    private float cellFontSize;
    private float sheetListFontSize;
    private Paint.FontMetrics fontMetrics;

    // Mutable Sheet
    private int numRows;
    private int numColumns;

    // Mutable SheetList
    private float sheetListWidth;
    private float sheetListHeight;
    private float sheetListLeftMargin;
    private float sheetListRightMargin;
    private float addSheetListWidth;
    private int currentSheetNum;

    private boolean selectionFlag = false;
    private boolean changeStartPositionFlag = false;
    private boolean headerRowFlag = false;
    private boolean headerColumnFlag = false;
    private boolean editFlag = false;

    private String sheetList[];
    private WorkBookController workBookController;
    private DrawText drawCellData = new DrawText();

    private StringBuilder stringBuilder = null;
    private InputMethodManager inputMethodManager = (InputMethodManager) getContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE);

    private int section = SECTION_ALL;

    public WorkBookView(Context context) {
        this(context, null);
    }

    public WorkBookView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkBookView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    // Default 값으로 초기화합니다.
    private void initialize() {
        // 초기 선택영역
        startColumn = 1;
        startRow = 1;
        endColumn = startColumn;
        endRow = startRow;

        // Font 초기화
        fontType = DEFAULT_FONT;
        headerFontSize = DEFAULT_SHEET_LIST_FONT_SIZE;
        sheetListFontSize = DEFAULT_SHEET_LIST_FONT_SIZE;
        cellFontSize = DEFAULT_CELL_FONT_SIZE;

        // CellSize 초기화
        cellHeight = DEFAULT_CELL_HEIGHT;
        cellWidth = DEFAULT_CELL_WIDTH;

        // HeaderSize 초기화
        headerRowWidth = DEFAULT_HEADER_ROW_WIDTH;
        headerColumnHeight = DEFAULT_HEADER_COLUMN_HEIGHT;

        // Sheet 초기화
        numRows = DEFAULT_NUMBER_OF_ROWS;
        numColumns = DEFAULT_NUMBER_OF_COLUMNS;

        // SheetList 초기화
        sheetListWidth = DEFAULT_SHEET_LIST_WIDTH;
        sheetListHeight = DEFAULT_SHEET_LIST_HEIGHT;
        sheetListLeftMargin = DEFAULT_SHEET_LIST_LEFT_MARGIN;
        sheetListRightMargin = DEFAULT_SHEET_LIST_RIGHT_MARGIN;
        addSheetListWidth = DEFAULT_ADD_NEW_SHEET_WIDTH;

        // 현재 위치 초기화
        currentSheetNum = 0;
        currentSheetX = 0;
        currentSheetY = 0;
        currentSheetListX = 0;

        editFlag = false;

        newSheetList();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClickable(true);
        requestFocus();

        // 키 리스너를 등록합니다.
        setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DEL:
                            Log.d("text", "KeyEvent.KEYCODE_DEL");
                            if (stringBuilder.length() > 0) {
                                stringBuilder.setLength(stringBuilder.length() - 1);
                            }
                            break;

                        case KeyEvent.KEYCODE_ENTER:
                            Log.d("text", "KeyEvent.KEYCODE_ENTER");
                            stringBuilder.setLength(stringBuilder.length());

                            if (stringBuilder.toString().length() != 0) {
                                workBookController.setCell(currentSheetNum, startColumn, startRow, stringBuilder.toString());
                            } else {
                                workBookController.deleteCell(currentSheetNum, startColumn, startRow);
                            }

                            hideSoftKeyboard();
                            editFlag = false;
                            stringBuilder.setLength(0);

                            break;
                        case KeyEvent.KEYCODE_BACK:
                            break;

                        default:
                            stringBuilder.append((char) event.getUnicodeChar());  //키보드에서 입력받은 값
                            break;
                    }

                    section = SECTION_SHEET;
                    invalidateSection(section);

                }
                return false;
            }
        });

        // Zoom In - out 을 위한 리스너 등록합니다.
        scaleGestureDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    public boolean onScale(ScaleGestureDetector detector) {

                        scaleFactor = detector.getScaleFactor();

                        float tempCellWidth = cellWidth * scaleFactor;
                        float tempCellHeight = cellHeight * scaleFactor;

                        // 지정된 최소/ 최대 확대비율에 따라 확대할 수 있습니다. 최대 : 3배, 최소 : 0.5배
                        if (tempCellHeight > DEFAULT_CELL_HEIGHT * MIN_SCALE && tempCellHeight < DEFAULT_CELL_HEIGHT * MAX_SCALE
                                && tempCellWidth > DEFAULT_CELL_WIDTH * MIN_SCALE && tempCellWidth < DEFAULT_CELL_WIDTH * MAX_SCALE) {

                            cellWidth *= scaleFactor;
                            cellHeight *= scaleFactor;
                            cellFontSize *= scaleFactor;
                            headerFontSize *= scaleFactor;
                            headerRowWidth *= scaleFactor;
                            headerColumnHeight *= scaleFactor;
                            currentSheetX *= scaleFactor;
                            currentSheetY *= scaleFactor;

                        }
                        headerColumnRectF.set(headerRowWidth, 0, width, headerColumnHeight);
                        headerRowRectF.set(0, headerColumnHeight, headerRowWidth, height - sheetListHeight);
                        cellsRectF.set(headerRowWidth, headerColumnHeight, width, height - sheetListHeight);

                        section = SECTION_ALL;
                        invalidateSection(section);
                        return true;
                    }
                });

        // Scroll, singleTapUp 등의 제스쳐를 사용하기 위한 리스너 등록합니다.
        gestureDetector = new GestureDetector(getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onDown(MotionEvent event) {

                        int column;
                        int row;

                        column = getColumn(event.getX());
                        row = getRow(event.getY());


                        // start 지점과 end 지점의 각각의 위치에 이벤트가가 들어올 경우 선택영역 확대 승인합니다.
                        if ((startColumn == column && startRow == row) || (column == endColumn && row == endRow)) {
                            if (cellsRectF.contains(event.getX(), event.getY())) {
                                if (startColumn == column && startRow == row) {
                                    changeStartPositionFlag = true;
                                }
                                if (column == endColumn && row == endRow) {
                                    changeStartPositionFlag = false;
                                }
                            }
                            selectionFlag = true;

                        } else {
                            selectionFlag = false;
                        }
                        // Column 헤더, 선택 영역 확보 승인합니다.
                        if (headerColumnRectF.contains(event.getX(), event.getY())) {
                            headerColumnFlag = true;
                            selectionFlag = true;
                        } else {
                            headerColumnFlag = false;
                        }
                        // Row 헤더, 선택 영역 확보 승인합니다.
                        if (headerRowRectF.contains(event.getX(), event.getY())) {
                            headerRowFlag = true;
                            selectionFlag = true;
                        } else {
                            headerRowFlag = false;
                        }

                        if (editFlag) {
                            selectionFlag = false;
                        }

                        return true;
                    }

                    public void onLongPress(MotionEvent event) {
                        if (sheetListRectF.contains(event.getX(), event.getY())) {
                            int tempSheetNum = (int) ((event.getX() - currentSheetListX - sheetListLeftMargin) / sheetListWidth);
                            if (tempSheetNum != currentSheetNum) {
                                if (tempSheetNum < sheetList.length - 1) {
                                    currentSheetNum = tempSheetNum;
                                }
                            }
                            DialogSelectOption(currentSheetNum);
                        }
                    }

                    public boolean onDoubleTap(MotionEvent event) {
                        editFlag = true;

                        if (stringBuilder != null) {
                            stringBuilder.setLength(0);
                        }
                        // 선택된 셀의 데이터를 편집합니다.
                        startColumn = getColumn(event.getX());
                        startRow = getRow(event.getY());

                        endColumn = startColumn;
                        endRow = startRow;

                        Cell cell = null;
                        try {
                            cell = workBookController.getCell(currentSheetNum, startColumn, startRow);
                        } catch (CellIndexOutOfBoundsException e) {
                            Log.v(TAG, "Cell's position is wrong.");
                            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                        }
                        if (cell != null) {
                            stringBuilder = new StringBuilder(cell.getValue());
                        } else {
                            stringBuilder = new StringBuilder();
                        }

                        showSoftKeyboard();

                        section = SECTION_SHEET;
                        invalidateSection(section);

                        return true;
                    }

                    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {

                        if (selectionFlag) {

                            if (headerColumnFlag && headerColumnRectF.contains(event2.getX(), event2.getY())) {
                                endColumn = getColumn(event2.getX());
                                endRow = numRows;
                                section = SECTION_SHEET;
                                invalidate((int) sheetRectF.left, (int) sheetRectF.top, (int) sheetRectF.right, (int) sheetRectF.bottom);
                            } else if (headerRowFlag && headerRowRectF.contains(event2.getX(), event2.getY())) {
                                endColumn = numColumns;
                                endRow = getRow(event2.getY());
                                section = SECTION_SHEET;
                                invalidate((int) sheetRectF.left, (int) sheetRectF.top, (int) sheetRectF.right, (int) sheetRectF.bottom);
                            } else if (cellsRectF.contains(event2.getX(), event2.getY())) {
                                int tempColumn;
                                int tempRow;

                                tempColumn = getColumn(event2.getX());
                                tempRow = getRow(event2.getY());

                                if (changeStartPositionFlag) {
                                    startColumn = tempColumn;
                                    startRow = tempRow;
                                } else {
                                    endColumn = tempColumn;
                                    endRow = tempRow;
                                }
                                section = SECTION_SHEET;
                                invalidateSection(section);
                            }
                            // else 문을 통해 화면에서의 이동합니다.
                        } else {
                            distanceMoveX = distanceX * -1;
                            distanceMoveY = distanceY * -1;
                            // 시트 영역에 대해 처리합니다.
                            if (sheetRectF.contains((int) event2.getX(), (int) event2.getY())) {
                                section = SECTION_SHEET;
                                invalidateSection(section);

                                // 시트리스트 영역에 대해 처리합니다.
                            } else if (sheetListRectF.contains((int) event2.getX(), (int) event2.getY())) {
                                section = SECTION_SHEET_LIST;
                                invalidateSection(section);
                            }
                        }
                        return true;
                    }

                    public boolean onSingleTapUp(MotionEvent event) {
                        editFlag = false;
                        // Cell 를 선택하여 선택 영역을 갖게합니다.
                        if (sheetRectF.contains(event.getX(), event.getY())) {
                            startColumn = getColumn(event.getX());
                            startRow = getRow(event.getY());

                            endColumn = startColumn;
                            endRow = startRow;
                            section = SECTION_SHEET;
                            invalidateSection(section);
                        }
                        // Column 헤더를 선택하여 선택 영역 가집니다.
                        if (headerColumnRectF.contains(event.getX(), event.getY())) {
                            startColumn = getColumn(event.getX());
                            startRow = 1;

                            endColumn = startColumn;
                            endRow = numRows;
                            section = SECTION_SHEET;
                            invalidateSection(section);
                        }
                        // Row 헤더를 선택하여 선택 영역 가집니다.
                        if (headerRowRectF.contains(event.getX(), event.getY())) {
                            startColumn = 1;
                            startRow = getRow(event.getY());

                            endColumn = numColumns;
                            endRow = startRow;
                            section = SECTION_SHEET;
                            invalidateSection(section);
                        }
                        // 전체영역을 선택영역으로 가집니다.
                        if (acrossHeaderF.contains(event.getX(), event.getY())) {
                            startColumn = 1;
                            startRow = 1;

                            endColumn = numColumns;
                            endRow = numRows;
                            section = SECTION_SHEET;
                            invalidateSection(section);
                        }
                        // Sheet 의 변경을 할 때 사용합니다.
                        if (sheetListRectF.contains(event.getX(), event.getY())) {
                            currentSheetNum = (int) ((event.getX() - currentSheetListX - sheetListLeftMargin) / sheetListWidth);
                            section = SECTION_ALL;
                            invalidateSection(section);
                        }
                        if (addSheetListRectF.contains(event.getX(), event.getY())) {
                            workBookController.addSheet();
                            currentSheetNum = sheetList.length - 1;

                            currentSheetListX = -(sheetListWidth * sheetList.length + sheetListLeftMargin + sheetListRightMargin + addSheetListWidth - width);
                            section = SECTION_ALL;
                            invalidateSection(section);
                        }
                        return true;
                    }
                });
    }

    // 디바이스상의 상대좌표를 인자로 받아 절대좌표의 Column 을 구하는 메서드.
    private int getColumn(float eventX) {
        if (headerRowWidth > cellWidth) {
            return (int) ((eventX - currentSheetX - headerRowWidth + cellWidth) / cellWidth);
        } else if (headerRowWidth == cellWidth) {
            return (int) ((eventX - currentSheetX) / cellWidth);
        } else {
            return (int) ((eventX - currentSheetX + cellWidth - headerRowWidth) / cellWidth);
        }
    }

    // 디바이스상의 상대좌표를 인자로 받아 절대좌표의 Row 를 구하는 메서드.
    private int getRow(float eventY) {
        if (headerColumnHeight > cellHeight) {
            return (int) ((eventY - currentSheetY - headerColumnHeight + cellHeight) / cellHeight);
        } else if (headerColumnHeight == cellHeight) {
            return (int) ((eventY - currentSheetY) / cellHeight);
        } else {
            return (int) ((eventY - currentSheetY + cellHeight - headerColumnHeight) / cellHeight);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 실제 구현상의 height 를 구합니다.
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.UNSPECIFIED: // mode 가 셋팅되지 않은 크기가 넘어올 때
                height = heightMeasureSpec;
                break;
            case MeasureSpec.AT_MOST: // wrap_content (뷰 내부의 크기에 따라 크기가 달라짐) // default
                break;
            case MeasureSpec.EXACTLY: // fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
        }

        // 실제 구현상의 width 를 구합니다.
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            // mode 가 셋팅되지 않은 크기가 넘어올 때
            case MeasureSpec.UNSPECIFIED:
                width = widthMeasureSpec;
                break;
            // wrap_content (뷰 내부의 크기에 따라 크기가 달라짐) - default
            case MeasureSpec.AT_MOST:
                break;
            // fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
            case MeasureSpec.EXACTLY:
                width = MeasureSpec.getSize(widthMeasureSpec);
                break;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // Attribute 로 값을 받아올 수 있는 것들을 넣을 수 있다.

        // Rect 초기화
        sheetRectF.set(0, 0, width, height - sheetListHeight);
        sheetListRectF.set(0, height - sheetListHeight, width - addSheetListWidth, height);
        headerColumnRectF.set(headerRowWidth, 0, width, headerColumnHeight);
        headerRowRectF.set(0, headerColumnHeight, headerRowWidth, height - sheetListHeight);
        cellsRectF.set(headerRowWidth, headerColumnHeight, width, height - sheetListHeight);
        acrossHeaderF.set(0, 0, headerRowWidth, headerColumnHeight);
        addSheetListRectF.set(width - addSheetListWidth, height - sheetListHeight, width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (section == SECTION_ALL || section == SECTION_SHEET) {
            sheetMove();
            drawSheet(canvas);
            drawHeader(canvas);
            drawSelectedCell(canvas);
            drawCellData(canvas);
            if (editFlag) {
                drawEditText(canvas);
            }
        }
        if (section == SECTION_ALL || section == SECTION_SHEET_LIST) {
            sheetListMove();
            drawSheetList(canvas);
            drawAddSheet(canvas);
        }
        section = SECTION_ALL;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1)
            scaleGestureDetector.onTouchEvent(event);
        else {
            gestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private void drawHeader(Canvas canvas) {

        // 글자 길이
        float textWidth;
        float textHeight;

        //폰트를 그리려는 위치
        float fontX;
        float fontY;

        canvas.save();

        paint.reset();
        paint.setColor(Color.LTGRAY);

        linePaint.setColor(Color.MAGENTA);

        selectedPaint.setColor(Color.WHITE);

        textPaint.setTextSize(headerFontSize);
        textPaint.setTypeface(fontType);
        textPaint.setColor(Color.BLACK);

        fontMetrics = textPaint.getFontMetrics();
        textHeight = (int) ((fontMetrics.top * -1) - fontMetrics.bottom);
        fontY = (headerColumnHeight + textHeight) / 2;

        canvas.clipRect(headerColumnRectF);
        canvas.translate(currentSheetX + headerRowWidth, 0);

        // vertical
        for (int i = 1; i <= numColumns; i++) {
            textWidth = textPaint.measureText(Character.toString((char) (i + 64)));
            fontX = (cellWidth - textWidth) / 2;

            if ((startColumn <= i && i <= endColumn) || (endColumn <= i && i <= startColumn)) {
                linePaint.setStrokeWidth(10);
                linePaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(cellWidth * (i - 1), 0, cellWidth * i, headerColumnHeight, selectedPaint);
                canvas.drawLine(cellWidth * (i - 1), headerColumnHeight, cellWidth * i, headerColumnHeight, linePaint);
            } else {
                canvas.drawRect(cellWidth * (i - 1), 0, cellWidth * i, headerColumnHeight, paint);
            }
            canvas.drawText(Character.toString((char) (i + 64)), (i - 1) * cellWidth + fontX, fontY, textPaint);
        }

        linePaint.reset();
        linePaint.setColor(Color.BLACK);

        for (int i = 1; i <= numColumns; i++) {
            canvas.drawLine(cellWidth * i, 0, cellWidth * i, headerColumnHeight, linePaint);
        }

        canvas.restore();

        canvas.save();

        canvas.clipRect(headerRowRectF);
        canvas.translate(0, currentSheetY + headerColumnHeight);

        paint.reset();
        paint.setColor(Color.LTGRAY);

        linePaint.setColor(Color.MAGENTA);

        selectedPaint.setColor(Color.WHITE);

        textPaint.reset();
        textPaint.setTextSize(headerFontSize);
        textPaint.setTypeface(fontType);
        textPaint.setColor(Color.BLACK);

        fontMetrics = textPaint.getFontMetrics();
        textHeight = (fontMetrics.top * -1) - fontMetrics.bottom;
        fontY = (cellHeight + textHeight) / 2;

        // Horizontal lines
        for (int i = 1; i <= numRows; i++) {
            textWidth = textPaint.measureText(Integer.toString(i));
            fontX = (headerRowWidth - textWidth) / 2;

            if ((startRow <= i && i <= endRow) || (endRow <= i && i <= startRow)) {
                linePaint.setStrokeWidth(10);
                linePaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(0, cellHeight * (i - 1), headerRowWidth, cellHeight * i, selectedPaint);
                canvas.drawLine(headerRowWidth, cellHeight * (i - 1), headerRowWidth, cellHeight * i, linePaint);
            } else {
                canvas.drawRect(0, cellHeight * (i - 1), headerRowWidth, cellHeight * i, paint);
            }
            canvas.drawText(Integer.toString(i), fontX, (i - 1) * cellHeight + fontY, textPaint);
        }
        linePaint.reset();
        linePaint.setColor(Color.BLACK);
        for (int i = 1; i < numRows; i++) {
            canvas.drawLine(0, cellHeight * i, headerRowWidth, cellHeight * i, linePaint);
        }
        canvas.restore();

        canvas.save();

        canvas.clipRect(sheetRectF);
        canvas.translate(0, 0);

        linePaint.setColor(Color.BLACK);
        canvas.drawLine(headerRowWidth, 0, headerRowWidth, height - sheetListHeight, linePaint);
        canvas.drawLine(0, headerColumnHeight, width, headerColumnHeight, linePaint);

        canvas.restore();
    }

    private void drawSheet(Canvas canvas) {

        canvas.save();

        canvas.clipRect(cellsRectF);
        canvas.translate(currentSheetX + headerRowWidth, currentSheetY + headerColumnHeight);
        canvas.drawColor(Color.WHITE);

        linePaint.setColor(Color.BLACK);
        // Vertical lines
        for (int i = 1; i <= numColumns; i++) {
            canvas.drawLine(cellWidth * i, 0, cellWidth * i, cellHeight * (numRows), linePaint);
        }

        // Horizontal lines
        for (int i = 1; i <= numRows; i++) {
            canvas.drawLine(0, cellHeight * i, cellWidth * (numColumns), cellHeight * i, linePaint);
        }
        canvas.restore();
    }

    private void drawSelectedCell(Canvas canvas) {

        canvas.save();

        int selectedStartRow, selectedStartColumn;
        int selectedEndRow, selectedEndColumn;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(200, 255, 178, 217));
        // 수정필요
        canvas.clipRect(cellsRectF);
        canvas.translate(currentSheetX + headerRowWidth, currentSheetY + headerColumnHeight);

        if (endRow < startRow) {
            selectedEndRow = startRow;
            selectedStartRow = endRow;
        } else {
            selectedEndRow = endRow;
            selectedStartRow = startRow;
        }

        if (endColumn < startColumn) {
            selectedEndColumn = startColumn;
            selectedStartColumn = endColumn;
        } else {
            selectedEndColumn = endColumn;
            selectedStartColumn = startColumn;
        }

        if (selectedStartRow < 1) {
            selectedStartRow = 1;
        }
        if (selectedStartColumn < 1) {
            selectedStartColumn = 1;
        }

        if (selectedEndRow < 1 || selectedEndRow >= numRows) {
            selectedEndRow = numRows;
        }

        if (selectedEndColumn < 1 || selectedEndColumn >= numColumns) {
            selectedEndColumn = numColumns;
        }

        canvas.drawRect((selectedStartColumn - 1) * cellWidth, (selectedStartRow - 1) * cellHeight, selectedEndColumn * cellWidth, selectedEndRow * cellHeight, paint);

        if ((startColumn != endColumn) || (startRow != endRow)) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);

            canvas.drawRect((selectedStartColumn - 1) * cellWidth, (selectedStartRow - 1) * cellHeight, selectedStartColumn * cellWidth, selectedStartRow * cellHeight, paint);
        }

        paint.setColor(Color.MAGENTA);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect((selectedStartColumn - 1) * cellWidth, (selectedStartRow - 1) * cellHeight, selectedEndColumn * cellWidth, selectedEndRow * cellHeight, paint);

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((selectedStartColumn - 1) * cellWidth, (selectedStartRow - 1) * cellHeight, 10, paint);
        canvas.drawCircle(selectedEndColumn * cellWidth, selectedEndRow * cellHeight, 10, paint);
        paint.reset();

        canvas.restore();
    }

    private void drawSheetList(Canvas canvas) {

        float textWidth;
        float textHeight;

        float fontX;
        float fontY;

        canvas.save();

        canvas.clipRect(sheetListRectF);
        canvas.translate(currentSheetListX, 0);

        paint.reset();
        paint.setColor(Color.LTGRAY);

        linePaint.setColor(Color.BLACK);

        selectedPaint.setTextSize(sheetListFontSize);
        selectedPaint.setColor(Color.WHITE);

        textPaint.setTextSize(sheetListFontSize);
        textPaint.setColor(Color.BLACK);

        fontMetrics = textPaint.getFontMetrics();

        textHeight = (fontMetrics.top * -1) - fontMetrics.bottom;
        fontY = (sheetListHeight + textHeight) / 2;

        for (int i = 0; i < sheetList.length; i++) {
            textWidth = textPaint.measureText(sheetList[i]);
            fontX = (sheetListWidth - textWidth) / 2;
            if (i == currentSheetNum) {
                canvas.drawRect(sheetListLeftMargin + sheetListWidth * (i), height - sheetListHeight, sheetListWidth * (i + 1) + sheetListLeftMargin, height, selectedPaint);
                canvas.drawText(sheetList[i], sheetListLeftMargin + (i) * sheetListWidth + fontX, fontY + height - sheetListHeight, textPaint);
            } else {
                canvas.drawRect(sheetListLeftMargin + sheetListWidth * (i), height - sheetListHeight, sheetListWidth * (i + 1) + sheetListLeftMargin, height, paint);
                canvas.drawText(sheetList[i], sheetListLeftMargin + (i) * sheetListWidth + fontX, fontY + height - sheetListHeight, textPaint);
            }
        }

        for (int i = 0; i <= sheetList.length; i++) {
            canvas.drawLine(sheetListLeftMargin + sheetListWidth * i, height - sheetListHeight, sheetListLeftMargin + sheetListWidth * i, height, linePaint);
        }
        canvas.drawLine(0, height - sheetListHeight, sheetListLeftMargin + sheetList.length * sheetListWidth + sheetListRightMargin, height - sheetListHeight, linePaint);

        canvas.restore();
    }

    private void drawAddSheet(Canvas canvas) {

        float textWidth;
        float textHeight;

        float fontX;
        float fontY;

        canvas.save();

        canvas.clipRect(addSheetListRectF);
        canvas.translate(0, 0);

        paint.setColor(Color.LTGRAY);
        textPaint.setTextSize(sheetListFontSize);

        canvas.drawRect(width - addSheetListWidth, height - sheetListHeight, width, height, paint);

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();

        textWidth = (int) textPaint.measureText(ADD_NEW_SHEET);
        textHeight = (int) ((fontMetrics.top * -1) - fontMetrics.bottom);

        fontX = (addSheetListWidth - textWidth) / 2;
        fontY = (sheetListHeight + textHeight) / 2;

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(width - addSheetListWidth, height - sheetListHeight, width, height, paint);

        textPaint.setColor(Color.BLACK);
        canvas.drawText(ADD_NEW_SHEET, width - addSheetListWidth + fontX, fontY + height - sheetListHeight, textPaint);

        canvas.restore();
    }

    private void drawEditText(Canvas canvas) {
        canvas.save();

        canvas.clipRect(cellsRectF);
        canvas.translate(currentSheetX + headerRowWidth, currentSheetY + headerColumnHeight);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.LTGRAY);

        cellDataPaint.setColor(Color.BLACK);
        cellDataPaint.setTextSize(cellFontSize);

        if (stringBuilder != null) {
            float textWidth = cellDataPaint.measureText(stringBuilder.toString());

            int spanWidth = (int) (textWidth / cellWidth);

            canvas.drawRect((startColumn - 1) * cellWidth, (startRow - 1) * cellHeight,
                    (startColumn + spanWidth) * cellWidth, startRow * cellHeight,
                    paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(Color.MAGENTA);

            canvas.drawRect((startColumn - 1) * cellWidth, (startRow - 1) * cellHeight,
                    (startColumn + spanWidth) * cellWidth, startRow * cellHeight,
                    paint);

            drawCellData.drawText(stringBuilder.toString(), TYPE_STRING, (startColumn - 1) * cellWidth, (startRow - 1) * cellHeight, cellWidth, cellHeight, cellDataPaint, canvas, false);

        }

        paint.reset();
        textPaint.reset();

        canvas.restore();

    }

    private void sheetMove() {

        currentSheetX += distanceMoveX;
        currentSheetY += distanceMoveY;

        if (currentSheetY > 0) {
            currentSheetY = 0;
        }
        if (currentSheetX > 0) {
            currentSheetX = 0;

        }
        if (Math.abs(currentSheetX) + width > cellWidth * numColumns + headerRowWidth) {
            currentSheetX = width - cellWidth * numColumns - (+headerRowWidth);
        }


        if (Math.abs(currentSheetY) + height > cellHeight * numRows + sheetListHeight + headerColumnHeight) {
            currentSheetY = height - sheetListHeight - cellHeight * numRows - (headerColumnHeight);
        }
        distanceMoveX = 0;
        distanceMoveY = 0;
    }

    private void sheetListMove() {

        currentSheetListX += distanceMoveX;
        if (currentSheetListX > 0) {
            currentSheetListX = 0;
        }
        if (Math.abs(currentSheetListX) + width > sheetListWidth * sheetList.length + sheetListLeftMargin + sheetListRightMargin + addSheetListWidth) {
            currentSheetListX = -(sheetListWidth * sheetList.length + sheetListLeftMargin + sheetListRightMargin + addSheetListWidth - width);
        }
        distanceMoveX = 0;

    }

    private void drawCellData(Canvas canvas) {
        canvas.save();

        // 수정 필요
        canvas.clipRect(cellsRectF);
        canvas.translate(currentSheetX + headerRowWidth, currentSheetY + headerColumnHeight);

        if (workBookController != null) {
            cellDataPaint.setTextSize(cellFontSize);
            cellDataPaint.setTypeface(fontType);
            cellDataPaint.setColor(Color.BLACK);

            for (int row = 1; row < workBookController.getMaxRow(currentSheetNum); row++) {
                for (int column = 1; column < workBookController.getMaxColumn(currentSheetNum); column++) {
                    Cell cell = workBookController.getCell(currentSheetNum, column, row);
                    if (cell != null) {
                        int alignment;
                        String cellValue = cell.getValue();
                        int cellType = cell.getType();
                        switch (cellType) {
                            case TYPE_NUMBER:
                            case TYPE_FUNCTION:
                                alignment = ALIGNMENT_RIGHT;
                                SpreadSheetFunction spreadSheetFunction = getExcelFunction(cellValue);
                                if (spreadSheetFunction != null) {
                                    cellValue = spreadSheetFunction.getResult();
                                }

                                break;
                            case TYPE_STRING:
                            default:
                                alignment = ALIGNMENT_LEFT;
                                break;
                        }

                        drawCellData.drawText(cellValue, alignment, column * cellWidth - headerRowWidth, row * cellHeight - headerColumnHeight, cellWidth, cellHeight, cellDataPaint, canvas, true);
                    }
                }
            }
        }

        canvas.restore();
    }

    public void setWorkBookController(WorkBookController workBookController) {
        this.workBookController = workBookController;
        invalidateSection(SECTION_RESET);
    }

    @Override
    public void dataChanged(int section) {
        invalidateSection(section);
    }

    private void invalidateSection(int sectionType) {
        switch (sectionType) {
            case SECTION_ALL:
                section = SECTION_ALL;

                newSheetList();

                currentSheetX = 0;
                currentSheetY = 0;

                startColumn = 1;
                startRow = 1;
                endColumn = startColumn;
                endRow = startRow;

                editFlag = false;
                invalidate();

                break;
            case SECTION_RESET:
                section = SECTION_ALL;

                initialize();
                invalidate();

                break;
            case SECTION_SHEET:
                invalidate((int) sheetRectF.left, (int) sheetRectF.top, (int) sheetRectF.right, (int) sheetRectF.bottom);
                break;
            case SECTION_SHEET_LIST:
                newSheetList();
                invalidate((int) sheetListRectF.left, (int) sheetListRectF.top, (int) sheetListRectF.right, (int) sheetListRectF.bottom);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    private void newSheetList() {
        if (workBookController != null) {
            sheetList = new String[workBookController.getSheetCount()];
            for (int i = 0; i < sheetList.length; i++) {
                sheetList[i] = workBookController.getSheetName(i);
            }
        }
    }

    private void DialogSelectOption(final int selectedSheetNum) {
        final String items[] = {getResources().getString(R.string.change_sheet_name), getResources().getString(R.string.change_sheet_order), getResources().getString(R.string.delete_sheet)};
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(getResources().getString(R.string.title_sheet));
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (whichButton) {
                            case 0:

                                alertDialogBuilder.setTitle(getResources().getString(R.string.title_change_sheet_name));
                                alertDialogBuilder.setMessage(getResources().getString(R.string.sub_message_change_sheet_neme));

                                final EditText sheetName = new EditText(getContext());
                                sheetName.setText(sheetList[selectedSheetNum]);
                                alertDialogBuilder.setView(sheetName);

                                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String inputSheetName = sheetName.getText().toString();
                                        if (inputSheetName.length() > 0) {
                                            try {
                                                workBookController.setSheetName(inputSheetName, workBookController.getSheet(selectedSheetNum).getSheetId());
                                                Toast.makeText(getContext(), getResources().getString(R.string.message_change_sheet_name_success), Toast.LENGTH_LONG).show();
                                            } catch (DuplicateSheetNameException e) {
                                                Toast.makeText(getContext(), getResources().getString(R.string.message_change_sheet_name_fail), Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), getResources().getString(R.string.message_change_sheet_name_fail_too_short), Toast.LENGTH_LONG).show();
                                        }
                                        dialog.dismiss();     // 닫기
                                    }
                                });


                                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();     // 닫기
                                    }
                                });
                                alertDialogBuilder.show();
                                break;

                            case 1:
                                final RelativeLayout linearLayout = new RelativeLayout(getContext());
                                final NumberPicker numberPicker = new NumberPicker(getContext());
                                numberPicker.setMaxValue(sheetList.length);
                                numberPicker.setMinValue(1);

                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
                                RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                                linearLayout.setLayoutParams(params);
                                linearLayout.addView(numberPicker, numPicerParams);

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                                alertDialogBuilder.setTitle(getResources().getString(R.string.title_change_sheet_order));
                                alertDialogBuilder.setView(linearLayout);
                                alertDialogBuilder
                                        .setCancelable(false)
                                        .setPositiveButton(getResources().getString(R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int id) {
                                                        workBookController.changeSheetOrder(workBookController.getSheet(selectedSheetNum).getSheetId(), numberPicker.getValue());
                                                        currentSheetNum = numberPicker.getValue() - 1;
                                                        if (currentSheetNum == sheetList.length - 1) {
                                                            currentSheetListX = -(sheetListWidth * sheetList.length + sheetListLeftMargin + sheetListRightMargin + addSheetListWidth - width);
                                                        } else {
                                                            currentSheetListX = -(sheetListWidth * currentSheetNum + sheetListLeftMargin + sheetListRightMargin + addSheetListWidth - width);
                                                        }
                                                        dialog.dismiss();     // 닫기
                                                    }
                                                })
                                        .setNegativeButton(getResources().getString(R.string.cancel),
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int id) {
                                                        dialog.cancel();
                                                    }
                                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                break;

                            case 2:
                                if (sheetList.length > 1) {
                                    if (currentSheetNum == sheetList.length - 1) {
                                        currentSheetNum -= 1;
                                    } else {
                                        currentSheetNum = selectedSheetNum;
                                    }
                                    workBookController.deleteSheet(workBookController.getSheet(selectedSheetNum).getSheetId());
                                    Toast.makeText(getContext(), getResources().getText(R.string.message_sheet_delete_fail), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), getResources().getText(R.string.message_sheet_delete_action_negative), Toast.LENGTH_LONG).show();
                                }
                                break;

                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Cancel 버튼 클릭시 입력된 데이터들을 가지지 않는다.
                    }
                });
        alertDialogBuilder.show();
    }

    private void showSoftKeyboard() {
        inputMethodManager.showSoftInput(this, 0);
    }

    private void hideSoftKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }
}