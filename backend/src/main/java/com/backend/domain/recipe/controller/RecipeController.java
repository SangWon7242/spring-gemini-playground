package com.backend.domain.recipe.controller;

import com.backend.domain.recipe.dto.RecipeListResponse;
import com.backend.domain.recipe.dto.RecipeListWithYoutubeResponse;
import com.backend.domain.recipe.dto.RecipeRequest;
import com.backend.domain.recipe.dto.YoutubeVideoResponse;
import com.backend.domain.recipe.entity.Recipe;
import com.backend.domain.recipe.service.RecipeService;
import com.backend.domain.recipe.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 레시피 추천 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

  private final RecipeService recipeService;
  private final YoutubeService youtubeService;

  /**
   * 이미지 기반 레시피 추천 API
   *
   * @param image             식재료 이미지 (필수)
   * @param additionalRequest 추가 요청사항 (선택)
   * @return 추천 레시피 목록
   */
  @PostMapping(value = "/recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<RecipeListResponse> recommendRecipes(
      @RequestPart("image") MultipartFile image,
      @RequestPart(value = "additionalRequest", required = false) String additionalRequest) {
    log.info("레시피 추천 요청 - 이미지: {}, 추가요청: {}",
        image.getOriginalFilename(), additionalRequest);

    // 이미지 유효성 검사
    if (image.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    RecipeRequest request = new RecipeRequest(additionalRequest);
    RecipeListResponse response = recipeService.recommendRecipes(image, request);

    return ResponseEntity.ok(response);
  }

  /**
   * 이미지 기반 레시피 추천 + 유튜브 영상 연동 API
   *
   * @param image             식재료 이미지 (필수)
   * @param additionalRequest 추가 요청사항 (선택)
   * @return 추천 레시피 및 관련 유튜브 영상 목록
   */
  @PostMapping(value = "/recommend-with-youtube", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<RecipeListWithYoutubeResponse> recommendRecipesWithYoutube(
      @RequestPart("image") MultipartFile image,
      @RequestPart(value = "additionalRequest", required = false) String additionalRequest) {
    log.info("레시피 + 유튜브 추천 요청 - 이미지: {}, 추가요청: {}",
        image.getOriginalFilename(), additionalRequest);

    // 이미지 유효성 검사
    if (image.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }

    RecipeRequest request = new RecipeRequest(additionalRequest);
    RecipeListWithYoutubeResponse response = recipeService.recommendRecipesWithYoutube(image, request);

    return ResponseEntity.ok(response);
  }

  /**
   * 저장된 레시피 목록 조회 API (향후 확장용)
   *
   * @return 저장된 레시피 목록
   */
  @GetMapping
  public ResponseEntity<List<Recipe>> getAllRecipes() {
    List<Recipe> recipes = recipeService.getAllRecipes();
    return ResponseEntity.ok(recipes);
  }

  /**
   * Health Check API
   */
  @GetMapping("/health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok("Snap Cook AI is running!");
  }

  /**
   * YouTube 검색 테스트 API (Gemini 없이 YouTube만 테스트)
   *
   * @param recipeName 검색할 레시피 이름
   * @return 관련 유튜브 영상 목록
   */
  @GetMapping("/youtube-test")
  public ResponseEntity<List<YoutubeVideoResponse>> testYoutubeSearch(
      @RequestParam("recipeName") String recipeName) {
    log.info("YouTube 검색 테스트 - 레시피: {}", recipeName);
    List<YoutubeVideoResponse> videos = youtubeService.searchRecipeVideos(recipeName, 3);
    return ResponseEntity.ok(videos);
  }
}
