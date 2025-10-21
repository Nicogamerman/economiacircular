package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    List<Article> findByUser_Id(Long userId);
    
    List<Article> findByCategory(Article.ArticleCategory category);
    
    List<Article> findByStatus(Article.ArticleStatus status);
    
    List<Article> findByCategoryAndStatus(Article.ArticleCategory category, Article.ArticleStatus status);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'AVAILABLE' ORDER BY a.createdAt DESC")
    List<Article> findAvailableArticles();
    
    @Query("SELECT a FROM Article a WHERE a.status = 'AVAILABLE' ORDER BY a.createdAt DESC")
    Page<Article> findAvailableArticles(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE " +
           "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:condition IS NULL OR a.condition = :condition) AND " +
           "a.status = 'AVAILABLE'")
    Page<Article> searchArticles(@Param("title") String title, 
                                @Param("category") Article.ArticleCategory category,
                                @Param("condition") Article.ArticleCondition condition,
                                Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.user.id != :userId AND a.status = 'AVAILABLE'")
    List<Article> findArticlesByOtherUsers(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM Article a WHERE a.user.id = :userId AND a.status = 'AVAILABLE'")
    Long countAvailableArticlesByUser(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'AVAILABLE' ORDER BY " +
           "(SELECT COUNT(v) FROM ArticleView v WHERE v.article = a) DESC")
    List<Article> findMostViewedArticles(Pageable pageable);
}
