package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
@Slf4j
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;


    //이 컨트롤러가 요청 될 떄 마다 validator를 항상 넣어둔다.
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    //@Validated  : WebDataBind에 등록한 검증기를 실행하라라는 의미의 Anno, 그결과가 bindingResult에 담긴다.
    //이때 supports 메서드가 사용된다.
    @PostMapping("/add")
    public String addItemV6(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        //검증에 실패하면 다시 입력 폼으로
        //BindingResult는 자동으로 view에 넘어가기때문에, Model에 안담아도 된다.
        if (bindingResult.hasErrors()) {
            log.info("Errors = {}", bindingResult);
            return "validation/v3/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    private boolean hasError(boolean errors) {
        return !errors;
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated(UpdateCheck.class) @ModelAttribute Item item, BindingResult bindingResult) {
        //글로벌 에러의 처리
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000)
                bindingResult.reject("totalPriceMin", new Object[]{1000, resultPrice},"default");
            }


        if (bindingResult.hasErrors()) {
            log.info("Errors = {}", bindingResult);
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

