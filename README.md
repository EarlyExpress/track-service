# Track Service

> Early Express 플랫폼의 배송 추적을 담당하는 마이크로서비스

## 📋 개요

Track Service는 주문 단위의 전체 배송 추적을 담당합니다.
Order Service, Hub Delivery Service, Last Mile Service로부터 이벤트를 수신하여 실시간 배송 상태를 관리하고, 사용자에게 배송 추적 정보를 제공합니다.
DDD(Domain-Driven Design) 아키텍처를 기반으로 설계되었으며, 이벤트 기반 아키텍처(EDA)로 배송 상태를 동기화합니다.

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.5.7, Spring Cloud 2025.0.0 |
| **Language** | Java 21 |
| **Database** | PostgreSQL + pgvector |
| **ORM** | Spring Data JPA, QueryDSL 5.1.0 |
| **Message Queue** | Apache Kafka (Spring Cloud Stream) |
| **Service Discovery** | Netflix Eureka Client |
| **Config** | Spring Cloud Config |
| **Security** | Spring Security, OAuth 2.0 (Keycloak) |
| **Service Communication** | OpenFeign (Hub Delivery, Last Mile 연동) |
| **Observability** | Micrometer, Zipkin, Loki, Prometheus |

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                         Track Service                           │
├─────────────────────────────────────────────────────────────────┤
│  Presentation Layer                                             │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   │
│  │ TrackMaster     │ │ TrackHubManager │ │ TrackCompanyUser│   │
│  │ Controller      │ │ Controller      │ │ Controller      │   │
│  │ (마스터 관리자)  │ │ (허브 관리자)    │ │ (업체 사용자)   │   │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│  Application Layer                                              │
│  ┌───────────────────────┐  ┌───────────────────────────────┐  │
│  │  TrackQueryService    │  │  TrackEventHandler            │  │
│  │  (조회 전용)           │  │  (이벤트 처리)                 │  │
│  └───────────────────────┘  └───────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Domain Layer                                                   │
│  ┌─────────────────┐  ┌─────────────────────────────────────┐  │
│  │ Track (AR)      │  │ Value Objects                       │  │
│  │ - TrackEvent    │  │ - TrackId, TrackStatus, TrackPhase  │  │
│  │ - 추적 로직     │  │ - HubSegmentInfo, DeliveryIds       │  │
│  └─────────────────┘  └─────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  Infrastructure Layer                                           │
│  ┌──────────────┐  ┌────────────────┐  ┌────────────────────┐  │
│  │ JPA Entity   │  │ HubDelivery    │  │ LastMileDelivery   │  │
│  │ TrackEntity  │  │ Client (Feign) │  │ Client (Feign)     │  │
│  └──────────────┘  └────────────────┘  └────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 📦 도메인 모델

### Track (Aggregate Root)

주문 단위 전체 배송 추적을 관리하는 핵심 도메인 모델입니다.

```
Track
├── TrackId (식별자)
├── orderId / orderNumber (주문 정보)
├── originHubId / destinationHubId (허브 정보)
├── hubDeliveryId (허브 배송 ID)
├── DeliveryIds (배송 ID 묶음)
│   ├── hubSegmentDeliveryIds (허브 구간별 ID 목록)
│   └── lastMileDeliveryId (최종 배송 ID)
├── HubSegmentInfo (허브 구간 진행 정보)
│   ├── totalSegments (전체 구간 수)
│   ├── currentSegmentIndex (현재 구간)
│   ├── completedSegments (완료된 구간 수)
│   └── currentFromHubId / currentToHubId
├── requiresHubDelivery (허브 배송 필요 여부)
├── TrackStatus (추적 상태)
├── TrackPhase (현재 단계 상세)
├── estimatedDeliveryTime / actualDeliveryTime
├── startedAt / completedAt
└── Audit Fields
```

### TrackEvent (추적 이벤트 이력)

```
TrackEvent
├── id (이벤트 ID)
├── trackId (추적 ID 참조)
├── TrackEventType (이벤트 타입)
├── occurredAt (발생 시간)
├── hubId (허브 ID)
├── segmentIndex (구간 순서)
├── description (설명)
└── source (이벤트 발생 서비스)
```

### 추적 상태 흐름 (TrackStatus)

