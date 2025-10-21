package com.pp.economia_circular.DTO;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MessageCreateDto {
    
    @NotBlank(message = "El contenido del mensaje es obligatorio")
    @Size(max = 1000, message = "El mensaje no puede exceder 1000 caracteres")
    private String content;
    
    private Long receiverId;
    private Long articleId;
    
    // Constructors
    public MessageCreateDto() {}
    
    public MessageCreateDto(String content, Long receiverId) {
        this.content = content;
        this.receiverId = receiverId;
    }
    
    public MessageCreateDto(String content, Long receiverId, Long articleId) {
        this.content = content;
        this.receiverId = receiverId;
        this.articleId = articleId;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    
    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
}
