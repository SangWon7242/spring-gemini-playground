package com.backend.domain.recipe.dto;

import java.util.List;

/**
 * 다중 레시피 + 유튜브 영상 응답 래퍼 DTO
 *
 * @param recipes 추천 레시피 및 관련 유튜브 영상 목록
 * @param message AI의 추가 메시지
 */
public record RecipeListWithYoutubeResponse(
    List<RecipeWithYoutubeResponse> recipes,
    String message) {
}
