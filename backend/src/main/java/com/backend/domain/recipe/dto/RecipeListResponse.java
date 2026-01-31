package com.backend.domain.recipe.dto;

import java.util.List;

/**
 * 다중 레시피 응답을 감싸는 래퍼 DTO
 *
 * @param recipes 추천된 레시피 목록
 * @param message AI의 추가 메시지
 */
public record RecipeListResponse(
    List<RecipeResponse> recipes,
    String message) {
}
