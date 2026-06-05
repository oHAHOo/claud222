# 결제 테스트 페이지 활용 가이드

> **대상:** 커머스 결제 시스템 프로젝트 수강생

---

## 파일 구성

```
src/main/resources/static/
├── config.js          ← 팀별 PortOne 키 입력 (여기만 수정)
├── index.html         ← 일반 결제창 · 포인트 복합결제 테스트
└── subscription.html  ← 구독 결제수단 등록(빌링키 발급) 테스트
```

Spring Boot를 실행하면 별도 컨트롤러 없이 아래 URL로 접근할 수 있습니다.

| 페이지 | URL |
|---|---|
| 일반 결제 | `http://localhost:8080/` 또는 `/index.html` |
| 구독 결제수단 등록 | `http://localhost:8080/subscription.html` |

---

## 시작 전 준비

### 1. config.js 설정

`config.js`를 열어 세 값을 입력합니다.
[PortOne 콘솔](https://admin.portone.io)에서 발급받은 값을 사용하세요.

```js
const CONFIG = {
  storeId:               "store-xxxx-...",       // 가맹점 식별코드
  channelKey:            "channel-key-xxxx-...", // KG이니시스 인증결제 채널
  subscriptionChannelKey:"channel-key-yyyy-...", // 토스페이먼츠 빌링키 채널
};
```

> 💡 **채널이 두 종류인 이유**
> 일반 결제(카드 인증)와 구독(빌링키 발급)은 PG사가 다릅니다.
> PortOne 콘솔에서 각각 별도 채널을 추가해야 합니다.

### 2. 서버 실행

```bash
./gradlew bootRun
```

---

## 일반 결제 테스트 (`index.html`)

> **이 페이지의 역할:** PortOne 결제창을 열고, 완료된 콜백 결과(`portonePaymentId`)를 화면에 표시합니다.
> 결제 확정 API 호출은 **Postman에서 직접** 수행합니다.

### 기본 흐름

```
① Postman : 로그인 → 토큰 발급
② Postman : 장바구니 담기
③ Postman : 주문 생성  →  응답: portonePaymentId, 결제 금액
④ 웹페이지 : portonePaymentId + 금액 입력 → 결제창 열기
⑤ PortOne 결제창 : 카드 정보 입력 → 결제 완료
⑥ 웹페이지 : 결과 확인 (portonePaymentId 복사)
⑦ Postman : 결제 확정 API 호출
```

---

### 케이스 A : 카드 단독 결제 (포인트 미사용)

**③ Postman 주문 생성 요청 예시**

```http
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "cartItemIds": [1, 2],
  "usePoint": 0
}
```

**응답 예시**

```json
{
  "orderId": "ORD-20260526-001",
  "portonePaymentId": "payment-20260526-abc123",
  "pgAmount": 68000
}
```

**④ 웹페이지 입력**

| 필드 | 입력값 |
|---|---|
| 포트원 결제 ID | `payment-20260526-abc123` (응답의 `portonePaymentId`) |
| 결제 금액 | `68000` (응답의 `pgAmount`) |
| 주문명 | 아무 값 (예: 스파르타 후드집업 외 1건) |

**⑦ Postman 결제 확정 요청 예시**

```http
POST http://localhost:8080/api/payments/confirm
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "portonePaymentId": "payment-20260526-abc123",
  "orderId": "ORD-20260526-001"
}
```

> ⚠️ API 경로와 요청 바디 필드명은 팀이 설계한 대로 사용하세요.

---

### 케이스 B : 포인트 복합결제 (포인트 일부 사용)

포인트와 카드를 함께 쓰는 경우입니다.
**서버가 계산한 포인트 차감 후 실결제 금액을 웹페이지에 입력**하는 것이 핵심입니다.

**③ Postman 주문 생성 요청 예시**

```http
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "cartItemIds": [1, 2],
  "usePoint": 10000
}
```

**응답 예시**

```json
{
  "orderId": "ORD-20260526-002",
  "portonePaymentId": "payment-20260526-def456",
  "pgAmount": 58000
}
```

> 포인트 차감 후 실결제 금액 = 상품 금액(68,000) - 포인트 사용액(10,000) = 58,000
> 포인트 차감 계산은 서버에서 완료된 값입니다.

**④ 웹페이지 입력**

| 필드 | 입력값 |
|---|---|
| 포트원 결제 ID | `payment-20260526-def456` |
| 결제 금액 | `58000` ← 포인트 차감 후 금액 |

> 💡 웹페이지는 단순히 이 금액을 PortOne에 전달합니다.
> PortOne이 `58,000원`으로 결제창을 열고, 서버는 이 금액을 검증합니다.

**⑦ Postman 결제 확정 요청 예시**

```http
POST http://localhost:8080/api/payments/confirm
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "portonePaymentId": "payment-20260526-def456",
  "orderId": "ORD-20260526-002",
  "usePoint": 10000
}
```

---

### 케이스 C : 포인트 전액 결제 (PortOne 결제창 불필요)

포인트만으로 전액 결제하는 경우, **PortOne 결제창을 열 필요가 없습니다.**
이 테스트 페이지를 거치지 않고 Postman에서 바로 처리합니다.

**③ Postman 주문 생성 요청 예시**

```http
POST http://localhost:8080/api/orders
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "cartItemIds": [3],
  "usePoint": 15000
}
```

**응답 예시**

```json
{
  "orderId": "ORD-20260526-003",
  "portonePaymentId": "payment-20260526-ghi789",
  "pgAmount": 0
}
```

포인트 차감 후 실결제 금액이 0이면 PortOne 결제창 없이 바로 결제 확정 API를 호출합니다.

**Postman 결제 확정 요청 예시 (PortOne 결제창 없이 바로)**

```http
POST http://localhost:8080/api/payments/confirm
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "portonePaymentId": "payment-20260526-ghi789",
  "orderId": "ORD-20260526-003",
  "usePoint": 15000
}
```

> 서버는 포인트 차감 후 실결제 금액이 0임을 확인하고 포인트만으로 결제를 완료 처리합니다.

---

## 구독 결제수단 등록 테스트 (`subscription.html`)

> **이 페이지의 역할:** PortOne 빌링키 발급창을 열고, 완료된 `billingKey`를 화면에 표시합니다.
> 구독 생성 API 호출은 **Postman에서 직접** 수행합니다.

### 흐름

```
① 웹페이지 : 결제자 정보 입력 → 카드 등록 클릭
② PortOne 빌링키 발급창 : 카드 정보 입력
   (토스페이먼츠 테스트 인증번호: 000000)
③ 웹페이지 : billingKey 확인 후 복사
④ Postman : 구독 생성 API 호출 (billingKey 포함)
```

### ③ 웹페이지에서 billingKey 확인

발급이 완료되면 노란 박스에 `billingKey`가 표시됩니다.
복사 버튼으로 클립보드에 복사하세요.

```
billingKey: billing-key-xxxx-yyyy-zzzz-...
```

### ④ Postman 구독 생성 요청 예시

```http
POST http://localhost:8080/api/subscriptions
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "billingKey": "billing-key-xxxx-yyyy-zzzz-...",
  "planId": 1
}
```

> ⚠️ API 경로와 요청 바디 필드명은 팀이 설계한 대로 사용하세요.

---

## 자주 겪는 문제

### 결제창이 열리지 않는다

- `config.js`의 `storeId`, `channelKey` 값이 올바른지 확인하세요.
- PortOne 콘솔에서 채널이 **테스트 모드**로 설정되어 있는지 확인하세요.
- 브라우저 팝업 차단이 활성화된 경우 허용으로 변경하세요.

### "결제창 열기" 버튼을 눌렀는데 바로 오류가 난다

- `portonePaymentId`가 비어 있지 않은지 확인하세요.
- `portonePaymentId` 값이 서버에서 이미 사용된 ID가 아닌지 확인하세요.
  (같은 ID로 두 번 결제창을 열 수 없습니다. 새 주문을 생성하세요.)

### 결제 완료 후 확정 API에서 금액 불일치 오류가 난다

- 웹페이지에 입력한 **결제 금액**이 서버가 기록한 포인트 차감 후 실결제 금액과 다른 경우 발생합니다.
- 주문 생성 응답의 포인트 차감 후 실결제 금액을 그대로 입력했는지 확인하세요.

### 빌링키 발급창이 열리지 않는다

- `config.js`의 `subscriptionChannelKey`가 **토스페이먼츠 빌링키 채널**인지 확인하세요.
  (KG이니시스 채널이 아닙니다.)
- PortOne 콘솔에서 토스페이먼츠 채널을 별도로 추가했는지 확인하세요.

### 테스트 카드 / 인증 정보

| 항목 | 값 |
|---|---|
| KG이니시스 테스트 카드 | PortOne 콘솔 테스트 모드에서 임의 카드번호 입력 가능 |
| 토스페이먼츠 인증번호 | `000000` |
