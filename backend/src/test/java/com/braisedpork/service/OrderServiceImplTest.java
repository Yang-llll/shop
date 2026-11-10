package com.braisedpork.service;

import com.braisedpork.dto.OrderResponse;
import com.braisedpork.entity.Order;
import com.braisedpork.entity.OrderItem;
import com.braisedpork.entity.Product;
import com.braisedpork.exception.ConflictException;
import com.braisedpork.exception.ForbiddenOperationException;
import com.braisedpork.repository.OrderItemRepository;
import com.braisedpork.repository.OrderRepository;
import com.braisedpork.repository.ProductRepository;
import com.braisedpork.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void cancelPendingOrderRestoresStockAndSalesCount() {
        Order order = order(1L, 7L, Order.Status.PENDING);
        OrderItem item = OrderItem.builder()
                .id(10L)
                .orderId(1L)
                .productId(9L)
                .productName("五花腊肉")
                .productPrice(new BigDecimal("68.00"))
                .quantity(2)
                .build();
        Product product = Product.builder()
                .id(9L)
                .name("五花腊肉")
                .price(new BigDecimal("68.00"))
                .stock(3)
                .salesCount(5L)
                .status(Product.Status.ON_SALE)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(product));
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        OrderResponse response = orderService.cancelOrder(7L, 1L);

        assertEquals(Order.Status.CANCELLED, order.getStatus());
        assertEquals(5, product.getStock());
        assertEquals(3L, product.getSalesCount());
        assertEquals("CANCELLED", response.getStatus());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrderRejectsOtherUsers() {
        Order order = order(1L, 7L, Order.Status.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ForbiddenOperationException.class, () -> orderService.cancelOrder(8L, 1L));
        verify(orderRepository, never()).save(order);
    }

    @Test
    void cancelOrderRejectsInvalidStatus() {
        Order order = order(1L, 7L, Order.Status.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.cancelOrder(7L, 1L));
        verify(orderRepository, never()).save(order);
    }

    private Order order(Long id, Long userId, Order.Status status) {
        return Order.builder()
                .id(id)
                .orderNo("ORD001")
                .userId(userId)
                .totalAmount(new BigDecimal("136.00"))
                .status(status)
                .build();
    }
}
