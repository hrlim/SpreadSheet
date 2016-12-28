package com.example.owner.spreadsheet.model;

public class Cell {

    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_FUNCTION = 3;

    private String value;
    private int type;

    // 생성자입니다.
   Cell(String value, int type) {
        this.value = value;
        this.type = type;
    }

    // cellValue 값을 리턴하는 메소드
    public String getValue() {
        return value;
    }

    // cellType 값을 리턴하는 메소드
    public int getType() {
        return type;
    }

}
