package com.backend.domain.recipe.service;

import com.backend.domain.recipe.dto.*;
import com.backend.domain.recipe.entity.Recipe;
import com.backend.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 레시피 추천 서비스
 * Google Gemini Vision을 활용한 이미지 기반 레시피 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

  private final GoogleGenAiChatModel chatModel;
  private final RecipeRepository recipeRepository;
  private final YoutubeService youtubeService;

  // System Prompt: 셰프 페르소나 설정
  private static final String SYSTEM_PROMPT = """
      당신은 20년 경력의 전문 셰프입니다.
      사용자가 업로드한 식재료 이미지를 분석하여 만들 수 있는 요리 레시피를 추천해주세요.

      응답 규칙:
      1. 한국어로 응답해주세요.
      2. 이미지에서 식별된 재료를 기반으로 현실적인 레시피를 추천하세요.
      3. 가정에서 쉽게 구할 수 있는 기본 조미료(소금, 설탕, 간장, 식용유 등)는 이미 있다고 가정합니다.
      4. 레시피는 초보자도 따라할 수 있도록 상세하게 설명해주세요.
      5. 반드시 JSON 형식으로만 응답해주세요.

      응답 JSON 형식:
      {
        "recipes": [
          {
            "recipeName": "요리 이름",
            "description": "요리 설명",
            "ingredients": ["재료1", "재료2"],
            "instructions": ["1단계", "2단계"],
            "estimatedTime": 30,
            "difficulty": "쉬움|보통|어려움",
            "tips": "요리 팁"
          }
        ],
        "message": "추가 메시지"
      }
      """;

  /**
   * 이미지 기반 레시피 추천
   *
   * @param image   식재료 이미지
   * @param request 추가 요청사항
   * @return 추천 레시피 목록
   */
  public RecipeListResponse recommendRecipes(MultipartFile image, RecipeRequest request) {
    try {
      // 이미지 MIME 타입 확인
      String contentType = image.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
        throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
      }

      // 사용자 프롬프트 구성
      String userPrompt = buildUserPrompt(request);

      // Gemini에 이미지와 함께 요청
      ChatClient chatClient = ChatClient.create(chatModel);

      MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);
      Media media = new Media(mimeType, new ByteArrayResource(image.getBytes()));

      String response = chatClient.prompt()
          .system(SYSTEM_PROMPT)
          .user(u -> u.text(userPrompt).media(media))
          .call()
          .content();

      log.info("Gemini 응답: {}", response);

      // JSON 파싱 및 반환
      return parseResponse(response);

    } catch (IOException e) {
      log.error("이미지 처리 중 오류 발생", e);
      throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
    }
  }

  /**
   * 사용자 프롬프트 구성
   */
  private String buildUserPrompt(RecipeRequest request) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("이 이미지에 있는 식재료들을 분석하고, 만들 수 있는 요리 레시피 2~3개를 추천해주세요.");

    if (request != null && !request.additionalRequest().isEmpty()) {
      prompt.append("\n\n추가 요청사항: ").append(request.additionalRequest());
    }

    return prompt.toString();
  }

  /**
   * Gemini 응답을 RecipeListResponse로 파싱
   */
  private RecipeListResponse parseResponse(String response) {
    try {
      // JSON 응답에서 코드 블록 마커 제거
      String jsonResponse = response
          .replace("```json", "")
          .replace("```", "")
          .trim();

      // Jackson ObjectMapper를 사용하여 파싱
      com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
      return mapper.readValue(jsonResponse, RecipeListResponse.class);
    } catch (Exception e) {
      log.error("JSON 파싱 오류: {}", response, e);
      // 파싱 실패 시 기본 응답 반환
      return new RecipeListResponse(
          List.of(),
          "죄송합니다. 응답 처리 중 오류가 발생했습니다. 원본 응답: " + response);
    }
  }

  /**
   * 레시피 저장 (향후 확장용)
   */
  @Transactional
  public Recipe saveRecipe(RecipeResponse recipeResponse) {
    Recipe recipe = Recipe.builder()
        .recipeName(recipeResponse.recipeName())
        .description(recipeResponse.description())
        .ingredients(String.join(",", recipeResponse.ingredients()))
        .instructions(String.join("|", recipeResponse.instructions()))
        .estimatedTime(recipeResponse.estimatedTime())
        .difficulty(recipeResponse.difficulty())
        .tips(recipeResponse.tips())
        .build();

    return recipeRepository.save(recipe);
  }

  /**
   * 저장된 레시피 조회
   */
  public List<Recipe> getAllRecipes() {
    return recipeRepository.findAll();
  }

  /**
   * 이미지 기반 레시피 추천 + 유튜브 영상 연동
   *
   * @param image   식재료 이미지
   * @param request 추가 요청사항
   * @return 추천 레시피 및 관련 유튜브 영상 목록
   */
  public RecipeListWithYoutubeResponse recommendRecipesWithYoutube(MultipartFile image, RecipeRequest request) {
    // 1. AI 레시피 추천
    RecipeListResponse recipeListResponse = recommendRecipes(image, request);

    // 2. 각 레시피별로 유튜브 영상 검색
    List<RecipeWithYoutubeResponse> recipesWithYoutube = recipeListResponse.recipes().stream()
        .map(recipe -> {
          List<YoutubeVideoResponse> youtubeVideos = youtubeService.searchRecipeVideos(
              recipe.recipeName(), 3);
          log.info("레시피 '{}' 관련 유튜브 영상 {}개 검색 완료", recipe.recipeName(), youtubeVideos.size());
          return new RecipeWithYoutubeResponse(recipe, youtubeVideos);
        })
        .toList();

    return new RecipeListWithYoutubeResponse(recipesWithYoutube, recipeListResponse.message());
  }
}