```
                    ┌──────────────────┐
                    │     CREATED      │ 추적 준비 완료
                    └────────┬─────────┘
                             │
           ┌─────────────────┴─────────────────┐
           │                                   │
           │ (허브 배송 필요)                    │ (허브 배송 불필요)
           ▼                                   │
┌──────────────────┐                           │
│  HUB_IN_PROGRESS │ 허브 이동 중              │
└────────┬─────────┘                           │
         │                                     │
         │ (모든 허브 구간 완료)                │
         │                                     │
         ▼                                     ▼
┌──────────────────────────────────────────────────┐
│            LAST_MILE_IN_PROGRESS                 │ 최종 배송 중
└────────────────────────┬─────────────────────────┘
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
       ┌───────────┐         ┌───────────┐
       │ COMPLETED │         │  FAILED   │
       │  (완료)   │         │  (실패)   │
       └───────────┘         └───────────┘
```

### 추적 단계 상세 (TrackPhase)

```
[허브 배송 단계]
WAITING_HUB_DEPARTURE → HUB_IN_TRANSIT → HUB_ARRIVED → HUB_DELIVERY_COMPLETED
       (출발 대기)         (이동 중)        (도착)          (허브 배송 완료)

[최종 배송 단계]
WAITING_LAST_MILE → LAST_MILE_PICKED_UP → LAST_MILE_IN_TRANSIT → DELIVERED
   (대기)              (픽업 완료)           (배송 중)            (완료)
```

### 추적 이벤트 타입 (TrackEventType)

| 타입 | 설명 |
|------|------|
| `TRACKING_STARTED` | 추적 시작 |
| `HUB_SEGMENT_DEPARTED` | 허브 구간 출발 |
| `HUB_SEGMENT_ARRIVED` | 허브 구간 도착 |
| `HUB_SEGMENT_DELAYED` | 허브 구간 지연 |
| `LAST_MILE_PICKED_UP` | 최종 배송 픽업 |
| `LAST_MILE_DEPARTED` | 최종 배송 출발 |
| `LAST_MILE_DELIVERED` | 배송 완료 |
| `LAST_MILE_FAILED` | 최종 배송 실패 |
| `TRACKING_COMPLETED` | 추적 완료 |
| `TRACKING_FAILED` | 추적 실패 |

## 🔌 API 엔드포인트

### Master API (마스터 관리자)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/track/web/master/tracks` | 전체 추적 목록 (상태 필터 + 페이징) |
| `GET` | `/v1/track/web/master/tracks/status/{status}` | 상태별 추적 목록 |
| `GET` | `/v1/track/web/master/tracks/{trackId}` | 추적 상세 조회 |
| `GET` | `/v1/track/web/master/orders/{orderId}/tracking` | 주문 ID로 추적 조회 |

### Hub Manager API (허브 관리자)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/track/web/hub-manager/tracks/waiting-departure` | 출발 대기 중 목록 |
| `GET` | `/v1/track/web/hub-manager/tracks/hub-in-progress` | 허브 이동 중 목록 |
| `GET` | `/v1/track/web/hub-manager/tracks/last-mile-in-progress` | 최종 배송 중 목록 |
| `GET` | `/v1/track/web/hub-manager/tracks/completed` | 완료 목록 |
| `GET` | `/v1/track/web/hub-manager/tracks/{trackId}` | 추적 상세 조회 |

### Company User API (업체 사용자)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/v1/track/web/company-user/orders/{orderId}/tracking` | 내 주문 배송 추적 |

### 응답 예시 (Company User)

```json
{
  "success": true,
  "data": {
    "trackId": "track-uuid",
    "orderId": "order-uuid",
    "orderNumber": "ORD-2025-001234",
    "status": "HUB_IN_PROGRESS",
    "currentPhase": "HUB_IN_TRANSIT",
    "statusDescription": "허브 이동 중",
    "phaseDescription": "허브 간 이동 중",
    "totalHubSegments": 3,
    "completedHubSegments": 1,
    "progressPercent": 33,
    "estimatedDeliveryTime": "2025-01-15T18:00:00",
    "startedAt": "2025-01-15T10:30:00",
    "timeline": [
      {
        "eventId": "event-uuid-1",
        "eventType": "TRACKING_STARTED",
        "eventDescription": "추적 시작",
        "occurredAt": "2025-01-15T10:30:00"
      },
      {
        "eventId": "event-uuid-2",
        "eventType": "HUB_SEGMENT_DEPARTED",
        "eventDescription": "허브 구간 출발",
        "occurredAt": "2025-01-15T10:35:00",
        "hubId": "hub-1",
        "segmentIndex": 0
      },
      {
        "eventId": "event-uuid-3",
        "eventType": "HUB_SEGMENT_ARRIVED",
        "eventDescription": "허브 구간 도착",
        "occurredAt": "2025-01-15T12:00:00",
        "hubId": "hub-2",
        "segmentIndex": 0
      }
    ]
  },
  "message": "배송 추적 정보를 조회했습니다."
}
```

