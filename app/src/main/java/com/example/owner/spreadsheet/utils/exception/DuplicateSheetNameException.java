package com.example.owner.spreadsheet.utils.exception;

// Sheet 의 이름을 변경할 때 기존의 sheet 이름과 동일할 경우 발생시키는 exception
public class DuplicateSheetNameException extends Exception {
    public DuplicateSheetNameException() {
        super();
    }
}
