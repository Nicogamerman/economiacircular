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

    long countByUsuario_IdAndEstado(Long usuarioId, Articulo.EstadoArticulo estado);
    
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

    @Query("SELECT DISTINCT a FROM Articulo a LEFT JOIN a.etiquetas e WHERE " +
           "a.estado = 'DISPONIBLE' AND " +
           "(:category IS NULL OR a.categoria = :category) AND " +
           "(:condition IS NULL OR a.condicion = :condition) AND " +
           "(:subcategoria IS NULL OR LOWER(a.subcategoria) = LOWER(:subcategoria)) AND " +
           "(:marca IS NULL OR LOWER(a.marca) = LOWER(:marca)) AND " +
           "(:tag IS NULL OR LOWER(e.etiqueta) = LOWER(:tag)) AND " +
           "(:q IS NULL OR " +
           "  LOWER(a.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(a.descripcion) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(COALESCE(a.subcategoria, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(COALESCE(a.marca, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(COALESCE(a.modelo, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "  LOWER(e.etiqueta) LIKE LOWER(CONCAT('%', :q, '%'))" +
           ")")
    Page<Articulo> searchArticlesAvanzada(@Param("q") String q,
                                          @Param("category") Articulo.CategoriaArticulo category,
                                          @Param("condition") Articulo.CondicionArticulo condition,
                                          @Param("subcategoria") String subcategoria,
                                          @Param("marca") String marca,
                                          @Param("tag") String tag,
                                          Pageable pageable);

    @Query("SELECT DISTINCT a.subcategoria FROM Articulo a " +
           "WHERE (:category IS NULL OR a.categoria = :category) " +
           "AND a.subcategoria IS NOT NULL AND a.subcategoria <> '' " +
           "ORDER BY a.subcategoria")
    List<String> findDistinctSubcategorias(@Param("category") Articulo.CategoriaArticulo category);

    @Query("SELECT DISTINCT a.marca FROM Articulo a " +
           "WHERE (:category IS NULL OR a.categoria = :category) " +
           "AND a.marca IS NOT NULL AND a.marca <> '' " +
           "ORDER BY a.marca")
    List<String> findDistinctMarcas(@Param("category") Articulo.CategoriaArticulo category);
    
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
