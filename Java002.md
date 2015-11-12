### 변수명 ###
변수명은 대소문자가 구분된 영문 또는 영문,숫자의 조합으로 이루어진다. 그러나 숫자로 시작할 수는 없다. 일반적으로 소문자로 시작하며 여러 단어를 조합해서 변수명을 만들 경우 두번째 단어부터는 단어의 첫단어를 대문자로 시작해서 만든다.
```
id
username
userName
_userName
```

### 클래스명, 메소드명 ###
클래스명은 대문자로 시작하며 여러 단어를 조합할 경우 변수명과 같이 다른 단어가 시작될때 대문자로 시작한다.
```
User
Group
Product
Order
```
메소드명은 소문자로 시작하며 주로 동사형의 단어를 사용한다.<br>
여러 단어가 조합될 경우 동사 + 명사 형태의 단어로 조합된다.<br>
<pre><code>User.getName();<br>
Group.addGroup();<br>
Product.setPrice();<br>
Order.process();<br>
</code></pre>
get 으로 시작하는 메소드는 일반적으로 데이타를 가져오는 메소드이며<br>
set 또는 put 시작하는 메소드는 일반적으로 데이타를 지정하는 메소드이다.<br>
<br>
<h3>코멘트</h3>
프로그램 소스안에서 코멘트는 아래와 같은 두가지 방식을 이용한다.<br>
<pre><code>//String id = "abc";<br>
<br>
/*<br>
String id = "abc";<br>
*/<br>
</code></pre>