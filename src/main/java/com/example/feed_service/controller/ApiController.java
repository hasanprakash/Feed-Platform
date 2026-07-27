package com.example.feed_service.controller;

import com.example.feed_service.model.Post;
import com.example.feed_service.service.FeedService;
import com.example.feed_service.service.FollowService;
import com.example.feed_service.service.PostService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ApiController {

    private final FollowService followService;
    private final PostService postService;
    private final FeedService feedService;
    private final com.example.feed_service.service.FeedGenerator feedGenerator;

    @PostMapping("/users/{id}/follow/{otherId}")
    public ResponseEntity<Void> followUser(
            @PathVariable("id") Long id,
            @PathVariable("otherId") Long otherId) {
        followService.followUser(id, otherId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts")
    public ResponseEntity<Void> createPost(@RequestParam(name = "userId", required = false) Long userId,
            @RequestBody Post post) {
        postService.createPost(userId, post);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Post>> getFeed(@RequestParam(name = "userId", required = false) Long userId) {
        List<Post> posts = feedGenerator.generateFeedForUser(userId, 10);
        return ResponseEntity.ok(posts);
    }
}
