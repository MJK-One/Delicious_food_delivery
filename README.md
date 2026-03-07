## 맛집배달하죠
### 프로젝트 목적 및 상세
**🛵 Delicious Food Delivery**
"광화문 지역 밀착형 서비스를 시작으로, 확장 가능하고 견고한 백엔드 시스템을 구축합니다."

**핵심 가치**
**1. 신뢰할 수 있는 보안 및 유저 관리**<br>
권한 분리: JWT 기반 인증과 Role-Based Access Control를 적용하여 고객, 사장님, 관리자의 권한을 엄격히 분리합니다.<br>
데이터 추적: 사용자 데이터 변경 이력을 완벽히 추적하여 보안성과 투명성을 확보합니다.<br>

**2. 지능형 가게 및 메뉴 관리**<br>
고성능 검색: QueryDSL을 활용해 복잡한 조건의 필터링과 페이징 처리를 최적화하여 사용자 경험을 개선합니다.<br>
AI 운영 효율화: LLM(AI) 연동을 통해 사장님이 상품 등록 시 프롬프트 기반으로 자동 설명을 생성하여 운영 번거로움을 최소화합니다.<br>

**3. 정교한 주문 및 결제 워크플로우**<br>
지역 기반 배달 검증: '구(District)' 단위의 지역 일치 여부를 사전에 검증하여, 가게와 사용자의 지역이 일치하지 않을 경우 주문을 제한하는 실무적 로직을 구현합니다.<br>
상태 관리: 주문 생성부터 5분 이내 취소 제한 타이머 등 실무적인 비즈니스 로직을 구현합니다.<br>

**4. 표준화된 시스템 인프라**<br>
협업 최적화: 공통 응답 규격(Response Wrapper)과 글로벌 예외 처리 전략을 수립하여 일관된 API 환경을 제공합니다.<br>
본 프로젝트는 실질적인 배달 서비스의 전체 라이프사이클을 백엔드 관점에서 밀도 있게 구현하는 것을 목표로 합니다. 단순한 기능 구현을 넘어, 대규모 협업과 서비스 확장을 고려한 표준화된 아키텍처를 지향합니다.

### 팀원 역할분담
| 성명 | 역할 | 주요 업무 및 담당 범위 |
| ---------- | ------------ | ------------ |
| 성명규 | Common Infra & Security | 공통 응답/예외 처리, Logback 전략 수립, CI/CD 및 Swagger/RestDocs 가이드라인 구축 |
| 김민준 | User & Auth | JWT/Security 기반 인증·인가, RBAC(권한 제어), 회원 관리 및 Redis 권한 검증 구현 |
| 임진건 | Store & Product | 가게/상품 CRUD, QueryDSL 기반 고도화된 검색 및 페이징 처리, 카테고리 구조 설계 |
| 송유진 | Order System | 주문 생성 및 상태 관리, 5분 이내 취소 제한 타이머 로직, 도메인 간 데이터 흐름 설계 |
| 박원준 | Payment & Review | 가상 결제 시스템, 리뷰 평점 로직 구현 및 Fetch Join/Batch Size를 통한 성능 최적화 |
| 이상윤 | AI Engineer | AI 프롬프트 엔지니어링(상품 설명 생성), AI 이력 관리 및 MCP(Model Context Protocol) 연구 |

### 서비스 구성 및 실행 방법 
본 프로젝트는 서비스 운영에 필요한 인프라(DB, Cache)를 Docker Compose를 통해 컨테이너화하여 관리합니다.

#### 시스템 아키텍처 (Infrastructure)
- Database: PostgreSQL 
- Cache & Auth: Redis 
- Application: Spring Boot

**1. 레포지토리 클론**
```Bash
git clone https://github.com/your-repo/delicious-food-delivery.git
cd delicious-food-delivery
```
**2. 인프라 컨테이너 실행 (Docker)**
PostgreSQL과 Redis를 백그라운드에서 실행합니다.
```Bash
docker-compose up -d
```
**3. 애플리케이션 실행**
프로젝트 루트 디렉토리에서 빌드 후 실행합니다.
```Bash
./gradlew bootRun
```

**⚙ 환경 설정(Environment Variables)**
| 서비스 | 포트(Port) | 비고 |
| ---------- | ------------ | ------------ |
| Spring Boot | 8080 | API Main Server |
| PostgreSQL | 5432 | Main Database |
| Redis | 6379 | Auth & Cache Server |
| Swagger | 8080/swagger-ui/index.html | API Documentation |

### 기술 스택

### ERD
<img width="2826" height="2500" alt="ERD 다이어그램" src="https://github.com/user-attachments/assets/f891c672-6dd6-4732-9bf8-ee0fb7d0c4ce" />

### API 명세서
[API 명세서 바로가기](https://www.notion.so/teamsparta/API-3122dc3ef51480138898fae4e4c8a34f?source=copy_link)
