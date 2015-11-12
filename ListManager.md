## ListManager 클래스 사용하기 ##
ListManager 클래스란 페이지 네비게이션을 포함한 데이타 목록을 만들어주는 클래스이다. ListManager 를 통해 몇가지 환경설정으로 페이징 처리를 포함한 List 쿼리를 자동으로 생성할 수 있고 페이지 네비게이션도 자동으로 생성할 수 있다.
```
ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER");

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```

## 검색조건 추가하기 ##
여러가지 검색조건을 추가할 수 있다.
```
ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER");
lm.addWhere("type = '02'");
lm.addWhere("status = 1");

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```

## addSearch 메소드 사용하기 ##
addSearch 메소드는 addWhere와 비슷하나 값이 없을 경우 검색조건이 붙지 않도록 해주는 기능이 있다.
```
String type = "02";
int status = 1;

ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER");
lm.addSearch("type", type); //만약 type 이 빈공백이거나 null 이면 조건이 무시됨
lm.addSearch("status", status, ">"); //세번째 파라미터는 연산기호를 의미함

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```

## 정렬옵션을 추가하기 ##
setOrderBy 메소드를 이용하여 정렬옵션을 추가할 수 있다.
```
ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER");
lm.addWhere("type = '02'");
lm.addWhere("status = 1");
lm.setOrderBy("id DESC");

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```

## 그룹옵션을 추가하기 ##
setGroupBy 메소드를 이용하여 그룹옵션을 추가할 수 있다.
```
ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER");
lm.setFields("type, count(*) cnt"); //데이타 칼럼을 지정할 수 있다.
lm.addWhere("type = '02'");
lm.addWhere("status = 1");
lm.setGroupBy("type");

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```

## 조인쿼리로 목록가져오기 ##
ListManager 는 복잡한 조인쿼리르 통해서도 목록 데이타를 가져올 수 있다.
```
ListManager lm = new ListManager();
lm.setRequest(request);
lm.setListNum(10); // 목록 갯수 지정
lm.setTable("TB_USER a JOIN TB_GROUP b ON b.user_id = a.id");
lm.setFields("a.name, b.group_name"); //데이타 칼럼을 지정할 수 있다.
lm.addWhere("a.status = 1");
lm.addWhere("b.type = '02'");
lm.setOrderBy("a.id DESC");

DataSet list = lm.getDataSet(); // 목록 데이타
int total = lm.getTotalNum(); // 검색 데이타 갯수
String pagebar = lm.getPaging(); // 페이지 네비게이션 태그
```