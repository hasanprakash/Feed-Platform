package com.example.feed_service.repository;

import com.example.feed_service.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorIdInOrderByCreatedAtDesc(Collection<Long> authorIds, Pageable pageable);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}
