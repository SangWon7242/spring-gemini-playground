package com.backend.domain.recipe.dto;

import java.util.List;

/**
 * 레시피와 관련 유튜브 영상을 함께 담는 DTO
 *
 * @param recipe        AI가 추천한 레시피 정보
 * @param youtubeVideos 관련 유튜브 영상 목록 (조회수 순 1~3개)
 */
public record RecipeWithYoutubeResponse(
    RecipeResponse recipe,
    List<YoutubeVideoResponse> youtubeVideos) {
}
