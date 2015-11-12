## Dao 클래스 만들기 ##
Dao 클래스는 일반적으로 테이블당 하나씩 생성하며 DataObject 클래스를 상속해서 만든다.
```
package dao;

import malgnsoft.db.*;

public class UserDao extends DataObject {

  public UserDao() {

    this.table = "TB_USER";

  }

}
```

## Dao 객체 생성하기 ##
Dao 객체를 다음과 같이 생성해서 사용할 수 있다.
```

UserDao user = new UserDao();
DataSet info = user.find("id = 'hopegiver'");

```

## Dao 클래스에 속성 추가하기 ##
Dao 클래스는 DataObject 클래스를 상속받기 때문에 DataObject 클래스의 모든 속성을 사용할 수 있다. 특별한 속성을 추가하고 싶은 경우 아래와 같이 만들 수 있다.
```
public class UserDao extends DataObject {

  public String[] types = {"01=>일반회원", "02=>운영자", "03=>최고관리자" };

}
```

## Dao 클래스에 메소드 추가하기 ##
Dao 클래스는 DataObject 클래스를 상속받기 때문에 DataObject 클래스의 모든 메소드를 사용할 수 있다. 특별한 메소드를 추가하고 싶은 경우 아래와 같이 만들 수 있다.
```
public class UserDao extends DataObject {

  public DataSet getUserName(String userId) {

    return getOne("SELECT name FROM TB_USER WHERE id = '" + userId + "'");

  }

  public DataSet getAdminList(String type) {

    return query("SELECT * FROM TB_USER WHERE type = '02'");

  }

}
```

## Dao 클래스 활용법 ##
맑은프레임워크에서는 특정 테이블에 대한 데이타 접근을 되도록이면 Dao 클래스를 통해서 하도록 권하고 있다. 또한 JSP 페이지안에서는 되도록 SQL 을 사용을 자제하고 있으며 SQL 은 Dao 클래스 안에서 사용하도록 권하고 있다. 이를 통해 데이타 레이어와 로직 레이어를 되도록 분리하고자 하였다.