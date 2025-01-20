package com.nhnacademy.hello.common.controller.cart;



import com.nhnacademy.hello.common.feignclient.CartAdapter;
import com.nhnacademy.hello.controller.advice.GlobalControllerAdvice;
import com.nhnacademy.hello.controller.book.BookController;
import com.nhnacademy.hello.controller.cart.CartController;
import com.nhnacademy.hello.image.ImageStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CartController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = GlobalControllerAdvice.class
        ),
        excludeAutoConfiguration = {ThymeleafAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImageStore imageStore;

    @MockitoBean
    private CartAdapter cartAdapter;

    @Test
    @DisplayName("GET /cart - Return Cart Page")
    void testCartPage() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart"));
    }

    @Test
    @DisplayName("POST /cart/getBookImages - Return Book Images")
    void testGetBookImages() throws Exception {
        List<String> bookIds = List.of("1", "2", "3");
        Mockito.when(imageStore.getImage("bookThumbnail_1")).thenReturn(List.of("/images/image1.jpg"));
        Mockito.when(imageStore.getImage("bookThumbnail_2")).thenReturn(List.of("/images/image2.jpg"));
        Mockito.when(imageStore.getImage("bookThumbnail_3")).thenReturn(List.of("/images/default-book.jpg"));

        String requestBody = "[\"1\", \"2\", \"3\"]";

        mockMvc.perform(post("/cart/getBookImages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1").value("/images/image1.jpg"))
                .andExpect(jsonPath("$.2").value("/images/image2.jpg"))
                .andExpect(jsonPath("$.3").value("/images/default-book.jpg"));
    }

    @Test
    @DisplayName("POST /cart/{memberId} - Save Cart")
    void testSaveCart() throws Exception {
        String memberId = "testMember";
        String cartData = "{\"items\": [\"item1\", \"item2\"]}";

        // 반환값 설정
        Mockito.when(cartAdapter.setCart(eq(memberId), eq(cartData)))
                .thenReturn(ResponseEntity.ok("Cart saved successfully"));

        mockMvc.perform(post("/cart/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cartData))
                .andExpect(status().isOk());

        Mockito.verify(cartAdapter, Mockito.times(1)).setCart(eq(memberId), eq(cartData));
    }
}
