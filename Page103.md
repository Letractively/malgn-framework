## Step1 ##
html 페이지에 validate.js 를 추가한다.
```
<html>
<script language="javascript" src="/html/js/lib.validate.js"></script>
<body>
<h3>회원가입</h3>

<form name="form1" method="POST" action="user_insert.jsp">
  <p><label>이름</label><br/><input type="text" name="name"></p>
  <p><label>이메일</label><br/><input type="text" name="email" size="50"></p>
  <p><input type="submit" value="회원가입"></p>
</form>
{{form_script}}
</body>
</html>
```
`</form>` 태그 다음에 {{form\_script}} 라는 템플릿 변수명도 추가한다.

## Step2 ##
jsp 프로그램에서 form\_script 를 지정한다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");

f.addElement("name", null, "hname:'이름', required:'Y'");
f.addElement("email", null, "hname:'이메일', required:'Y', option:'email'");

p.setLayout(null);
p.setBody("main.user_insert");
p.setVar("form_script", f.getScript());
p.display(out);

%>
```
f.addElement 메소드를 이용해서 유효성 검사를 위한 옵션값을 지정한다.
f.getScript() 메소드를 통해 자바스크립트 유효성 검사 코드를 가져온다.

## Step3 ##
등록 프로세스에서 html 과 jsp 소스를 정리하면 아래와 같다.
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
p.setVar("form_script", f.getScript());
p.display(out);

%>
```
```
<html>
<script language="javascript" src="/html/js/lib.validate.js"></script>
<body>
<h3>회원가입</h3>

<form name="form1" method="POST">
  <p><label>이름</label><br/><input type="text" name="name"></p>
  <p><label>이메일</label><br/><input type="text" name="email" size="50"></p>
  <p><input type="submit" value="회원가입"></p>
</form>
{{form_script}}
</body>
</html>
```

## Step4 ##
수정 프로세스에서 html 과 jsp 소스를 정리하면 아래와 같다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataObject dao = new DataObject("tb_user");
DataSet info = dao.query("SELECT * FROM tb_user WHERE name = '홍길동'");

if(!info.next()) {
  m.jsError("회원정보가 없습니다.");
  return;
}

f.addElement("name", info.getString("name"), "hname:'이름', required:'Y'");
f.addElement("email", info.getString("email"), "hname:'이메일', required:'Y', option:'email'");

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
p.setVar("form_script", f.getScript());
p.display(out);

%>
```
```
<html>
<script language="javascript" src="/html/js/lib.validate.js"></script>
<body>
<h3>회원정보수정</h3>

<form name="form1" method="POST">
  <p><label>이름</label><br/><input type="text" name="name"></p>
  <p><label>이메일</label><br/><input type="text" name="email" size="50"></p>
  <p><input type="submit" value="회원가입"></p>
</form>
{{form_script}}
</body>
</html>
```