## Step1 ##
HTML 폼 태그를 이용한 회원가입 폼 작성
  * /main/user\_form.jsp
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout(null);
p.setBody("main.user_form");
p.display(out);

%>
```
  * /html/main/user\_form.html
```
<html>
<body>
<h3>회원가입</h3>

<form name="form1" method="POST" action="user_insert.jsp">
  <p><label>이름</label><br/><input type="text" name="name"></p>
  <p><label>이메일</label><br/><input type="text" name="email" size="50"></p>
  <p><input type="submit" value="회원가입"></p>
</form>

</body>
</html>
```

## Step2 ##
회원가입을 위한 jsp 프로그램을 아래와 같이 작성한다.
  * /main/user\_insert.jsp
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");

f.addElement("name", null, "hname:'이름', required:'Y'");
f.addElement("email", null, "hname:'이메일', required:'Y', option:'email'");

if(f.validate()) {

  dao.item("name", f.get("name"));
  dao.item("email", f.get("email"));
  dao.insert();
} else {
  m.jsError("회원가입 항목을 정확하게 넣어주세요.");
  return;
}

p.setLayout(null);
p.setBody("main.user_insert");
p.display(out);

%>
```
  * /html/main/user\_insert.html
```
<html>
<body>
<h3>회원가입</h3>
<p>성공적으로 회원가입이 완료되었습니다.</p>
</body>
</html>
```

## Step3 ##
위 user\_form.jsp 와 user\_insert.jsp 는 하나로 합쳐질 수 있다. 실제 맑은프레임워크에서는 합쳐진 형태의 프로그램 작성을 권하고 있다.
  * /main/user\_insert.jsp
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");

f.addElement("name", null, "hname:'이름', required:'Y'");
f.addElement("email", null, "hname:'이메일', required:'Y', option:'email'");

if(m.isPost() && f.validate()) {

  dao.item("name", f.get("name"));
  dao.item("email", f.get("email"));
  dao.insert();

  m.jsAlert("성공적으로 회원가입이 완료되었습니다.");
  m.jsReplace("user_insert_done.jsp");
  return;
}

p.setLayout(null);
p.setBody("main.user_insert");
p.display(out);

%>
```
  * /html/main/user\_insert.html
```
<html>
<body>
<h3>회원가입</h3>

<form name="form1" method="POST">
  <p><label>이름</label><br/><input type="text" name="name"></p>
  <p><label>이메일</label><br/><input type="text" name="email" size="50"></p>
  <p><input type="submit" value="회원가입"></p>
</form>

</body>
</html>
```
여기서 주의할 점은 form 태그안에 action 이란 속성이 없어졌다는 점이다. 이는 자기 자신에게 폼 데이타를 전송할 경우 action 을 생략할 수 있기 때문이다.

## Step4 ##
회원등록과 비슷하게 회원정보 수정 프로그램을 작성할 수 있다.
  * /main/user\_modify.jsp
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");
DataSet info = dao.find("name = '홍길동'");
if(!info.next()) {
  m.jsError("회원정보가 없습니다.");
  return;
}

f.addElement("name", info.getString("name"), "hname:'이름', required:'Y'");
f.addElement("email", info.getString("email"), "hname:'이메일', required:'Y', option:'email'");

if(m.isPost() && f.validate()) {

  dao.item("name", f.get("name"));
  dao.item("email", f.get("email"));
  dao.update("name = '홍길동'");

  m.jsAlert("회원정보가 수정되었습니다.");
  m.jsReplace("user_modify.jsp");
  return;
}

p.setLayout(null);
p.setBody("main.user_modify");
p.setVar(info);
p.display(out);

%>
```
  * /html/main/user\_modify.html
```
<html>
<body>
<h3>회원정보수정</h3>

<form name="form1" method="POST">
  <p><label>이름</label><br/><input type="text" name="name" value="{{name}}"></p>
  <p><label>이메일</label><br/><input type="text" name="email" value="{{email}}" size="50"></p>
  <p><input type="submit" value="정보수정"></p>
</form>

</body>
</html>
```