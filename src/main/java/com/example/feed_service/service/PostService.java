package com.example.feed_service.service;

import com.example.feed_service.model.Post;
import com.example.feed_service.repository.FollowerRepository;
import com.example.feed_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final FollowerRepository followerRepository;

    public void createPost() {
        // TODO: Implement create post logic & Kafka fan-out publication
    }

    /**
     * Pulls the latest posts from users that the given user follows.
     * Equivalent to:
     * SELECT * FROM posts WHERE author_id IN (people I follow) ORDER BY created_at DESC LIMIT limit
     */
    public List<Post> getPostsForFollowedUsers(Long userId, int limit) {
        List<Long> followedUserIds = followerRepository.findFollowingIdsByFollowerId(userId);

        if (followedUserIds.isEmpty()) {
            return Collections.emptyList();
        }

        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByAuthorIdInOrderByCreatedAtDesc(followedUserIds, pageable);
    }
}
