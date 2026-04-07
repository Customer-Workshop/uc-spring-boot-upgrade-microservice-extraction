package io.spring.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.application.TagsQueryService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.springframework.http.ResponseEntity;

public class TagsApiPropertyTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Property
  @SuppressWarnings("unchecked")
  void tag_list_endpoint_should_return_valid_json_regardless_of_tag_count(
      @ForAll @IntRange(min = 0, max = 100) int tagCount) throws JsonProcessingException {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    List<String> tags =
        IntStream.range(0, tagCount)
            .mapToObj(i -> "tag-" + i)
            .collect(Collectors.toList());
    when(tagsQueryService.allTags()).thenReturn(tags);

    TagsApi tagsApi = new TagsApi(tagsQueryService);
    ResponseEntity response = tagsApi.getTags();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCodeValue(), is(200));
    assertThat(response.getBody(), is(notNullValue()));

    // Verify the response body can be serialized to valid JSON
    String json = objectMapper.writeValueAsString(response.getBody());
    assertThat(json, is(notNullValue()));
    assertThat(json.isEmpty(), is(false));

    // Verify the JSON can be deserialized back
    Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
    assertThat(parsed.containsKey("tags"), is(true));

    List<String> returnedTags = (List<String>) parsed.get("tags");
    assertThat(returnedTags.size(), is(tagCount));
  }

  @Property
  @SuppressWarnings("unchecked")
  void tag_names_should_handle_unicode_characters(
      @ForAll("unicodeTagNames") List<String> tagNames) throws JsonProcessingException {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    when(tagsQueryService.allTags()).thenReturn(tagNames);

    TagsApi tagsApi = new TagsApi(tagsQueryService);
    ResponseEntity response = tagsApi.getTags();

    assertThat(response, is(notNullValue()));
    assertThat(response.getStatusCodeValue(), is(200));

    // Verify the response body can be serialized to valid JSON even with unicode
    String json = objectMapper.writeValueAsString(response.getBody());
    assertThat(json, is(notNullValue()));

    // Verify the JSON can be deserialized back and tags are preserved
    Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
    List<String> returnedTags = (List<String>) parsed.get("tags");
    assertThat(returnedTags.size(), is(tagNames.size()));

    // Verify each tag name is preserved exactly
    for (int i = 0; i < tagNames.size(); i++) {
      assertThat(returnedTags.get(i), is(tagNames.get(i)));
    }
  }

  @Property
  @SuppressWarnings("unchecked")
  void empty_tag_list_should_return_valid_response() throws JsonProcessingException {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    when(tagsQueryService.allTags()).thenReturn(Collections.emptyList());

    TagsApi tagsApi = new TagsApi(tagsQueryService);
    ResponseEntity response = tagsApi.getTags();

    assertThat(response.getStatusCodeValue(), is(200));

    String json = objectMapper.writeValueAsString(response.getBody());
    Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
    List<String> returnedTags = (List<String>) parsed.get("tags");
    assertThat(returnedTags.size(), is(0));
  }

  @Property
  @SuppressWarnings("unchecked")
  void tag_names_with_special_characters_should_be_preserved(
      @ForAll("specialCharTagNames") List<String> tagNames) throws JsonProcessingException {
    TagsQueryService tagsQueryService = mock(TagsQueryService.class);
    when(tagsQueryService.allTags()).thenReturn(tagNames);

    TagsApi tagsApi = new TagsApi(tagsQueryService);
    ResponseEntity response = tagsApi.getTags();

    String json = objectMapper.writeValueAsString(response.getBody());
    Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
    List<String> returnedTags = (List<String>) parsed.get("tags");

    for (int i = 0; i < tagNames.size(); i++) {
      assertThat(
          "Tag at index " + i + " should be preserved",
          returnedTags.get(i),
          is(tagNames.get(i)));
    }
  }

  @Provide
  Arbitrary<List<String>> unicodeTagNames() {
    Arbitrary<String> unicodeTag =
        Arbitraries.of(
            "java",
            "spring",
            "\u4e2d\u6587",
            "\u65e5\u672c\u8a9e",
            "\ud55c\uad6d\uc5b4",
            "caf\u00e9",
            "\u00fc\u00f6\u00e4",
            "\u00f1o\u00f1o",
            "\u0440\u0443\u0441\u0441\u043a\u0438\u0439",
            "\u0639\u0631\u0628\u064a",
            "tag-with-\u00e9m\u00f8j\u00ef",
            "\u2603\u2764\u2605");

    return unicodeTag.list().ofMinSize(1).ofMaxSize(20);
  }

  @Provide
  Arbitrary<List<String>> specialCharTagNames() {
    Arbitrary<String> specialTag =
        Arbitraries.of(
            "c++",
            "c#",
            ".net",
            "node.js",
            "vue.js",
            "objective-c",
            "f#",
            "asp.net",
            "tag with spaces",
            "tag/slash",
            "tag&amp",
            "tag<html>",
            "tag\"quote\"");

    return specialTag.list().ofMinSize(1).ofMaxSize(10);
  }
}
