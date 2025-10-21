package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.MessageCreateDto;
import com.pp.economia_circular.DTO.MessageResponseDto;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.entity.Article;
import com.pp.economia_circular.entity.Message;
import com.pp.economia_circular.repositories.ArticleRepository;
import com.pp.economia_circular.repositories.MessageRepository;
import com.pp.economia_circular.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public MessageResponseDto sendMessage(MessageCreateDto createDto, Long senderId) {
        Usuario sender = usuarioRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Usuario emisor no encontrado"));

        Usuario receiver = null;
        Article article = null;
        
        if (createDto.getReceiverId() != null) {
            // Mensaje directo a usuario
            receiver = usuarioRepository.findById(createDto.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Usuario receptor no encontrado"));
        } else if (createDto.getArticleId() != null) {
            // Mensaje relacionado con artículo
            article = articleRepository.findById(createDto.getArticleId())
                    .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
            receiver = article.getUser();
        } else {
            throw new RuntimeException("Debe especificar un receptor o un artículo");
        }
        
        Message message = new Message();
        message.setContent(createDto.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setArticle(article);
        
        Message savedMessage = messageRepository.save(message);
        return convertToResponseDto(savedMessage);
    }
    
    public List<MessageResponseDto> getMyMessages(Long userId) {
        return messageRepository.findConversationsByUser(userId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<MessageResponseDto> getConversationWithUser(Long userId, Long otherUserId) {
        return messageRepository.findConversationBetweenUsers(userId, otherUserId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<MessageResponseDto> getMessagesByArticle(Long articleId) {
        return messageRepository.findByArticle_Id(articleId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public List<MessageResponseDto> getUnreadMessages(Long userId) {
        return messageRepository.findUnreadMessagesByUser(userId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    public Long getUnreadMessageCount(Long userId) {
        return messageRepository.countUnreadMessagesByUser(userId);
    }
    
    public MessageResponseDto markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar que el usuario sea el receptor del mensaje
        if (!message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para marcar este mensaje como leído");
        }
        
        message.setStatus(Message.MessageStatus.READ);
        message.setReadAt(LocalDateTime.now());
        
        Message updatedMessage = messageRepository.save(message);
        return convertToResponseDto(updatedMessage);
    }
    
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));
        
        // Verificar que el usuario sea el emisor o receptor del mensaje
        if (!message.getSender().getId().equals(userId) && 
            !message.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para eliminar este mensaje");
        }
        
        message.setStatus(Message.MessageStatus.DELETED);
        messageRepository.save(message);
    }
    
    private MessageResponseDto convertToResponseDto(Message message) {
        MessageResponseDto dto = new MessageResponseDto();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getEmail()); // Usando email como username por ahora
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverUsername(message.getReceiver().getEmail()); // Usando email como username por ahora
        dto.setArticleId(message.getArticle() != null ? message.getArticle().getId() : null);
        dto.setStatus(message.getStatus());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        return dto;
    }
}
