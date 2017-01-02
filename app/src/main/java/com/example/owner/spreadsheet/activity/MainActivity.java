package com.example.owner.spreadsheet.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.owner.spreadsheet.R;
import com.example.owner.spreadsheet.ctrl.WorkBookController;
import com.example.owner.spreadsheet.utils.Hana;
import com.example.owner.spreadsheet.utils.listener.ModelDataChangeListener;
import com.example.owner.spreadsheet.view.WorkBookView;

public class MainActivity extends Activity {
    private static final int LOAD_CONTENT_REQUEST_CODE = 1;

    WorkBookView workBookView;
    WorkBookController workBookController = new WorkBookController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        workBookView = (WorkBookView) findViewById(R.id.workBookView);
        // view 가 Controller 를 통해 데이터 접근하기 위해
        workBookView.setWorkBookController(workBookController);
        // model 이 변경되었을 때 통지받지 위해 listener 에 추가
        ModelDataChangeListener.addDataChangeable(workBookView);

        workBookView.setBackgroundColor(Color.GRAY);

        // 새 파일 버튼의 이벤트를 통해 controller 에서 sheet 를 생성한다.
        Button newFile = (Button) findViewById(R.id.newFile);
        newFile.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (workBookView != null) {
                    workBookController.createWorkBook();
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.message_create_new_workBook), Toast.LENGTH_LONG).show();
                }
            }
        });

        // 파일 불러오기 버튼의 이벤트를 통해 인텐트로 파일 탐색기를 보여준다.
        Button loadFile = (Button) findViewById(R.id.loadFile);
        loadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workBookView != null) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, LOAD_CONTENT_REQUEST_CODE);
                }
            }
        });

        // 파일 저장하기 버튼의 이벤트를 통해 다이어로그로 fileName(workBookName)을 입력받는다.
        Button saveFile = (Button) findViewById(R.id.saveFile);
        saveFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

                alertDialogBuilder.setTitle(getResources().getString(R.string.title_save_workbook_name));
                alertDialogBuilder.setMessage(getResources().getString(R.string.sub_message_save_workbook_neme));

                final EditText workBookName = new EditText(MainActivity.this);
                alertDialogBuilder.setView(workBookName);

                String fileName = workBookController.getFileName();
                if(fileName != null){
                    String fileNameWithoutExtension = fileName.substring(0, fileName.length() - Hana.HANA_EXTENSION.length());
                    workBookName.setText(fileNameWithoutExtension);
                }

                // 확인버튼을 눌렀을 때
                alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (workBookView != null) {
                            String inputWorkBookName = workBookName.getText().toString();
                            // 한 글자 이상을 입력해야 한다.
                            if (inputWorkBookName.length() > 0) {
                                try {
                                    workBookController.saveHana(inputWorkBookName, getApplicationContext());
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_save_workBook_success), Toast.LENGTH_LONG).show();
                                    // saveHana 에서 Exception 이 발생할 수 있다.
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_save_workBook_fail), Toast.LENGTH_LONG).show();
                                }finally{
                                    dialog.dismiss();
                                }
                            // 입력한 WorkbookName이 한 글자 이상이 아닐 때
                            } else {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_save_workBook_fail_too_short), Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });

                alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alertDialogBuilder.show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_CONTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedFileURI;
                if (data.getData() != null) {
                    selectedFileURI = data.getData();
                    try {
                        // .hana파일만을 읽을 수 있으므로 확장자 체크를 한다.
                        if(selectedFileURI.toString().substring(selectedFileURI.toString().length() - Hana.HANA_EXTENSION.length() ,selectedFileURI.toString().length() ).equals(Hana.HANA_EXTENSION)) {
                            workBookController.readHana(selectedFileURI, this);
                            Toast.makeText(getApplicationContext(), getString(R.string.message_load_workBook_success), Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),getString(R.string.message_not_apply_extension), Toast.LENGTH_LONG).show();
                        }
                        // readHana 에서 Exception 이 발생할 수 있다.
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.message_load_workBook_fail), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ModelDataChangeListener.removeDataChangeable(workBookView);
    }
}




