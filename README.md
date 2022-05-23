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