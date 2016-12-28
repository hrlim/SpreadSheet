package com.example.owner.spreadsheet.exception;

// row, column 이 값이 sheet 를 구성하고 있는 최대 Row, Column 을 넘을 때 혹은 (1,1)보다 작은 값일 때 발생시키려는 Exception
public class CellIndexOutOfBoundsException extends IndexOutOfBoundsException {
    public CellIndexOutOfBoundsException() {
        super();
    }
}
