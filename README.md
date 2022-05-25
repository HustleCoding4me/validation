# validation


# (타임리프 + 스프링부트 ) 검증 페이지 생성하기

방법 1. Model에 Error 담아서 view로 리턴
> Controller에서 검증하기

프론트 단 뿐만 아니라 백엔드에서도 검증을 수행해야 한다.


순서 
* Controller의 Model 객체에 'Error'로 담아 넘긴다.
* 오류가 발생할 경우, 다시 해당 Page 의 View를 보여준다.
* 타임리프의 조건문으로, 해당 오류가 나는 부분을 수정하라고 사용자에게 보여준다.



방법 2. 스프링이 제공하는 `BindingResult` 방법 사용하기

> BindingResult란?

검증 오류를 보관하는 객체이다. @ModelAttribute 뒤에 인자로 선언하여서 오류가 발생하면 담아서 사용하면 된다.
Model에 담지 않아도 자동으로 View화면에 넘어간다. (BindingResult가 없으면 400에러가 나서 오류페이지가 호출됨, 근데 그냥 형변환 같은 오류를 BindingResult에 담고 Controller가 그대로 수행된다.)
`@ModelAttribute`에 `데이터 바인딩시에 오류`가 발생해도 컨트롤러가 호출된다.( String -> Integer같이 형변환 잘못선언


> BindingResult 사용 종류

* 종류 1. 비즈니스 로직에 의해 원하는 오류를 발생시키기 (ex) 개수는 100개 이상 필수적으로 등록해야한다)
* 종류 2. 형변환에 대한 오류 (ex)개수를 적는곳에 문자가 들어간다.) 

> BindingResult 검증 오류를 적용하는 3가지 방법

* 방법 1. 스프링이 자동으로 넣어주는 방법. `@ModelAttribute` 의 타입 오류등으로 바인딩 실패하는 경우 `FieldError`를 생성하여 BindingResult에 담아준다.
* 방법 2. new FieldError를 생성하여 수동으로 넣어주기
* 방법 3. `Validator` 사용하기



* Controller에서 @ModelAttribute뒤에 BindingResult 인자 추가
* BindingResult에 검증된 에러 추가
* bindingResult는 스프링이 지원해줘서 Model에 추가하지 않아도 넘어간다.
* View에서 #field로 BindingResult가 지원하는 에러에 접근,
* th:errors, th:errorclass 등으로 조건 추가하여 오류가 발생시에 오류 추가

> 형변환 오류시에 BindingResult에 기본적으로 에러가 담겨서 넘어가는 모습 + 400 에러가 아닌, Controller를 실행하는 모습이다.

