package com.example.team3trimcommercepaymentproject.domain.order.service;

import com.example.team3trimcommercepaymentproject.domain.cart.entity.Cart;
import com.example.team3trimcommercepaymentproject.domain.cart.entity.CartItem;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartItemRepository;
import com.example.team3trimcommercepaymentproject.domain.cart.repository.CartRepository;
import com.example.team3trimcommercepaymentproject.domain.member.entity.Member;
import com.example.team3trimcommercepaymentproject.domain.member.service.MemberService;
import com.example.team3trimcommercepaymentproject.domain.order.dto.OrderCancelDTO;
import com.example.team3trimcommercepaymentproject.domain.order.dto.OrderPartialRefundDTO;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCancelRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderCreateRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.OrderPreviewRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.PartialRefundItemRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.request.PartialRefundRequest;
import com.example.team3trimcommercepaymentproject.domain.order.dto.response.*;
import com.example.team3trimcommercepaymentproject.domain.order.entity.Order;
import com.example.team3trimcommercepaymentproject.domain.order.repository.OrderRepository;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderItemResponse;
import com.example.team3trimcommercepaymentproject.domain.orderItem.dto.response.OrderPreviewItemResponse;
import com.example.team3trimcommercepaymentproject.domain.orderItem.entity.OrderItem;
import com.example.team3trimcommercepaymentproject.domain.payment.dto.response.PaymentCreateResponse;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.Payment;
import com.example.team3trimcommercepaymentproject.domain.payment.entity.PaymentStatus;
import com.example.team3trimcommercepaymentproject.domain.pointTransaction.service.PointTransactionService;
import com.example.team3trimcommercepaymentproject.domain.product.entity.Product;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.Refund;
import com.example.team3trimcommercepaymentproject.domain.refund.entity.RefundItem;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberService memberService;
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

    /**
     * 부분(또는 전액) 환불 - 단일 트랜잭션으로 모든 DB 변경 처리
     **/
    @Transactional
    public OrderPartialRefundDTO partialRefund(Long memberId, Long orderId, PartialRefundRequest request) {
        Order order = getOrderEntity(orderId, memberId);
        Payment payment = order.getPayment();

        Map<Long, OrderItem> itemMap = order.getOrderItems().stream()
            .collect(Collectors.toMap(OrderItem::getId, item -> item));

        // 환불 대상 수집 및 총액 계산
        List<OrderItem> targetItems = new ArrayList<>();
        List<Integer> targetQty = new ArrayList<>();
        long rawTotalRefund = 0;

        for (PartialRefundItemRequest itemReq : request.items()) {
            OrderItem oi = itemMap.get(itemReq.orderItemId());
            rawTotalRefund += oi.getPriceSnapshot() * itemReq.quantity();
            targetItems.add(oi);
            targetQty.add(itemReq.quantity());
        }

        // 마지막 부분환불 여부 판단 (이번 환불 후 모든 아이템 잔여 수량이 0인지)
        boolean isLastRefund = order.getOrderItems().stream().allMatch(oi -> {
            int requested = request.items().stream()
                .filter(r -> r.orderItemId().equals(oi.getId()))
                .mapToInt(PartialRefundItemRequest::quantity)
                .findFirst()
                .orElse(0);
            return oi.getRefundableQuantity() - requested == 0;
        });

        // 마지막 환불 보정: 반올림 누적 오차 제거
        long totalRefundAmount;
        if (isLastRefund) {
            long alreadyRefunded = refundItemRepository.sumRefundedAmountByPaymentIdAndStatus(
                payment.getId(), Refund.RefundStatus.COMPLETED);
            totalRefundAmount = payment.getTotalAmount() - alreadyRefunded;
        } else {
            totalRefundAmount = rawTotalRefund;
        }

        // 포인트/PG 환불 금액 분리 산정
        long pointRefundAmount;
        long pgRefundAmount;
        long earnCancelAmount;

        if (payment.getPgAmount() == 0) {
            // 포인트 전액 결제 예외: PG 환불 없음
            pointRefundAmount = totalRefundAmount;
            pgRefundAmount = 0;
            earnCancelAmount = 0;
        } else {
            long rawPoint = totalRefundAmount * payment.getUsedPoint() / payment.getTotalAmount();
            long rawPg = totalRefundAmount * payment.getPgAmount() / payment.getTotalAmount();
            pointRefundAmount = rawPoint;
            pgRefundAmount = rawPg + (totalRefundAmount - rawPoint - rawPg); // 잔돈은 PG에 가산

            earnCancelAmount = payment.getEarnedPoint() * pgRefundAmount / payment.getPgAmount();
        }

        // 포인트 부족 정책: 회수할 적립 포인트가 잔액 부족 시 부족분을 PG 환불액에 가산
        if (earnCancelAmount > 0) {
            Member member = memberService.findMemberEntity(memberId);
            if (member.getPoint() < earnCancelAmount) {
                pgRefundAmount += earnCancelAmount - member.getPoint();
                earnCancelAmount = member.getPoint();
            }
        }

        // 주문 상품 환불 수량 갱신 + 재고 복구
        for (int i = 0; i < targetItems.size(); i++) {
            OrderItem oi = targetItems.get(i);
            oi.refundQuantity(targetQty.get(i));
            oi.getProduct().increaseStock(targetQty.get(i));
        }

        // 결제/주문 상태 전이
        if (isLastRefund) {
            order.cancel(request.reason()); // 주문 상태: 주문취소
            payment.refund();               // 결제 상태: 전액환불
        } else {
            payment.partialRefund();        // 결제 상태: 부분환불, 주문 상태 유지
        }

        // 포인트 갱신
        if (earnCancelAmount > 0)
            pointTransactionService.cancelEarnPoint(memberId, payment, earnCancelAmount);
        if (pointRefundAmount > 0)
            pointTransactionService.restoreUsedPoint(memberId, payment, pointRefundAmount);

        // 환불 레코드 저장 (REQUESTED 상태로 시작, 이후 Facade에서 COMPLETED/FAILED 처리)
        Refund refund = Refund.builder()
            .payment(payment)
            .reason(request.reason())
            .pointRefundPrice(pointRefundAmount)
            .pgRefundPrice(pgRefundAmount)
            .build();
        refundRepository.save(refund);

        for (int i = 0; i < targetItems.size(); i++) {
            OrderItem oi = targetItems.get(i);
            int qty = targetQty.get(i);
            refundItemRepository.save(RefundItem.builder()
                .refund(refund)
                .orderItem(oi)
                .refundedQuantity(qty)
                .refundedAmount(oi.getPriceSnapshot() * qty)
                .build());
        }

        return new OrderPartialRefundDTO(
            refund.getId(),
            payment.getPortonePaymentId(),
            pgRefundAmount,
            request.reason(),
            new PartialRefundResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                payment.getStatus(),
                totalRefundAmount,
                pointRefundAmount,
                pgRefundAmount
            )
        );
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
