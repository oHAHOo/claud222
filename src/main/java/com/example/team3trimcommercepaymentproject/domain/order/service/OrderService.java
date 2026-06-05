package com.example.team3trimcommercepaymentproject.domain.order.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderPreviewRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.*;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.dto.OrderCancelDTO;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderItemResponse;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderPreviewItemResponse;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.service.PointTransactionService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundItemRepository;
import com.example.team3trimcommercepaymentproject.domain.refund.repository.RefundRepository;
import com.example.team3trimcommercepaymentproject.global.exception.BusinessException;
import com.example.team3trimcommercepaymentproject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {


    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PointTransactionService pointTransactionService;
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;

    @Transactional(readOnly = true)
    public Order getOrderEntity(Long orderId, Long memberId) {
        return orderRepository.findOrderDetailByIdAndMemberId(orderId, memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 주문서 미리보기
     **/
    @Transactional(readOnly = true)
    public OrderPreviewResponse preview(Long memberId, OrderPreviewRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findAllByMemberId(memberId);

        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        List<Long> cartItemIds = request.cartItemIds();

        List<CartItem> targetCartItems;

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            targetCartItems = cartItems;
        } else {
            targetCartItems = cartItems.stream()
                    .filter(cartItem -> cartItemIds.contains(cartItem.getId()))
                    .toList();

            if (targetCartItems.isEmpty() || targetCartItems.size() != cartItemIds.size()) {
                throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
            }
        }

        List<OrderPreviewItemResponse> previewItems = new ArrayList<>();
        Long totalAmount = 0L;

        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK);
            }

            Long price = product.getPrice().longValue();
            Integer quantity = cartItem.getQuantity();
            Long subtotalAmount = price * quantity;

            OrderPreviewItemResponse previewItem = new OrderPreviewItemResponse(
                    cartItem.getId(),
                    product.getId(),
                    product.getName(),
                    price,
                    quantity,
                    subtotalAmount
            );

            previewItems.add(previewItem);
            totalAmount += subtotalAmount;
        }

        return new OrderPreviewResponse(previewItems, totalAmount);
    }

    /**
     * 주문 생성
     **/
    @Transactional
    public OrderCreateResponse createOrderWithPayment(Long memberId, OrderCreateRequest request) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_EMPTY));

        List<CartItem> cartItems = cartItemRepository.findAllByMemberId(memberId);
        if (cartItems.isEmpty()) throw new BusinessException(ErrorCode.CART_EMPTY);

        List<CartItem> targetCartItems = selectCartItems(cartItems, request.cartItemIds());

        validateCartItems(targetCartItems, request.cartItemIds());

        Long totalAmount = 0L;

        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            validateProduct(product, cartItem.getQuantity());

            totalAmount += product.getPrice().longValue() * cartItem.getQuantity();
        }

        Long usedPoint = request.usedPoint() == null ? 0L : request.usedPoint();
        Long pgAmount = totalAmount - usedPoint;
        Long earnedPoint = pgAmount / 100;

        String orderNumber = generateOrderNumber();
        String portonePaymentId = generatePortonePaymentId(orderNumber);

        Order order = Order.builder()
                .member(cart.getMember())
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .usedPoint(usedPoint)
                .pgAmount(pgAmount)
                .earnedPoint(earnedPoint)
                .build();

        for (CartItem cartItem : targetCartItems) {
            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productNameSnapshot(product.getName())
                    .priceSnapshot(product.getPrice().longValue())
                    .quantity(cartItem.getQuantity())
                    .build();

            order.addOrderItem(orderItem);

            product.decreaseStock(cartItem.getQuantity());
        }

        Payment payment = Payment.builder()
                .portonePaymentId(portonePaymentId)
                .totalAmount(totalAmount)
                .usedPoint(usedPoint)
                .pgAmount(pgAmount)
                .earnedPoint(earnedPoint)
                .build();

        order.assignPayment(payment);

        Order savedOrder = orderRepository.save(order);

        Payment savedPayment = savedOrder.getPayment();

        return new OrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getStatus(),
                new PaymentCreateResponse(
                        savedPayment.getId(),
                        savedPayment.getPortonePaymentId(),
                        savedPayment.getStatus(),
                        savedPayment.getTotalAmount(),
                        savedPayment.getUsedPoint(),
                        savedPayment.getPgAmount(),
                        savedPayment.getEarnedPoint()

                )
        );
    }


    /**
     * 주문 내역 조회
     **/
    public OrderPageResponse findOrders(Long memberId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findOrderPageByMemberId(memberId, pageable);

        List<OrderSummaryResponse> orders = orderPage.getContent().stream()
                .map(order -> new OrderSummaryResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                )).toList();
        return new OrderPageResponse(
                orders,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    /**
     * 주문상세조회
     **/
    public OrderDetailResponse findByIdOrder(Long memberId, Long orderId) {

        Order order = orderRepository.findOrderDetailByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Payment payment = order.getPayment();

        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getId(),
                        orderItem.getProduct().getId(),
                        orderItem.getProductNameSnapshot(),
                        orderItem.getPriceSnapshot(),
                        orderItem.getQuantity(),
                        orderItem.getRefundedQuantity(),
                        orderItem.getSubtotalAmount()
                ))
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                payment.getStatus(),
                order.getTotalAmount(),
                order.getUsedPoint(),
                order.getPgAmount(),
                order.getEarnedPoint(),
                items,
                order.getCreatedAt()
        );
    }

    /**
     * 주문취소
     **/
    @Transactional
    public OrderCancelDTO cancel(Long memberId, Long orderId, OrderCancelRequest cancelRequest) {
        Order order = getOrderEntity(memberId, orderId);
        Payment payment = order.getPayment();

        // 결제 상태가 PG사에 요청을 보내야 하는 상태인지 검사
        boolean needsPgCancel = false;
        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.PARTIAL_REFUNDED) {
            needsPgCancel = true;
        }

        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.getProduct().increaseStock(orderItem.getQuantity());
        }

        order.cancel(cancelRequest.cancelReason());
        payment.cancel();

        // 결제 완료 상태였던 경우만 포인트 정산 (0원이면 트랜잭션 생성 안 함)
        if (needsPgCancel) {
            if (payment.getEarnedPoint() > 0)
                pointTransactionService.cancelEarnPoint(memberId, payment, payment.getEarnedPoint());
            if (payment.getUsedPoint() > 0)
                pointTransactionService.restoreUsedPoint(memberId, payment, payment.getUsedPoint());
        }

        OrderCancelResponse response = new OrderCancelResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                payment.getStatus(),
                order.getCancelReason(),
                order.getCanceledAt()
        );

        return new OrderCancelDTO(response, payment.getPortonePaymentId(), cancelRequest.cancelReason(), needsPgCancel, payment.getId());
    }

    private List<CartItem> selectCartItems(List<CartItem> cartItems, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return cartItems;
        }

        List<CartItem> targetCartItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            if (cartItemIds.contains(cartItem.getId())) {
                targetCartItems.add(cartItem);
            }
        }

        return targetCartItems;
    }

    private String generatePortonePaymentId(String orderNumber) {
        String random = java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        return "PAY-" + orderNumber + "-" + random;
    }

    private String generateOrderNumber() {
        String date = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String random = java.util.UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        return "ORD-" + date + "-" + random;
    }

    private void validateCartItems(List<CartItem> targetCartItems, List<Long> cartItemIds) {
        if (targetCartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        if (cartItemIds != null && !cartItemIds.isEmpty()
                && targetCartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
    private void validateProduct(Product product, Integer quantity) {
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (product.getStockQuantity() < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
    }

}


