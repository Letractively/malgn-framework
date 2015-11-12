## Step1 ##
http://code.google.com/p/malgn-framework 사이트의 Downloads 페이지에서 malgn.jar 최신 파일을 다운로드 한다.

## Step2 ##
자바웹어플리케이션 루트폴더내의 /WEB-INF/lib 폴더안에 malgn.jar 파일을 복사한다.

## Step3 ##
/WEB-INF/config.xml 환경설정 파일을 만든다.
```
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <env>
        <jndi>jdbc/site</jndi>
    </env>
    <database>
        <jndi-name>jdbc/site</jndi-name>
        <driver>JDBC 드라이버</driver>
        <url>JDBC URL</url>
        <user>아이디</user>
        <password>암호</password>
        <max-active>20</max-active>
        <min-idle>2</min-idle>
        <max-wait-time>180</max-wait-time>
    </database>
</config>
```

## Step4 ##
/WEB-INF/web.xml 에 아래 내용을 추가한다.
```
<!-- 맨 처음 시작시에 환경설정값을 읽어오는 서블릿 실행 -->
<servlet>
    <servlet-name>ConfigServlet</servlet-name>
    <servlet-class>malgnsoft.util.Config</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
```

## Step5 ##
WAS (Web Application Server)를 재시작한다.