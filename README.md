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