![Screen Shot 2022-05-23 at 1 34 45 PM](https://user-images.githubusercontent.com/37995817/169743990-88e9c28b-0bc8-4df1-810e-be5cc1b64631.png)
![Screen Shot 2022-05-23 at 1 35 19 PM](https://user-images.githubusercontent.com/37995817/169744055-76499481-afbf-414e-b2ab-27f40f097214.png)




> 오류 발생시에도 값을 유지하는 방법 FieldError를 통해 적용

Integer에 문자열을 담으면, 임시적으로 사용자가 작성한 것을 담아둘 곳이 없어진다.
그래서 FieldError의 `rejectedValue` 인자에 오류 발생시, 사용자의 value를 저장해두어서 값을 유지해주는 역할을 한다.

타임리프의 th:field는 오류가 나면, 자동으로 FieldError에 보관한 값을 사용해서 값을 출력해준다.

> FieldError 생성자

```java
	public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure,
			@Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage) {

ex) bindingResult.addError(new FieldError("item","price",item.getPrice(), false, null,null,"가격은 1,000 ~ 1,000,000 까지 허용합니다."));
```
* objectName : 넘어온 객체 이름 item
* field : 객체의 필드 itemName
* rejectedValue : 실패할 경우 오류와 함께 유지될 사용자 작성 글
||messageProperties와 같이 가져와서 메세지를 참조하여 보여줄 수 있다.||
* codes : 메세지 코드
* arguments : 메세지 코드의 arguments



```html
<div>
            <label for="price" th:text="#{label.item.price}">가격</label>
            <input type="text" id="price" th:field="*{price}"
                   th:errorclass="field-error"
                   class="form-control" placeholder="가격을 입력하세요">
            <div class="field-error" th:errors="*{price}"></div>
</div>
```


```java
 @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
  
        //검증 로직
        if (hasError(StringUtils.hasText(item.getItemName()))) {
            bindingResult.addError(new FieldError("item","itemName",item.getItemName(), false, null,null,"상품 이름은 필수 입니다."));
        }
        
        //검증에 실패하면 다시 입력 폼으로
        //BindingResult는 자동으로 view에 넘어가기때문에, Model에 안담아도 된다.
        if (bindingResult.hasErrors()) {
            log.info("Errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }
```

* @ModelAttribute 뒤에 BindingResult를 붙임
* 이 BindingResult는 형변환 오류 등이 있으면 자동으로 error를 담아 옴
* addError 메서드를 통해 어떤 객체의 어떤 변수인지, reject시 어떤 변수들을 계속 유지할지, 어떤 메세지를 남길지 적어둘 수 있음
* 이걸 보고 th:field, th:error, th:errorclass를 사용하여 상황에 대처 가능하다.

> properties에 따로 빼서 오류 메세징 처리

프로젝트에서, 특정 메세지들을 따로 빼서 관리하는 것은 코드의 일관성을 좋게 한다.

1. `application.properties`에 `'spring.messages.basename=messages,errors'`를 추가한다. (resources 하위에 messages, errors 폴더 밑의 properties들을 쓰겠다는 의미)
2. properties 파일을 추가한다

```properties
required.item.itemName=상품 이름은 필수입니다.t
range.item.price=가격은 {0} ~ {1} 까지 허용합니다.t
max.item.quantity=수량은 최대 {0} 까지 허용합니다.t
totalPriceMin=가격 * 수량의 합은 {0}원 이상이어야 합니다. 현재 값 = {1}t
```

3. BindingResult에 넣을 FieldError에 code, arguments 생성자를 추가해준다.
```java
bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000,resultPrice},"");
```




BindingResult는 앞에 Target뒤에 바로 작성을 해야 하기 때문에, 이미 Target 정보를 담고 있다고 생각하면 된다.


> `bindingReseult.rejectValue()`, `bindingResult.reject` 사용법 (bindingResult.addError(new Field...) 대체

매번 FieldError, ObjectError를 추가해서 담아주기에는 너무 번거로워서, 스프링에서는 더욱 축약을 위해
rejectValue, reject를 사용한다. (이미 BindingResult는 어떤 객체의 어떤 속성들을 사용하는지 위치로 알고 있기 때문에)

```java

void rejectValue(@Nullable String field, String errorCode,
        @Nullable Object[] errorArgs, @Nullable String defaultMessage);

```

* field : 해당 객체의 오류 필드
* errorCode : 어떤 오류인지
* errorArgs : 오류의 인자들 (properties에 정의된)

> reject, rejectValue 예시 

```java
@PostMapping("/add")
public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        //검증 로직
        if (hasError(StringUtils.hasText(item.getItemName()))) {
            bindingResult.rejectValue("itemName", "required");
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price", "range");
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max");
        }
}
```

* bindingResult는 앞에 item의 정보를 담고 있기 때문에,(바로 앞의 ModelAttribute를 담는다.) 축약하여 사용.


> Error Properties 정의 법

오류 코드가 디테일 할 수록 범용성이 떨어지고, ex) required.item.itemname=아이템 이름은 필수적입니다.
단순할 수록 여러 곳에서 사용할 수 있지만, 세밀함이 부족해진다. ex)required=필수 값입니다.


> bindingResult.reject(), rejectValue()의 errorCode + error프로퍼티를 통해 단계적으로 오류코드 설정법

가장 좋은 방법은 범용적으로 사용하다 세밀하게 적용되도록 단계를 두는 것이다.

* 기존 방법
 ```java
            bindingResult.rejectValue("itemName", "required");
```

```properties
#Level1
required.item.itemName=아이템 이름은 필수입니다.
#Level2
required=필수 값입니다.
```

범용 -> 세밀 순으로 찾아가게 설계를 하면 된다.

* 개선 방법


 ```java
            bindingResult.rejectValue("itemName", "required");
            ex) new String[]{"required.item.itemName","required"} 추가
```


먼저 개발을 이렇게 하면, properties만으로 수정이 된다.
스프링은 `MessageCodesResolver`로 이런 기능을 지원한다.


* `MessageCodesResolver`의 기능을 사용하면, 프로퍼티 레벨별로 디테일한 순서대로 가져온다.

```java
  @Test
  void messageCodesResolverField() {
    String[] strings = codeResolver.resolveMessageCodes("required", "item", "itemName", String.class);
    for (String string : strings) {
      System.out.println("string = " + string);
    }
  }
```

 
```java
/Library/Java/JavaVirtualMachines/jdk-11.0.14.jdk/Contents/Home/bin/java -ea -Didea.test.cyclic.buffer.size=1048576 -javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=50303:/Applications/IntelliJ IDEA.app/Contents/bin -Dfile.encoding=UTF-8 -classpath /Users/woodie/.m2/repository/org/junit/platform/junit-platform-launcher/1.7.1/junit-platform-launcher-1.7.1.jar:/Users/woodie/.m2/repository/org/apiguardian/apiguardian-api/1.1.0/apiguardian-api-1.1.0.jar:/Users/woodie/.m2/repository/org/junit/platform/junit-platform-engine/1.7.1/junit-platform-engine-1.7.1.jar:/Users/woodie/.m2/repository/org/opentest4j/opentest4j/1.2.0/opentest4j-1.2.0.jar:/Users/woodie/.m2/repository/org/junit/platform/junit-platform-commons/1.7.1/junit-platform-commons-1.7.1.jar:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar:/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit5-rt.jar:/Applications/IntelliJ IDEA.app/Contents/plugins/junit/lib/junit-rt.jar:/Users/woodie/project/validation/out/test/classes:/Users/woodie/project/validation/out/production/classes:/Users/woodie/project/validation/out/production/resources:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-thymeleaf/2.4.4/b6cfa0786720394dea233a85c06774bae4f26732/spring-boot-starter-thymeleaf-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-web/2.4.4/8b84b6800a0b72d644d77aea3df0bf02008096a7/spring-boot-starter-web-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-test/2.4.4/bf35adef93978c3e8458e4e12a3c220641e8b461/spring-boot-starter-test-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter/2.4.4/5807f7ab098711f28d7d92c5986c6c5cfd82e996/spring-boot-starter-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.thymeleaf/thymeleaf-spring5/3.0.12.RELEASE/aa640b214411978a23cbe271c3fb9569d1bda608/thymeleaf-spring5-3.0.12.RELEASE.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.thymeleaf.extras/thymeleaf-extras-java8time/3.0.4.RELEASE/36e7175ddce36c486fff4578b5af7bb32f54f5df/thymeleaf-extras-java8time-3.0.4.RELEASE.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-json/2.4.4/3f9622c37d6ece1cbf5889aee688adbcc78ddeea/spring-boot-starter-json-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-tomcat/2.4.4/80f3a79a16d80639741f35034364fc30c6e9016a/spring-boot-starter-tomcat-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-webmvc/5.3.5/449649af0eda09def9b5748bc8438253bf02f9f3/spring-webmvc-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-web/5.3.5/8233c67b3f33f619f6f8f34cc0f56e01a00e136e/spring-web-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-test-autoconfigure/2.4.4/2499705b47ff071d6157461662300710e9ffb306/spring-boot-test-autoconfigure-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-test/2.4.4/d7383048ee38e7bc551fb9e86cd31123c7aae603/spring-boot-test-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.jayway.jsonpath/json-path/2.4.0/765a4401ceb2dc8d40553c2075eb80a8fa35c2ae/json-path-2.4.0.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/jakarta.xml.bind/jakarta.xml.bind-api/2.3.3/48e3b9cfc10752fba3521d6511f4165bea951801/jakarta.xml.bind-api-2.3.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.assertj/assertj-core/3.18.1/aaa02680dd92a568a4278bb40aa4a6305f632ec0/assertj-core-3.18.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest/2.2/1820c0968dba3a11a1b30669bb1f01978a91dedc/hamcrest-2.2.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter/5.7.1/a4a80ea9b0cca47781edcf9f2d4f1f4f7ce9436e/junit-jupiter-5.7.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.mockito/mockito-junit-jupiter/3.6.28/23149890c3b6047604a682aa3d47151d440e1bfa/mockito-junit-jupiter-3.6.28.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.mockito/mockito-core/3.6.28/ad16f503916da658bd7b805816ae3b296f3eea4c/mockito-core-3.6.28.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.skyscreamer/jsonassert/1.5.0/6c9d5fe2f59da598d9aefc1cfc6528ff3cf32df3/jsonassert-1.5.0.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-test/5.3.5/404b15dbbc53cbf44cec01be3246b3c261ccde08/spring-test-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-core/5.3.5/633de7c79bfeccf05c81a0d4a32b3336010f06ab/spring-core-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.xmlunit/xmlunit-core/2.7.0/4d014eac96329c70175116b185749765cee0aad5/xmlunit-core-2.7.0.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-autoconfigure/2.4.4/6237b28c1bad51b175e5b91222292924322fda54/spring-boot-autoconfigure-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot/2.4.4/38392ae406009e55efe873baee4633bfa6b766b3/spring-boot-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework.boot/spring-boot-starter-logging/2.4.4/4432cf056309ece02eb23417bc70f96b59ac8c24/spring-boot-starter-logging-2.4.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/jakarta.annotation/jakarta.annotation-api/1.3.5/59eb84ee0d616332ff44aba065f3888cf002cd2d/jakarta.annotation-api-1.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.yaml/snakeyaml/1.27/359d62567480b07a679dc643f82fc926b100eed5/snakeyaml-1.27.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.thymeleaf/thymeleaf/3.0.12.RELEASE/de1865b0d58590a50c33900115a293335dd8ef25/thymeleaf-3.0.12.RELEASE.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.30/b5a4b6d16ab13e34a88fae84c35cd5d68cac922c/slf4j-api-1.7.30.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.datatype/jackson-datatype-jdk8/2.11.4/e1540dea3c6c681ea4e335a960f730861ee3bedb/jackson-datatype-jdk8-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.datatype/jackson-datatype-jsr310/2.11.4/ce6fc76bba06623720e5a9308386b6ae74753f4d/jackson-datatype-jsr310-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.module/jackson-module-parameter-names/2.11.4/432e050d79f2282a66c320375d628f1b0842cb12/jackson-module-parameter-names-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-databind/2.11.4/5d9f3d441f99d721b957e3497f0a6465c764fad4/jackson-databind-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.apache.tomcat.embed/tomcat-embed-websocket/9.0.44/3208d52d84bf2839f063a81382f9dc49f4864bc9/tomcat-embed-websocket-9.0.44.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.apache.tomcat.embed/tomcat-embed-core/9.0.44/227374f7179e4d34ae6611a20b63ac140f6880ee/tomcat-embed-core-9.0.44.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.glassfish/jakarta.el/3.0.3/dab46ee1ee23f7197c13d7c40fce14817c9017df/jakarta.el-3.0.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-context/5.3.5/15166e945c1221016a534f1aa83bbddf992dba0a/spring-context-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-aop/5.3.5/a52b30c37937ddb01585430bcc8442b2ac2a8b58/spring-aop-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-beans/5.3.5/7604a458b0d8a47cdb113cf874c21c9750b53188/spring-beans-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-expression/5.3.5/7149f94a2b134ffcd23cfd74f04ee1f1f2215347/spring-expression-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/net.minidev/json-smart/2.3/7396407491352ce4fa30de92efb158adb76b5b/json-smart-2.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/jakarta.activation/jakarta.activation-api/1.2.2/99f53adba383cb1bf7c3862844488574b559621f/jakarta.activation-api-1.2.2.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-params/5.7.1/6f81b3c053433a8a40a378d2b4f056c5c31e50ff/junit-jupiter-params-5.7.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-api/5.7.1/a7261dff44e64aea7f621842eac5977fd6d2412d/junit-jupiter-api-5.7.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy/1.10.22/ef45d7e2cd1c600d279704f492ed5ce2ceb6cdb5/byte-buddy-1.10.22.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.10.22/b01df6b71a882b9fde5a608a26e641cd399a4d83/byte-buddy-agent-1.10.22.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.objenesis/objenesis/3.1/48f12deaae83a8dfc3775d830c9fd60ea59bbbca/objenesis-3.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.vaadin.external.google/android-json/0.0.20131108.vaadin1/fa26d351fe62a6a17f5cda1287c1c6110dec413f/android-json-0.0.20131108.vaadin1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.springframework/spring-jcl/5.3.5/b4d8d3af78fb4e0f86534f75122e6896c37cf3f6/spring-jcl-5.3.5.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-classic/1.2.3/7c4f3c474fb2c041d8028740440937705ebb473a/logback-classic-1.2.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-to-slf4j/2.13.3/966f6fd1af4959d6b12bfa880121d4a2b164f857/log4j-to-slf4j-2.13.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.slf4j/jul-to-slf4j/1.7.30/d58bebff8cbf70ff52b59208586095f467656c30/jul-to-slf4j-1.7.30.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.attoparser/attoparser/2.0.5.RELEASE/a93ad36df9560de3a5312c1d14f69d938099fa64/attoparser-2.0.5.RELEASE.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.unbescape/unbescape/1.1.6.RELEASE/7b90360afb2b860e09e8347112800d12c12b2a13/unbescape-1.1.6.RELEASE.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-core/2.11.4/593f7b18bab07a76767f181e2a2336135ce82cc4/jackson-core-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-annotations/2.11.4/2c3f5c079330f3a01726686a078979420f547ae4/jackson-annotations-2.11.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/net.minidev/accessors-smart/1.2/c592b500269bfde36096641b01238a8350f8aa31/accessors-smart-1.2.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.apiguardian/apiguardian-api/1.1.0/fc9dff4bb36d627bdc553de77e1f17efd790876c/apiguardian-api-1.1.0.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.platform/junit-platform-commons/1.7.1/7c49f0074842d07f4335de2389d624a7437d1407/junit-platform-commons-1.7.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.opentest4j/opentest4j/1.2.0/28c11eb91f9b6d8e200631d46e20a7f407f2a046/opentest4j-1.2.0.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/ch.qos.logback/logback-core/1.2.3/864344400c3d4d92dfeb0a305dc87d953677c03c/logback-core-1.2.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.13.3/ec1508160b93d274b1add34419b897bae84c6ca9/log4j-api-2.13.3.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/5.0.4/da08b8cce7bbf903602a25a3a163ae252435795/asm-5.0.4.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.jupiter/junit-jupiter-engine/5.7.1/7fcfa59c5533abb41f86b1408960737eeaf1f49f/junit-jupiter-engine-5.7.1.jar:/Users/woodie/.gradle/caches/modules-2/files-2.1/org.junit.platform/junit-platform-engine/1.7.1/d276a968c57f5d60a421dedd1f8b6ca2fae09e86/junit-platform-engine-1.7.1.jar com.intellij.rt.junit.JUnitStarter -ideVersion5 -junit5 hello.itemservice.validation.MessageCodesResolverTest,messageCodesResolverField
string = required.item.itemName
string = required.itemName
string = required.java.lang.String
string = required

Process finished with exit code 0
```



`BindingResult.rejectValue("")`는 내부적으로 codeResolver를 사용하여 에러코드에 따라 계층적으로 프로퍼티에 찾아서 값을 가져온다.
선언적으로 써보자면 `BindingResult.rejectValue`는 아래와 같은 느낌이다.

```java
MessageCodesResolver codeResolver = new DefaultMessageCodesResolver();
    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult){

    bindingResult.addError(new FieldError(bindingResult.getObjectName(),"원하는 필드","item에 담겨 온 필드 값",null,codeResolver.resolveMessageCodes("required","item","itemName",String.class),args));
                                            오브젝트 네임 item / itemName / item.getItemName / null / 프로퍼티 키값들 new String[] {"",""} / 아규먼트들
    }
```


> 객체 오류 작성법

객체 오류의 경우 다음 순서로 2가지 생성 

1.: code + "." + object name 
2.: code

예) 오류 코드: required, object name: item 

