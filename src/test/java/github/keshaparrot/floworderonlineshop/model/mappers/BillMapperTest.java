package github.keshaparrot.floworderonlineshop.model.mappers;

import github.keshaparrot.floworderonlineshop.model.dto.BillDTO;
import github.keshaparrot.floworderonlineshop.model.dto.UserDTO;
import github.keshaparrot.floworderonlineshop.model.entity.Bill;
import github.keshaparrot.floworderonlineshop.model.entity.User;
import github.keshaparrot.floworderonlineshop.model.enums.BillType;
import github.keshaparrot.floworderonlineshop.services.interfaces.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class BillMapperTest {

    private BillMapper billMapper;
    private IUserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        billMapper = Mappers.getMapper(BillMapper.class);
        userService = org.mockito.Mockito.mock(IUserService.class);
        ReflectionTestUtils.setField(billMapper, "userService", userService);
    }

    @Test
    public void testToDtoMapping() {
        // Arrange: створюємо об'єкт Bill з тестовими даними
        Bill bill = new Bill();
        bill.setId(100L);
        bill.setIssueDate(LocalDateTime.of(2025, 3, 17, 10, 0));
        bill.setSeller("Test Seller");
        bill.setBuyer(42L); // buyer - це ідентифікатор
        bill.setProducts(Collections.singletonMap("Product1", 3));
        bill.setTotalAmount(new BigDecimal("150.00"));
        bill.setVat(new BigDecimal("30.00"));
        bill.setPaymentMethod("Credit Card");
        bill.setStatus("COMPLETED");
        bill.setType(BillType.BUYING); // Приклад використання enum'а
        bill.setTransactionNumber(UUID.randomUUID());
        bill.setSalesAddress("Test Address");
        bill.setCurrency("USD");

        // Stub: налаштовуємо мок, щоб при виклику getById(42L) поверталося тестове значення
       User user = User.builder()
               .firstName("John")
               .lastName("Doe")
               .build();
        when(userService.getEntityById(42L)).thenReturn(user);

        // Act: конвертуємо Bill в BillDTO
        BillDTO dto = billMapper.toDto(bill);

        // Assert: перевіряємо, що всі поля мапляться правильно, зокрема buyer
        assertNotNull(dto);
        assertEquals(bill.getId(), dto.getId());
        assertEquals(bill.getIssueDate(), dto.getIssueDate());
        assertEquals(bill.getSeller(), dto.getSeller());
        // buyer має містити повне ім'я, отримане з DummyUser
        assertEquals("John Doe", dto.getBuyer());
        assertEquals(bill.getProducts(), dto.getProducts());
        assertEquals(bill.getTotalAmount(), dto.getTotalAmount());
        assertEquals(bill.getVat(), dto.getVat());
        assertEquals(bill.getPaymentMethod(), dto.getPaymentMethod());
        assertEquals(bill.getStatus(), dto.getStatus());
        assertEquals(bill.getType(), dto.getType());
        assertEquals(bill.getTransactionNumber(), dto.getTransactionNumber());
        assertEquals(bill.getSalesAddress(), dto.getSalesAddress());
        assertEquals(bill.getCurrency(), dto.getCurrency());
    }
}
