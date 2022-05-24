package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

public class MessageCodesResolverTest {

  MessageCodesResolver codeResolver = new DefaultMessageCodesResolver();

  @Test
  void messageCodesResolverObject() {
    String[] strings = codeResolver.resolveMessageCodes("required", "item");
    for (String string : strings) {
      System.out.println("string = " + string);
    }
   // new ObjectError("item", new String[]{"required.item", "required"});

    Assertions.assertThat(strings).containsExactly("required.item", "required");

  }


  @Test
  void messageCodesResolverField() {
    String[] strings = codeResolver.resolveMessageCodes("required", "item", "itemName", String.class);
    for (String string : strings) {
      System.out.println("string = " + string);
    }
  }
}
