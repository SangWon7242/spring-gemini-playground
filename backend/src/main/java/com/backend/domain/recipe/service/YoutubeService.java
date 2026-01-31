package com.backend.domain.recipe.service;

import com.backend.domain.recipe.dto.YoutubeVideoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * YouTube Data API를 활용한 영상 검색 서비스
 */
@Slf4j
@Service
public class YoutubeService {

  @Value("${youtube.api.key:}")
  private String youtubeApiKey;

  private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
  private static final String YOUTUBE_VIDEOS_URL = "https://www.googleapis.com/youtube/v3/videos";
  private static final int MAX_RESULTS = 10; // 검색 결과 최대 개수 (조회수 정렬 후 상위 3개 선택)

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 레시피 이름으로 유튜브 영상 검색 (조회수 순 상위 1~3개)
   *
   * @param recipeName 검색할 레시피 이름
   * @param maxCount   반환할 최대 영상 개수 (1~3)
   * @return 조회수 순으로 정렬된 유튜브 영상 목록
   */
  public List<YoutubeVideoResponse> searchRecipeVideos(String recipeName, int maxCount) {
    if (youtubeApiKey == null || youtubeApiKey.isEmpty()) {
      log.warn("YouTube API Key가 설정되지 않았습니다.");
      return List.of();
    }

    try {
      // 1. 검색 API로 영상 ID 목록 가져오기
      List<String> videoIds = searchVideoIds(recipeName + " 레시피");

      if (videoIds.isEmpty()) {
        log.info("'{}' 관련 유튜브 영상을 찾을 수 없습니다.", recipeName);
        return List.of();
      }

      // 2. 영상 상세 정보 (조회수 포함) 가져오기
      List<YoutubeVideoResponse> videos = getVideoDetails(videoIds);

      // 3. 조회수 기준 내림차순 정렬 후 상위 N개 반환
      return videos.stream()
          .sorted(Comparator.comparingLong(YoutubeVideoResponse::viewCount).reversed())
          .limit(Math.min(maxCount, 3))
          .toList();

    } catch (Exception e) {
      log.error("유튜브 영상 검색 중 오류 발생: {}", e.getMessage(), e);
      return List.of();
    }
  }

  /**
   * YouTube Search API로 영상 ID 목록 검색
   */
  private List<String> searchVideoIds(String query) {
    String url = UriComponentsBuilder.fromHttpUrl(YOUTUBE_SEARCH_URL)
        .queryParam("part", "snippet")
        .queryParam("q", query)
        .queryParam("type", "video")
        .queryParam("maxResults", MAX_RESULTS)
        .queryParam("regionCode", "KR")
        .queryParam("relevanceLanguage", "ko")
        .queryParam("key", youtubeApiKey)
        .build()
        .toUriString();

    try {
      String response = restTemplate.getForObject(url, String.class);
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("items");

      List<String> videoIds = new ArrayList<>();
      for (JsonNode item : items) {
        String videoId = item.path("id").path("videoId").asText();
        if (!videoId.isEmpty()) {
          videoIds.add(videoId);
        }
      }

      log.info("YouTube 검색 완료: {} - {}개 영상 발견", query, videoIds.size());
      return videoIds;

    } catch (Exception e) {
      log.error("YouTube Search API 호출 실패: {}", e.getMessage());
      return List.of();
    }
  }

  /**
   * YouTube Videos API로 영상 상세 정보 조회 (조회수 포함)
   */
  private List<YoutubeVideoResponse> getVideoDetails(List<String> videoIds) {
    String idsParam = String.join(",", videoIds);

    String url = UriComponentsBuilder.fromHttpUrl(YOUTUBE_VIDEOS_URL)
        .queryParam("part", "snippet,statistics")
        .queryParam("id", idsParam)
        .queryParam("key", youtubeApiKey)
        .build()
        .toUriString();

    try {
      String response = restTemplate.getForObject(url, String.class);
      JsonNode root = objectMapper.readTree(response);
      JsonNode items = root.path("items");

      List<YoutubeVideoResponse> videos = new ArrayList<>();
      for (JsonNode item : items) {
        String videoId = item.path("id").asText();
        JsonNode snippet = item.path("snippet");
        JsonNode statistics = item.path("statistics");

        String title = snippet.path("title").asText();
        String description = snippet.path("description").asText();
        String channelTitle = snippet.path("channelTitle").asText();

        // 썸네일 (high > medium > default 순서로 선택)
        JsonNode thumbnails = snippet.path("thumbnails");
        String thumbnailUrl = thumbnails.has("high")
            ? thumbnails.path("high").path("url").asText()
            : thumbnails.has("medium")
                ? thumbnails.path("medium").path("url").asText()
                : thumbnails.path("default").path("url").asText();

        // 조회수 (없으면 0)
        long viewCount = statistics.has("viewCount")
            ? statistics.path("viewCount").asLong()
            : 0L;

        videos.add(new YoutubeVideoResponse(
            videoId,
            title,
            description.length() > 200 ? description.substring(0, 200) + "..." : description,
            thumbnailUrl,
            channelTitle,
            viewCount,
            YoutubeVideoResponse.buildVideoUrl(videoId)));
      }

      return videos;

    } catch (Exception e) {
      log.error("YouTube Videos API 호출 실패: {}", e.getMessage());
      return List.of();
    }
  }
}
