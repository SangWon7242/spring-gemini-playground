# Role

너는 Spring AI와 Google Gemini를 활용한 백엔드 개발 테크 리드야.

# Task

현재 진행 중인 '식재료 이미지 기반 레시피 추천 프로젝트(Project Name: Snap Cook AI)'의 개발 가이드 문서인 `GEMINI.md` 파일을 루트 디렉토리에 생성하려고 해.
아래 내용을 포함하여 마크다운(Markdown) 형식으로 전문적이고 깔끔하게 작성해줘.

# Contents

1. **프로젝트 개요**: Spring AI와 Google Gemini(Vision)를 이용한 멀티모달 레시피 추천 서비스임을 명시.
2. **기술 스택**: Java 21+, Spring Boot 3.x, Spring AI, Google Gemini 3 Flash, H2, JPA.
3. **환경 설정(Setup)**: API Key 발급처 링크와 `application.yml` 또는 `application.yaml` 설정 방법 (보안 주의사항 포함).

- 보안과 관련된 API KEY는 `application-secret.yaml` 통해서 관리가 되게끔 설정

4. **프롬프트 엔지니어링(Prompt Engineering)**:

- System Prompt: 셰프 페르소나 설정 내용.
- User Prompt: 이미지와 텍스트 결합 방식.

5. **데이터 구조(Data Structure)**: AI가 응답해야 하는 JSON 구조와 매핑되는 Java `record` (DTO) 명세.
6. **API 명세**: 요청(Request) 파라미터와 응답(Response) 예시.
7. **향후 계획(Roadmap)**: RAG 도입, 데이터베이스 저장 등 확장 계획.

# Tone & Manner

개발자가 보고 바로 구현할 수 있도록 명확하고 구체적인 코드를 포함할 것.
