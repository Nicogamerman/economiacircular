package com.pp.economia_circular.repositories;

import com.pp.economia_circular.entity.Articulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Articulo, Long> {
    
    List<Articulo> findByUsuario_Id(Long userId);
    
    List<Articulo> findByCategoria(Articulo.CategoriaArticulo categoria);
    
    List<Articulo> findByEstado(Articulo.EstadoArticulo estado);
    
    List<Articulo> findByCategoriaAndEstado(Articulo.CategoriaArticulo categoria, Articulo.EstadoArticulo estado);
    
    @Query("SELECT a FROM Articulo a WHERE a.estado = 'DISPONIBLE' ORDER BY a.creadoEn DESC")
    List<Articulo> findAvailableArticles();
    
    @Query("SELECT a FROM Articulo a WHERE a.estado = 'DISPONIBLE' ORDER BY a.creadoEn DESC")
    Page<Articulo> findAvailableArticles(Pageable pageable);
    
    @Query("SELECT a FROM Articulo a WHERE " +
           "(:title IS NULL OR LOWER(a.titulo) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:category IS NULL OR a.categoria = :category) AND " +
           "(:condition IS NULL OR a.condicion = :condition) AND " +
           "a.estado = 'DISPONIBLE'")
    Page<Articulo> searchArticles(@Param("title") String title,
                                @Param("category") Articulo.CategoriaArticulo category,
                                @Param("condition") Articulo.CondicionArticulo condition,
                                Pageable pageable);
    
    @Query("SELECT a FROM Articulo a WHERE a.usuario.id != :userId AND a.estado = 'DISPONIBLE'")
    List<Articulo> findArticlesByOtherUsers(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM Articulo a WHERE a.usuario.id = :userId AND a.estado = 'DISPONIBLE'")
    Long countAvailableArticlesByUser(@Param("userId") Long userId);
    
    @Query(value = "SELECT a.* FROM articulos a " +
           "LEFT JOIN vistas_articulos v ON v.articulo_id = a.id " +
           "WHERE a.estado = 'DISPONIBLE' " +
           "GROUP BY a.id " +
           "ORDER BY COUNT(v.id) DESC",
           nativeQuery = true)
    List<Articulo> findMostViewedArticles(Pageable pageable);

    List<Articulo> findByUsuarioEmail(String email);

}