1.: required.item
2.: required



> 필드 오류 작성법 (error.properties)

필드 오류의 경우 다음 순서로4가지 메시지 코드 생성
1.: code + "." + object name + "." + field
2.: code + "." + field
3.: code + "." + field type
4.: code
예) 오류 코드: typeMismatch, object name "user", field "age", field type: int 1. "typeMismatch.user.age"
2. "typeMismatch.age"
3. "typeMismatch.int"
4. "typeMismatch"


> 오류 불러오기 예시

1. `required.item.itemName`
2. `required.itemName`
3. `required.java.lang.String`
4. `required`



> why? 이렇게 사용할까

`bindingResult` `reject` 내부에 `MessageCodeResolver` 를 도입하면서까지 messageCode를 
디테일 -> 범용적인 코드로 나눠서 사용하는 이유는 개발할 때 편하기 위해서다.
`MessageCodeResolver`가 디테일 -> 범용 코드드 순으로 모두 가져와주기 때문에, 개발자는 범용적인 
ErrorCode를 작성한 뒤, 필요에 따라 디테일 한 부분을 추가해주면 되는 방식으로 개발할 수 있다.


> 간소화를 위한 ValidationUtils

ValidationUtils를 사용하면 간편한 공백, Empty 같은 것들을 간소화 할 수 있다.

