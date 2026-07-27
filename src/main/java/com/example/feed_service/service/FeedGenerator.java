package com.example.feed_service.service;

import com.example.feed_service.model.Post;
import com.example.feed_service.repository.FollowerRepository;
import com.example.feed_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedGenerator {

    private final FollowerRepository followerRepository;
    private final PostService postService;
    private final PostRepository postRepository;
    private final StringRedisTemplate redisTemplate;

    public List<Post> generateFeedForUser(Long userId, int limit) {
        String feedKey = "feed:" + userId;

        // 1. Check Redis Cache First (Cache Hit)
        // We use ZREVRANGE to get the latest posts (highest timestamp scores)
        Set<String> cachedPostIds = redisTemplate.opsForZSet().reverseRange(feedKey, 0, limit - 1);
        
        if (cachedPostIds != null && !cachedPostIds.isEmpty()) {
            log.info("CACHE HIT: Retrieved {} cached post IDs for userId={}", cachedPostIds.size(), userId);
            List<Long> postIds = cachedPostIds.stream().map(Long::valueOf).collect(Collectors.toList());
            List<Post> posts = postRepository.findAllById(postIds);
            
            // Re-sort them because findAllById doesn't guarantee the order of the IN clause
            return posts.stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        }

        // 2. Cache Miss - Generate Feed
        log.info("CACHE MISS: Generating feed from DB for userId={}", userId);
        List<Long> followedUserIds = followerRepository.findFollowingIdsByFollowerId(userId);

        if (followedUserIds.isEmpty()) {
            log.info("No followed users found for userId={}", userId);
            return List.of();
        }

        List<Post> allPosts = new ArrayList<>();
        for (Long followedId : followedUserIds) {
            List<Post> posts = postService.getRecentPostsForAuthor(followedId, limit);
            allPosts.addAll(posts);
        }

        List<Post> topPosts = allPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 3. Store in Redis
        for (Post post : topPosts) {
            redisTemplate.opsForZSet().add(feedKey, String.valueOf(post.getId()), post.getCreatedAt().toEpochMilli());
        }
        
        // Optional: Set a TTL for the feed key so it expires if the user goes inactive
        redisTemplate.expire(feedKey, Duration.ofDays(7));
        log.info("CACHE POPULATED: Stored {} posts in Redis feed for userId={}", topPosts.size(), userId);

        return topPosts;
    }
}
