## Step 1 ##
아래와 같이 웹프로그램 루트폴더에 아래 파일을 만든다.
```
/init.jsp
```

```
<%@ page import="java.util.*, java.io.*, dao.*, malgnsoft.db.*, malgnsoft.util.*" %><%

String tplRoot = Config.getTplRoot();

Malgn m = new Malgn(request, response, out);

Form f = new Form("form1");
f.setRequest(request);

Page p = new Page(tplRoot);
p.setRequest(request);
p.setPageContext(pageContext);

%>
```

## Step2 ##
프로그램 jsp 파일을 만든다.
```
/main/index.jsp
```
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout(null);
p.setBody("main.index");
p.display(out);

%>
```

## Step3 ##
프로그램과 연결될 html 파일을 만든다.
```
/html/main/index.html
```
```
<html>
<body>
시작 페이지 입니다.
</body>
</html>
```

## Step4 ##
기본적인 템플릿 변수 치환은 아래와 같이 처리한다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout(null);
p.setBody("main.index");
p.setVar("title", "시작 페이지");
p.display(out);

%>
```
```
<html>
<body>
{{title}} 입니다.
</body>
</html>
```

## Step5 ##
루프 템플릿 변수 치환은 아래와 같이 처리한다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

DataSet rs = new DataSet();

rs.addRow();
rs.put("name", "홍길동");
rs.put("email", "hong@gmail.com");

rs.addRow();
rs.put("name", "김철수");
rs.put("email", "kim@gmail.com");

p.setLayout(null);
p.setBody("main.index");
p.setVar("title", "시작 페이지");
p.setLoop("users", rs);
p.display(out);

%>
```
```
<html>
<body>
{{title}} 입니다.
<h3>회원목록</h3>
<ul>
  <!-- LOOP START 'users' -->
  <li>{{users.name}} ({{users.email}})</li>
  <!-- LOOP END 'users' -->
</ul>
</body>
</html>
```

## Step6 ##
조건 치환은 아래와 같이 처리한다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout(null);
p.setBody("main.index");
p.setVar("title", "시작 페이지");
p.setVar("is_admin", true);
p.display(out);

%>
```
조건 치환은 변수값에 true 나 false 를 지정함으로 사용가능하다. true 값 대신에 임의의 값을 넣어도 된다. 이 경우 임의의 값이 있으면 true 값이 없으면 false 가 된다.
```
<html>
<body>
{{title}} 입니다.
<!-- IF START 'is_admin' -->
<p>관리자페이지 바로가기</p>
<!-- IF END 'is_admin' -->
</body>
</html>
```