```java
ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult,"itemName","required");

//검증 로직
if (hasError(StringUtils.hasText(item.getItemName()))) {
bindingResult.rejectValue("itemName", "required");
}
```

두 코드는 같은 내용이다. 세부적인 요소는 기존 방법대로 구현해야 한다. (ValidationUtils가 공백, empty밖에 제공되지 않으므로)


> 응용, 타입이 안맞을 경우 기본 메세지값 추가

 ex) 수량에 문자를 썼을 때처럼, 스프링에서 제공하는 exception 문구 대신 넣는 방법?

* BindingResult에 이미 FieldError가 담겨있고, 어떤 에러코드를 확인했는지 담겨있다.

`Field error in object 'item' on field 'price': rejected value [qq]; codes [typeMismatch.item.price,typeMismatch.price,typeMismatch.java.lang.Integer,typeMismatch]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [item.price,price]; arguments []; default message [price]]; default message [Failed to convert property value of type 'java.lang.String' to required type 'java.lang.Integer' for property 'price'; nested exception is java.lang.NumberFormatException: For input string: "qq"`

여기서 중점은 `[typeMismatch.item.price,typeMismatch.price,typeMismatch.java.lang.Integer,typeMismatch]`

error.properties에 해당 코드들을 설정해주면, 기본 값을 변경할 수 있다.