## ⚙️ 환경 설정

### 필수 환경 변수

```bash
# Application
APP_PORT=4014
APP_NAME=track-service
APP_PROFILE=dev

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=default_db
DB_USERNAME=postgres
DB_PASSWORD=postgres123!

# Eureka
EUREKA_DEFAULT_ZONE=https://www.pinjun.xyz/eureka1/eureka/,https://www.pinjun.xyz/eureka2/eureka/
EUREKA_INSTANCE_HOSTNAME=192.168.0.42

# Config Server
CONFIG_SERVER_URL=https://www.pinjun.xyz/config

# Kafka
KAFKA_BOOTSTRAP_SERVERS=61.254.69.188:9092,61.254.69.188:9093,61.254.69.188:9094
KAFKA_CONSUMER_GROUP_ID=track-service-group

# Keycloak (OAuth 2.0)
KEYCLOAK_ISSUER_URI=https://www.pinjun.xyz/keycloak/realms/codefactory
KEYCLOAK_CLIENT_ID=user
KEYCLOAK_CLIENT_SECRET=user-password

# Feign Clients
CLIENT_HUB_DELIVERY_SERVICE_URL=http://hub-delivery-service:8080
CLIENT_LAST_MILE_DELIVERY_SERVICE_URL=http://last-mile-delivery-service:8080

# Observability
ZIPKIN_ENABLED=true
ZIPKIN_BASE_URL=https://www.pinjun.xyz/zipkin
LOKI_ENABLED=true
LOKI_URL=https://www.pinjun.xyz/loki/api/v1/push
PROMETHEUS_PUSHGATEWAY_ENABLED=true
PROMETHEUS_PUSHGATEWAY_URL=https://www.pinjun.xyz/prometheus/pushgateway
```

## 🚀 실행 방법

### 로컬 개발 환경

```bash
# 1. 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 2. Gradle 빌드
./gradlew clean build

# 3. 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 직접 실행
java -jar build/libs/track-service-0.0.1-SNAPSHOT.jar
```

### Docker 실행

```bash
docker build -t track-service .
docker run -p 4014:4014 --env-file .env track-service
```

## 📨 Kafka 이벤트

Track Service는 **이벤트 소비자(Consumer)** 역할이 주요합니다.
여러 서비스로부터 배송 상태 이벤트를 수신하여 추적 정보를 업데이트합니다.

### 이벤트 흐름도

```
┌───────────────┐     ┌────────────────────┐     ┌────────────────────┐
│ Order Service │     │ Hub Delivery Svc   │     │ Last Mile Svc      │
└───────┬───────┘     └──────────┬─────────┘     └──────────┬─────────┘
        │                        │                          │
        │ tracking-start-        │ hub-segment-             │ last-mile-
        │ requested              │ departed/arrived         │ departed/completed
        │                        │                          │
        ▼                        ▼                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                         Track Service                                │
│                                                                      │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ OrderEvent       │  │ HubDeliveryEvent │  │ LastMileEvent    │   │
│  │ Consumer         │  │ Consumer         │  │ Consumer         │   │
│  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘   │
│           │                     │                     │             │
│           └─────────────────────┼─────────────────────┘             │
│                                 │                                   │
│                                 ▼                                   │
│                      ┌──────────────────┐                           │
│                      │ TrackEventHandler│                           │
│                      │ (이벤트 처리)     │                           │
│                      └────────┬─────────┘                           │
│                               │                                     │
│                               ▼                                     │
│                      ┌──────────────────┐                           │
│                      │      Track       │                           │
│                      │  (상태 업데이트)  │                           │
│                      └──────────────────┘                           │
└──────────────────────────────────────────────────────────────────────┘
```

### 수신 이벤트 (Consumer)

#### Order Service → Track Service

| Topic | Event | 설명 | 처리 |
|-------|-------|------|------|
| `tracking-start-requested` | `TrackingStartRequestedEvent` | 추적 시작 요청 | Track 생성 |

