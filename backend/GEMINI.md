# 🍳 Snap Cook AI - 개발 가이드

> Spring AI와 Google Gemini(Vision)를 이용한 **멀티모달 레시피 추천 서비스**

---

## 1. 프로젝트 개요

**Snap Cook AI**는 사용자가 업로드한 식재료 이미지를 Google Gemini Vision AI가 분석하여, 해당 재료로 만들 수 있는 요리 레시피를 추천해주는 서비스입니다.

### 주요 기능

- 📸 **이미지 기반 식재료 인식**: 사진 한 장으로 재료 자동 분석
- 🍽️ **맞춤형 레시피 추천**: 인식된 재료 기반 2~3개 레시피 제공
- 💾 **레시피 저장**: 추천받은 레시피를 데이터베이스에 저장 (향후 확장)

---

## 2. 기술 스택

| 구분               | 기술                               |
| ------------------ | ---------------------------------- |
| **Language**       | Java 21+                           |
| **Framework**      | Spring Boot 3.x                    |
| **AI Integration** | Spring AI 1.1.2                    |
| **AI Model**       | Google Gemini 3 Flash              |
| **Database**       | H2 (개발), MySQL/PostgreSQL (운영) |
| **ORM**            | Spring Data JPA                    |
| **Build Tool**     | Gradle (Kotlin DSL)                |

---

## 3. 환경 설정 (Setup)

### 3.1 Google AI API Key 발급