* errors.properties에 설정된 값으로 스프링 기본 형변환 에러의 문구를 수정하는 모습

![Screen Shot 2022-05-24 at 5 03 14 PM](https://user-images.githubusercontent.com/37995817/169980981-daa06b4e-8064-4245-8fd9-48755023e841.png)
![Screen Shot 2022-05-24 at 5 03 29 PM](https://user-images.githubusercontent.com/37995817/169981031-f850f943-79af-4a87-9326-3127537c95f2.png)



> ### 최종 Validation 기능 사용법


* 스프링의 `import org.springframework.validation.Validator`를 상속하여 만든 클래스로 만든 
`Validator`(ItemValidator)를 생성한다.
---
>> Validator 인터페이스는 supports, validate의 두 메서드로 이루어져 있는데,<br>
> supports로 Controller에서 @Validated가 붙을 시에 해당 @ModelAttribute가 붙은 객체가<br>
> 어떤 Validator로 검증되어야 하는지, Class로 비교하는 역할을 한다.
```java

public interface Validator {
  
	boolean supports(Class<?> clazz);

	void validate(Object target, Errors errors);
```

>> 구현한 모습

```java
@Component
public class ItemValidator implements Validator {
  
  //넘어오는 클래스가 아이템이 맞느냐, 자식까지 포함해서
  @Override
  public boolean supports(Class<?> clazz) {
    return Item.class.isAssignableFrom(clazz);
    //item == clazz
    //item == subItem
  }

  //supports로 허가 났으면, 검증 시작
  // * target = item, errors = 에러들
  //Errors가 BindingResult의 부모다.
  @Override
  public void validate(Object target, Errors errors) {
    Item item = (Item) target;

    ValidationUtils.rejectIfEmptyOrWhitespace(errors,"itemName","required");
    //검증 로직
    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
      errors.rejectValue("price", "range");
    }
    if (item.getQuantity() == null || item.getQuantity() >= 9999) {
      errors.rejectValue("quantity", "max");
    }

    //필드의 처리
    //글로벌 에러의 처리
    if (item.getPrice() != null && item.getQuantity() != null) {
      int resultPrice = item.getPrice() * item.getQuantity();
      if (resultPrice < 10000) {
        errors.reject("totalPriceMin");
      }
    }
    //검증에 실패하면 다시 입력 폼으로
    //BindingResult는 자동으로 view에 넘어가기때문에, Model에 안담아도 된다.
  }
}

```


---


* `@InitBinder` 를 컨트롤러에 선언하여, Controller에 요청이 들어올 때 마다 `WebDataBinder`에
Validator를 추가해준다. 


```java
//해당 컨트롤러가 요청 될 떄 마다 validator를 항상 넣어둔다.
private final ItemValidator itemValidator;
@InitBinder
public void init(WebDataBinder dataBinder) {
    dataBinder.addValidators(itemValidator);
}
```

---
* Controller에 인자 맨 앞에 `@Validated` 를 선언해주면, 해당 메서드에 @ModelAttribute 객체를 보고 확인하여
추가된 Validator들 중, 적합한 것을 찾아 검증을 실시한다.


```java
 //@Validated  : WebDataBind에 등록한 검증기를 실행하라라는 의미의 Anno, 그결과가 bindingResult에 담긴다.
    //이때 supports 메서드가 사용된다.
    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        
        //검증에 실패하면 다시 입력 폼으로
        //BindingResult는 자동으로 view에 넘어가기때문에, Model에 안담아도 된다.
        if (bindingResult.hasErrors()) {
            log.info("Errors = {}", bindingResult);
            return "validation/v2/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

```

---

```java
@Validated  : WebDataBind에 등록한 검증기를 실행하라라는 의미의 Anno, 그결과가 bindingResult에 담긴다.
    
WebDataBinder : 스프링 요청시마다 동작을 수행해주는 애라고만 알고 있으면 됨
 

```








## Bean Validation

---
Controller단에서 Validation을 구현하는 것은, 소스코드도 복잡해지고 한 눈에 들어오기 어려워진다.
그래서 Spring에서는 `Bean Validation`을 사용하도록 권장한다.

`Bean Validation`은 `JPA`처럼 추상 표준기술이다. 많은 구현체들이 존재하고, 대표적으로
`하이버네이트 Validation`이 있다.



---

## Gradle 설정

`implementation 'org.springframework.boot:spring-boot-starter-validation'` 을 추가해줘야 한다.