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

    public void createPost(Long userId, Post post) {
        postRepository.save(post);
    }

    /**
     * Pulls the latest posts from a single author.
     */
    public List<Post> getRecentPostsForAuthor(Long authorId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }
}
