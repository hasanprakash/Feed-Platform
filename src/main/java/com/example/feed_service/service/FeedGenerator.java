package com.example.feed_service.service;

import com.example.feed_service.model.Post;
import com.example.feed_service.repository.FollowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FeedGenerator {

    private final FollowerRepository followerRepository;
    private final PostService postService;

    public List<Post> generateFeedForUser(Long userId, int limit) {
        // 1. Query all followed users
        List<Long> followedUserIds = followerRepository.findFollowingIdsByFollowerId(userId);

        if (followedUserIds.isEmpty()) {
            return List.of();
        }

        // 2. Collect posts for each followed user individually (avoiding huge IN clause)
        List<Post> allPosts = new ArrayList<>();
        for (Long followedId : followedUserIds) {
            List<Post> posts = postService.getRecentPostsForAuthor(followedId, limit);
            allPosts.addAll(posts);
        }

        // 3. Sort by created_at descending and 4. Return top 'limit' posts
        return allPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
