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

    @PostMapping("/users/{id}/follow/{otherId}")
    public ResponseEntity<Void> followUser(
            @PathVariable("id") Long id,
            @PathVariable("otherId") Long otherId) {
        followService.followUser(id, otherId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> createPost(@RequestParam(name = "userId", required = false) Long userId) {
        List<Post> posts = postService.getPostsForFollowedUsers(userId, 10);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed")
    public ResponseEntity<Void> getFeed(@RequestParam(name = "userId", required = false) Long userId) {
        feedService.getUserFeed(userId);
        return ResponseEntity.ok().build();
    }
}