```json
// TrackingStartRequestedEvent 예시
{
  "eventId": "uuid",
  "eventType": "TRACKING_START_REQUESTED",
  "source": "order-service",
  "orderId": "order-uuid",
  "orderNumber": "ORD-2025-001234",
  "hubDeliveryId": "hub-delivery-uuid",
  "lastMileDeliveryId": "last-mile-uuid",
  "originHubId": "hub-origin",
  "destinationHubId": "hub-destination",
  "routingHub": "hub1,hub2,hub3",
  "requiresHubDelivery": true,
  "estimatedDeliveryTime": "2025-01-15T18:00:00",
  "requestedAt": "2025-01-15T10:00:00"
}
```

#### Hub Delivery Service → Track Service

| Topic | Event | 설명 | 처리 |
|-------|-------|------|------|
| `hub-segment-departed` | `HubSegmentDepartedEvent` | 허브 구간 출발 | `Track.departHubSegment()` |
| `hub-segment-arrived` | `HubSegmentArrivedEvent` | 허브 구간 도착 | `Track.arriveHubSegment()` |

```json
// HubSegmentDepartedEvent 예시
{
  "eventId": "uuid",
  "eventType": "HUB_SEGMENT_DEPARTED",
  "source": "hub-delivery-service",
  "orderId": "order-uuid",
  "hubDeliveryId": "hub-delivery-uuid",
  "segmentIndex": 0,
  "fromHubId": "hub-1",
  "toHubId": "hub-2",
  "departedAt": "2025-01-15T10:35:00"
}
```

```json
// HubSegmentArrivedEvent 예시
{
  "eventId": "uuid",
  "eventType": "HUB_SEGMENT_ARRIVED",
  "source": "hub-delivery-service",
  "orderId": "order-uuid",
  "hubDeliveryId": "hub-delivery-uuid",
  "segmentIndex": 0,
  "hubId": "hub-2",
  "arrivedAt": "2025-01-15T12:00:00"
}
```

#### Last Mile Service → Track Service

| Topic | Event | 설명 | 처리 |
|-------|-------|------|------|
| `last-mile-departed` | `LastMileDepartedEvent` | 최종 배송 출발 | `Track.departLastMile()` |
| `last-mile-completed` | `LastMileCompletedEvent` | 배송 완료 | `Track.complete()` |

```json
// LastMileDepartedEvent 예시
{
  "eventId": "uuid",
  "eventType": "LAST_MILE_DEPARTED",
  "source": "last-mile-service",
  "orderId": "order-uuid",
  "lastMileDeliveryId": "last-mile-uuid",
  "hubId": "hub-destination",
  "departedAt": "2025-01-15T15:00:00"
}
```

```json
// LastMileCompletedEvent 예시
{
  "eventId": "uuid",
  "eventType": "LAST_MILE_COMPLETED",
  "source": "last-mile-service",
  "orderId": "order-uuid",
  "lastMileDeliveryId": "last-mile-uuid",
  "completedAt": "2025-01-15T17:30:00",
  "receiverName": "홍길동",
  "signature": "base64-signature"
}
```

### Kafka 설정

```yaml
spring:
  kafka:
    topic:
      # 수신 토픽 (Order Service)
      tracking-start-requested: tracking-start-requested
      # 수신 토픽 (Hub Delivery Service)
      hub-segment-departed: hub-segment-departed
      hub-segment-arrived: hub-segment-arrived
      # 수신 토픽 (Last Mile Service)
      last-mile-departed: last-mile-departed
      last-mile-completed: last-mile-completed
    consumer:
      group-id: track-service-group
      enable-auto-commit: false  # 수동 ACK
```

## 🔗 서비스 간 통신 (Feign Client)

### Hub Delivery Service

```java
@FeignClient(name = "hub-delivery-service")
public interface HubDeliveryClient {

    @PostMapping("/v1/hub-delivery/internal/deliveries/{hubDeliveryId}/segments/{segmentIndex}/assign-driver")
    AssignDriverForSegmentResponse assignDriverForSegment(
            @PathVariable String hubDeliveryId,
            @PathVariable Integer segmentIndex
    );
}
```

```json
// AssignDriverForSegmentResponse
{
  "hubDeliveryId": "hub-delivery-uuid",
  "segmentIndex": 0,
  "driverId": "driver-uuid",
  "driverName": "김배송",
  "status": "ASSIGNED",
  "success": true,
  "message": "드라이버 배정 완료"
}
```

### Last Mile Delivery Service

```java
@FeignClient(name = "last-mile-delivery-service")
public interface LastMileDeliveryClient {

    @PostMapping("/v1/last-mile-delivery/internal/deliveries/{lastMileDeliveryId}/assign-driver")
    AssignDriverResponse assignDriver(
            @PathVariable String lastMileDeliveryId
    );
}
```

