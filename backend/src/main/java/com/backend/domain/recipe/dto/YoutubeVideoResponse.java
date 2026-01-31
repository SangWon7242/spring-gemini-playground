package com.backend.domain.recipe.dto;

/**
 * 유튜브 영상 정보 DTO
 *
 * @param videoId      유튜브 영상 ID
 * @param title        영상 제목
 * @param description  영상 설명
 * @param thumbnailUrl 썸네일 이미지 URL
 * @param channelTitle 채널 이름
 * @param viewCount    조회수
 * @param videoUrl     영상 URL
 */
public record YoutubeVideoResponse(
    String videoId,
    String title,
    String description,
    String thumbnailUrl,
    String channelTitle,
    Long viewCount,
    String videoUrl) {
  /**
   * 영상 ID로 전체 URL 생성
   */
  public static String buildVideoUrl(String videoId) {
    return "https://www.youtube.com/watch?v=" + videoId;
  }
}
