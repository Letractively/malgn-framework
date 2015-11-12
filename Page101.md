## Step1 ##
회원 테이블에서 회원 데이타를 가져와 화면에 출력하기
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject();
DataSet rs = dao.query("SELECT * FROM tb_user WHERE name LIKE '%'");
while(rs.next()) {
  rs.put("name", rs.getString("name") + "장군");
  String email = rs.getString("email");
  if("".equals(email)) email = "empty@gmail.com";
  rs.put("email", email);
}

p.setLayout(null);
p.setBody("main.index");
p.setLoop("users", rs);
p.display(out);

%>
```
while문 안에 있는 내용은 데이타베이스에서 가져온 정보를 화면에 출력할 때 필요에 따라 적절히 내용을 수정하기 위한 것이다. 위 예에서는 이름 옆에 "장군"이란 호칭을 붙이고 이메일 주소가 없는 사용자는 "empty@gmail.com" 이란 이메일을 출력하라는 내용이다.
```
<html>
<body>
<h3>회원목록</h3>
<ul>
  <!-- LOOP START 'users' -->
  <li>{{users.name}} ({{users.email}})</li>
  <!-- LOOP END 'users' -->
</ul>
</body>
</html>
```

## Step2 ##
특정회원의 정보를 가져와 화면에 출력하기
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject();
DataSet info = dao.query("SELECT name, email FROM tb_user WHERE name = '홍길동'");
if(!info.next()) {
  m.jsError("회원정보를 찾을 수 없습니다.");
  return;
}

p.setLayout(null);
p.setBody("main.index");
p.setVar(info);
p.display(out);

%>
```
데이타베이스에서 가져온 데이타의 레코드 갯수가 하나일 경우 if문을 통해 next() 메소드를 실행하고 그 조건에 따라 처리한다.
```
<html>
<body>
<h3>회원목록</h3>
<ul>
  <li>이름 : {{name}}</li>
  <li>이메일 : {{email}}</li>
</ul>
</body>
</html>
```

## Step3 ##
회원정보를 등록하기
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");
dao.item("name", "홍길동");
dao.item("email", "hong@gmail.com");
dao.insert();

%>
```
먼저 DataObject 객체를 생성할 때 회원정보 테이블명을 파라미터로 넣어준다.  그 다음 등록할 회원의 항목들을 item 메소드에 지정하고 insert 메소드를 호출한다. insert 메소드의 리턴값은 boolean 이다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject();
dao.execute("INSERT INTO tb_user (name, email) VALUES ('홍길동', 'hong@gmail.com')");

%>
```
execute 메소도의 리턴값은 int 이며, 등록된 레코드의 갯수이다.

## Step4 ##
회원정보를 수정하기
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");
dao.item("email", "hong@gmail.com");
dao.update("name = '홍길동'");

%>
```
수정할 항목들을 item 메소드를 통해 지정하고 update 메소드의 파라미터로 수정 조건을 넣어준다. 아래와 같이 UPDATE SQL문을 사용해서도 수정이 가능하다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject();
dao.execute("UPDATE tb_user SET email = 'hongkildong@gmail.com' WHERE name = '홍길동'");

%>
```

## Step5 ##
회원정보를 삭제하기
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");
dao.delete("name = '홍길동'");

%>
```
delete 메소드에 삭제할 회원정보의 조건을 넣어준다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject();
dao.execute("DELETE FROM tb_user WHERE name = '홍길동'");

%>
```