package com.example.backend.service;

import com.example.backend.model.Client;
import com.example.backend.model.CompanyAccountant;
import com.example.backend.model.Notification;
import com.example.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MongoTemplate mongoTemplate;

    public List<Notification> getNotifications(String accountantId) {
        // Récupérer l'objet CompanyAccountant par son id String
        CompanyAccountant accountant = mongoTemplate.findById(accountantId, CompanyAccountant.class);
        return notificationRepository.findByAccountantOrderByCreatedAtDesc(accountant);
    }

    public Long getUnreadCount(String accountantId) {
        CompanyAccountant accountant = mongoTemplate.findById(accountantId, CompanyAccountant.class);
        return notificationRepository.countByAccountantAndReadFalse(accountant);
    }

    public void markAllAsRead(String accountantId) {
        CompanyAccountant accountant = mongoTemplate.findById(accountantId, CompanyAccountant.class);
        List<Notification> notifications = notificationRepository.findByAccountantAndReadFalse(accountant);
        for (Notification n : notifications) {
            n.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
    public void createNotification(String accountantId, String clientId, String message) {
        CompanyAccountant accountant = mongoTemplate.findById(accountantId, CompanyAccountant.class);
        Client client = mongoTemplate.findById(clientId, Client.class);

        Notification notification = new Notification();
        notification.setAccountant(accountant);
        notification.setClient(client);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Scheduled(cron = "0 * * * * *") // Pour test : toutes les minutes
    public void autoDeleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(15);
        Query query = new Query();
        query.addCriteria(Criteria.where("createdAt").lt(threshold));

        long count = mongoTemplate.remove(query, Notification.class).getDeletedCount();
        if (count > 0) {
            System.out.println("Auto-deleted notifications: " + count);
        }
    }


}
