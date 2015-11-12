## 레이아웃 페이지 만들기 ##
레이아웃 페이지는 반드시 템플릿 폴더 아래에 있는 layout 폴더에 layout\_xxx.html 와 같은 파일명으로 만들어야 한다.
```
/html/layout/layout_sample.html

<html>
<body>
<div>레이아웃 상단</div>

<!-- INCLUDE NAME 'BODY' -->

<div>레이아웃 하단</div>
</body>
</html>
```


## 바디(Body) 페이지 만들기 ##
이제 화면 출력을 원하는 바디페이지를 만들어 보자
```
/html/sample/index.html

<h1>샘플</h1>
<p>샘플페이지 입니다.</p>
```


## 레이아웃페이지와 바디페이지가 결합된 웹페이지 만들기 ##
두개의 웹페이지를 결합해서 하나의 웹페이지를 출력하는 JSP 를 만들어보자
```
/sample/index.jsp

<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout("sample"); // layout_sample.html 을 의미한다.
p.setBody("sample.index"); // sample 폴더안의 index.html 을 의미한다.
p.display(out);

%>
```

## 레이아웃 페이지 없이 출력하기 ##
레이아웃을 지정하지 않고 바디페이지만 출력하고자 할 경우 아래와 같이 할 수 있다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.setLayout(null); // 레이아웃을 지정하지 않는다.
p.setBody("sample.index"); // sample 폴더안의 index.html 을 의미한다.
p.display(out);

%>
```

## print 함수를 이용하여 출력하기 ##
print 함수를 이용할 경우 템플릿 폴더를 기준으로 정확한 경로와 파일명을 적어주어야 한다.
```
<%@ page contentType="text/html; charset=utf-8" %><%@ include file="../init.jsp" %><%

p.print(out, "sample/index.html");

%>
```