```json
// AssignDriverResponse
{
  "lastMileDeliveryId": "last-mile-uuid",
  "driverId": "driver-uuid",
  "driverName": "이배달",
  "status": "ASSIGNED",
  "success": true,
  "message": "드라이버 배정 완료"
}
```

## 🔐 보안

- OAuth 2.0 Resource Server (Keycloak 연동)
- 역할별 API 분리 (Master / Hub Manager / Company User)
- `X-User-Id`, `X-Hub-Id` 헤더를 통한 사용자/허브 식별

## 📈 모니터링

| 도구 | 용도 | 엔드포인트 |
|------|------|-----------|
| **Actuator** | 헬스체크/메트릭 | `/actuator/health`, `/actuator/prometheus` |
| **Zipkin** | 분산 추적 | Push to Zipkin Server |
| **Loki** | 로그 수집 | Push via Logback Appender |
| **Prometheus** | 메트릭 수집 | Push to Pushgateway |

## 📁 프로젝트 구조

```
src/main/java/com/early_express/track_service/
├── domain/track/
│   ├── application/
│   │   ├── event/
│   │   │   └── TrackEventHandler.java
│   │   ├── query/
│   │   │   ├── TrackQueryService.java
│   │   │   └── dto/
│   │   │       └── TrackQueryDto.java
│   │   └── service/
│   │       └── TrackService.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Track.java (Aggregate Root)
│   │   │   ├── TrackEvent.java
│   │   │   └── vo/
│   │   │       ├── TrackId.java
│   │   │       ├── TrackStatus.java
│   │   │       ├── TrackPhase.java
│   │   │       ├── TrackEventType.java
│   │   │       ├── HubSegmentInfo.java
│   │   │       └── DeliveryIds.java
│   │   ├── exception/
│   │   └── repository/
│   ├── infrastructure/
│   │   ├── client/
│   │   │   ├── hub_delivery/
│   │   │   │   ├── HubDeliveryClient.java
│   │   │   │   ├── HubDeliveryClientConfig.java
│   │   │   │   ├── HubDeliveryErrorDecoder.java
│   │   │   │   └── dto/
│   │   │   │       └── AssignDriverForSegmentResponse.java
│   │   │   └── last_mile_delivery/
│   │   │       ├── LastMileDeliveryClient.java
│   │   │       ├── LastMileDeliveryClientConfig.java
│   │   │       ├── LastMileDeliveryErrorDecoder.java
│   │   │       └── dto/
│   │   │           └── AssignDriverResponse.java
│   │   ├── messaging/
│   │   │   ├── order/
│   │   │   │   ├── consumer/
│   │   │   │   │   └── OrderEventConsumer.java
│   │   │   │   └── event/
│   │   │   │       └── TrackingStartRequestedEvent.java
│   │   │   ├── hubdelivery/
│   │   │   │   ├── consumer/
│   │   │   │   │   └── HubDeliveryEventConsumer.java
│   │   │   │   └── event/
│   │   │   │       ├── HubSegmentDepartedEvent.java
│   │   │   │       └── HubSegmentArrivedEvent.java
│   │   │   └── lastmile/
│   │   │       ├── consumer/
│   │   │       │   └── LastMileEventConsumer.java
│   │   │       └── event/
│   │   │           ├── LastMileDepartedEvent.java
│   │   │           └── LastMileCompletedEvent.java
│   │   └── persistence/
│   │       └── entity/
│   │           ├── TrackEntity.java
│   │           └── TrackEventEntity.java
│   └── presentation/
│       └── web/
│           ├── master/
│           │   ├── TrackMasterController.java
│           │   └── dto/response/
│           │       └── MasterTrackDetailResponse.java
│           ├── hubmanager/
│           │   ├── TrackHubManagerController.java
│           │   └── dto/response/
│           │       └── HubManagerTrackDetailResponse.java
│           ├── companyuser/
│           │   ├── TrackCompanyUserController.java
│           │   └── dto/response/
│           │       └── CompanyUserTrackDetailResponse.java
│           └── common/
│               └── dto/response/
│                   ├── TrackSimpleResponse.java
│                   └── TrackEventSimpleResponse.java
└── global/
    ├── common/
    ├── config/
    ├── infrastructure/
    │   └── event/base/
    │       └── BaseEvent.java
    └── presentation/
        └── dto/
            ├── ApiResponse.java
            └── PageResponse.java
```