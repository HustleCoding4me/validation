package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

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
