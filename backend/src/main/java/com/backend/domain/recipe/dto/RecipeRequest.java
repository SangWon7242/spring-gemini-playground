package com.backend.domain.recipe.dto;

/**
 * 레시피 추천 요청 DTO
 *
 * @param additionalRequest 추가 요청사항 (선호 요리 스타일, 제외 재료 등)
 */
public record RecipeRequest(
    String additionalRequest) {
  public RecipeRequest {
    if (additionalRequest == null) {
      additionalRequest = "";
    }
  }
}
