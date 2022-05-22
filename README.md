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

* Controller에서 @ModelAttribute뒤에 BindingResult 인자 추가
* BindingResult에 검증된 에러 추가
* bindingResult는 스프링이 지원해줘서 Model에 추가하지 않아도 넘어간다.
* View에서 #field로 BindingResult가 지원하는 에러에 접근,
* th:errors, th:errorclass 등으로 조건 추가하여 오류가 발생시에 오류 추가