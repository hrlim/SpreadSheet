# SpreadSheet
SpreadSheet는 Android Studio로 제작하였습니다.
View 클래스를 상속받은 하나의 Custom View로 구현합니다.

## 기능

+ WorkBook을 생성하고, Sheet와 Cell에 대해 편집이 가능합니다.
+ Hana(*.hana)파일을 불러오고, 작업한 내용을 Hana(*.hana)파일로 저장합니다.

## 동작

### Cell에 대한 동작
>+ singleTap : Cell 선택하기
>+ doubleTap : Cell 편집하기

### Sheet에 대한 동작
>+ onLongPress : Sheet에 대한 diolog 생성(Sheet이름 바꾸기, Sheet순서 바꾸기, Sheet 삭제하기)


## 미구현

+ Cell에 대한 Context Menu를 제공하지 않습니다.
+ 화면 회전에 대한 고려하지 않습니다.
+ Zoom in/out의 이벤트가 두 터치 이벤트의 중심점으로 이루어지지 않습니다. 
++(임시로 화면의 왼쪽 상단을 기준으로 발생하게 하였습니다.)

## 프로젝트 구조

### activity
>+ Activity에서는 View를 그리며, View에 Controller를 연결한다.
>+ 새 문서 만들기, Hana(*.hana)파일 불러오기, Hana(*.hana)파일로 저장하는 버튼들이 구성되어 있습니다.
>+ 파일 탐색기를 호출해 Intent를 통해 얻은 데이터를 Controller에 전달합니다. 

### ctrl(controller)
>+ controller는 View와 Model를 연결합니다.
>+ View에게 Model의 갱신된 데이터를 알려줍니다.

### model
>+ Model에는 WorkBook과 Sheet, Cell이 존재합니다.

### view
>+ View는 디바이스 화면에 보여지는 부분을 어떠한 방식으로 그려낼 지를 정의합니다.

### utils
>+ 지원하는 함수(SUM, AVG)에 대해 정의합니다. 
>+ View에게 Model이 갱신되었음을 알려주는 Custom Listener이 있습니다.
>+ Custom Extension인 Hana(*.hana)를 불러오고 저장하는 Class를 포함하고 있습니다.
>+ Cell에 대한 데이터를 화면에 그려주는 Class를 포함합니다. 

## 제약조건
+ Custom Extenstion인 Hana(*.hana)파일만을 지원합니다.
+ 한글 입력은 고려하지 않습니다.