1. [Google AI Studio](https://aistudio.google.com/apikey)에 접속
2. Google 계정으로 로그인
3. **"Get API Key"** 버튼 클릭하여 API Key 발급
4. 발급받은 API Key를 안전하게 보관

### 3.2 application.yaml 설정

```yaml
# src/main/resources/application.yaml
server:
  port: 8080

spring:
  application:
    name: backend
  profiles:
    include: secret # application-secret.yaml 포함
  datasource:
    url: jdbc:h2:./db_dev;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
    hikari:
      auto-commit: false
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_batch_fetch_size: 100
        format_sql: true
        highlight_sql: true
        use_sql_comments: true
  ai:
    google:
      genai:
        chat:
          options:
            model: Gemini-3-Flash
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### 3.3 application-secret.yaml 설정 (⚠️ 보안 주의)

```yaml
# src/main/resources/application-secret.yaml
# ⚠️ 이 파일은 절대 Git에 커밋하지 마세요!
# .gitignore에 반드시 추가해야 합니다.

spring:
  ai:
    google:
      genai:
        api-key: YOUR_API_KEY_HERE
```

### 3.4 .gitignore 설정

```gitignore
# Secret 설정 파일
application-secret.yaml
application-secret.yml
```

---

## 4. 프롬프트 엔지니어링 (Prompt Engineering)

### 4.1 System Prompt - 셰프 페르소나

```text
당신은 20년 경력의 전문 셰프입니다.
사용자가 업로드한 식재료 이미지를 분석하여 만들 수 있는 요리 레시피를 추천해주세요.

응답 규칙:
1. 한국어로 응답해주세요.
2. 이미지에서 식별된 재료를 기반으로 현실적인 레시피를 추천하세요.
3. 가정에서 쉽게 구할 수 있는 기본 조미료(소금, 설탕, 간장, 식용유 등)는 이미 있다고 가정합니다.
4. 레시피는 초보자도 따라할 수 있도록 상세하게 설명해주세요.
5. 반드시 JSON 형식으로만 응답해주세요.
```

### 4.2 User Prompt - 이미지 + 텍스트 결합

```java
// 이미지와 함께 전송되는 User Prompt
String userPrompt = "이 이미지에 있는 식재료들을 분석하고, 만들 수 있는 요리 레시피 2~3개를 추천해주세요.";

// 추가 요청사항이 있는 경우
if (additionalRequest != null) {
    userPrompt += "\n\n추가 요청사항: " + additionalRequest;
}
```

---

## 5. 데이터 구조 (Data Structure)

### 5.1 AI 응답 JSON 구조

```json
{
  "recipes": [
    {
      "recipeName": "토마토 파스타",
      "description": "신선한 토마토와 마늘로 만드는 간단한 파스타",
      "ingredients": [
        "토마토 3개",
        "파스타면 200g",
        "마늘 4쪽",
        "올리브오일 3큰술",
        "소금 약간",
        "후추 약간"
      ],
      "instructions": [
        "파스타면을 끓는 물에 8분간 삶아주세요.",
        "토마토를 깍둑썰기 해주세요.",
        "팬에 올리브오일을 두르고 마늘을 볶아주세요.",
        "토마토를 넣고 5분간 졸여주세요.",
        "삶은 파스타면을 넣고 버무려주세요."
      ],
      "estimatedTime": 20,
      "difficulty": "쉬움",
      "tips": "파스타 삶은 물을 조금 남겨두면 소스가 더 부드럽게 됩니다."
    }
  ],
  "message": "신선한 재료들로 맛있는 요리를 만들어보세요!"
}
```

### 5.2 Java Record (DTO) 명세

```java
// RecipeRequest.java - 요청 DTO
public record RecipeRequest(
    String additionalRequest  // 추가 요청사항 (선택)
) {}

// RecipeResponse.java - 개별 레시피 DTO
public record RecipeResponse(
    String recipeName,        // 요리 이름
    String description,       // 요리 설명
    List<String> ingredients, // 필요한 재료 목록
    List<String> instructions,// 조리 단계
    int estimatedTime,        // 예상 조리 시간 (분)
    String difficulty,        // 난이도 (쉬움/보통/어려움)
    String tips               // 요리 팁
) {}

// RecipeListResponse.java - 응답 래퍼 DTO
public record RecipeListResponse(
    List<RecipeResponse> recipes,  // 추천 레시피 목록
    String message                 // AI의 추가 메시지
) {}
```

---

## 6. API 명세

### 6.1 레시피 추천 API

#### Request

```http
POST /api/recipes/recommend
Content-Type: multipart/form-data
```

| 파라미터            | 타입 | 필수 | 설명                                          |
| ------------------- | ---- | ---- | --------------------------------------------- |
| `image`             | file | ✅   | 식재료 이미지 (jpg, png, webp)                |
| `additionalRequest` | text | ❌   | 추가 요청사항 (예: "매운 요리로", "채식으로") |

#### Response (200 OK)

```json
{
  "recipes": [
    {
      "recipeName": "계란 볶음밥",
      "description": "간단하고 빠르게 만들 수 있는 한그릇 요리",
      "ingredients": ["계란 2개", "밥 1공기", "대파 1대", "간장 1큰술"],
      "instructions": [
        "계란을 풀어 스크램블하세요.",
        "대파를 송송 썰어주세요.",
        "밥과 함께 볶아주세요.",
        "간장으로 간을 맞추세요."
      ],
      "estimatedTime": 10,
      "difficulty": "쉬움",
      "tips": "차가운 밥을 사용하면 더 맛있어요!"
    }
  ],
  "message": "맛있는 요리 되세요! 🍳"
}
```

### 6.2 저장된 레시피 조회 API

```http
GET /api/recipes
```

#### Response (200 OK)

```json
[
  {
    "id": 1,
    "recipeName": "계란 볶음밥",
    "description": "간단하고 빠르게 만들 수 있는 한그릇 요리",
    "ingredients": "계란 2개,밥 1공기,대파 1대",
    "instructions": "계란을 풀어 스크램블하세요.|밥과 함께 볶아주세요.",
    "estimatedTime": 10,
    "difficulty": "쉬움",
    "tips": "차가운 밥을 사용하면 더 맛있어요!",
    "createdAt": "2026-01-31T10:30:00"
  }
]
```

### 6.3 Health Check API

```http
GET /api/recipes/health
```

#### Response (200 OK)

```text
Snap Cook AI is running!
```

---

## 7. 프로젝트 구조

```
src/main/java/com/backend/
├── BackendApplication.java
└── domain/
    └── recipe/
        ├── controller/
        │   └── RecipeController.java
        ├── service/
        │   └── RecipeService.java
        ├── repository/
        │   └── RecipeRepository.java
        ├── entity/
        │   └── Recipe.java
        └── dto/
            ├── RecipeRequest.java
            ├── RecipeResponse.java
            └── RecipeListResponse.java
```

---

## 8. 향후 계획 (Roadmap)

### Phase 2: 기능 고도화

- [ ] **RAG (Retrieval-Augmented Generation) 도입**
  - 레시피 데이터베이스와 연동하여 더 정확한 추천
  - 벡터 DB (Pinecone, Weaviate) 활용

### Phase 3: 사용자 경험 개선

- [ ] **사용자 인증/인가**
  - 레시피 즐겨찾기 기능
  - 개인화된 추천
- [ ] **레시피 히스토리 저장**
  - 추천받은 레시피 자동 저장
  - 검색 및 필터링 기능

### Phase 4: 확장

- [ ] **운영 DB 전환**
  - H2 → MySQL/PostgreSQL
- [ ] **클라우드 배포**
  - Docker 컨테이너화
  - AWS/GCP 배포
- [ ] **프론트엔드 개발**
  - React/Next.js 기반 웹 UI

---

## 9. 실행 방법

```bash
# 프로젝트 루트에서 실행
./gradlew bootRun

# 또는 Windows
gradlew.bat bootRun
```

### API 테스트 (cURL)

```bash
curl -X POST http://localhost:8080/api/recipes/recommend \
  -F "image=@/path/to/your/ingredients.jpg" \
  -F "additionalRequest=매운 음식으로 추천해주세요"
```

---

## 📝 License

MIT License

## 👨‍💻 Author

Snap Cook AI Team
