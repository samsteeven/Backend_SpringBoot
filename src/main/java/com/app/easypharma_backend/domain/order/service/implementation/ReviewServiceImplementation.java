package com.app.easypharma_backend.domain.order.service.implementation;

import com.app.easypharma_backend.domain.order.dto.CreateReviewDTO;
import com.app.easypharma_backend.domain.order.dto.ReviewDTO;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderStatus;
import com.app.easypharma_backend.domain.order.entity.Review;
import com.app.easypharma_backend.domain.order.entity.ReviewStatus;
import com.app.easypharma_backend.domain.order.repository.OrderRepository;
import com.app.easypharma_backend.domain.order.repository.ReviewRepository;
import com.app.easypharma_backend.domain.pharmacy.entity.Pharmacy;
import com.app.easypharma_backend.domain.pharmacy.repository.PharmacyRepository;
import com.app.easypharma_backend.domain.delivery.repository.DeliveryRepository;
import com.app.easypharma_backend.domain.delivery.entity.Delivery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImplementation {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DeliveryRepository deliveryRepository;

    public ReviewDTO createReview(UUID patientId, CreateReviewDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Commande non trouvée"));

        if (!order.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Vous ne pouvez pas noter une commande qui ne vous appartient pas");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Vous ne pouvez noter que les commandes livrées");
        }

        // Récupérer le livreur s'il y en a un
        Delivery delivery = deliveryRepository.findByOrderId(order.getId()).orElse(null);

        Review review = Review.builder()
                .patient(order.getPatient())
                .pharmacy(order.getPharmacy())
                .order(order)
                .courier(delivery != null ? delivery.getDeliveryPerson() : null)
                .rating(dto.getPharmacyRating())
                .comment(dto.getPharmacyComment())
                .courierRating(dto.getCourierRating())
                .courierComment(dto.getCourierComment())
                .status(ReviewStatus.PENDING)
                .build();

        Review saved = reviewRepository.save(review);
        return mapToDTO(saved);
    }

    public ReviewDTO moderateReview(UUID reviewId, ReviewStatus status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        ReviewStatus oldStatus = review.getStatus();
        review.setStatus(status);
        Review saved = reviewRepository.save(review);

        // Mettre à jour la moyenne si le statut passe à APPROVED ou s'il l'était déjà
        if (status == ReviewStatus.APPROVED || oldStatus == ReviewStatus.APPROVED) {
            updatePharmacyAverageRating(review.getPharmacy().getId());
        }

        return mapToDTO(saved);
    }

    public void deleteReview(UUID reviewId, UUID patientId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        if (!review.getPatient().getId().equals(patientId)) {
            throw new RuntimeException("Vous ne pouvez supprimer que votre propre avis");
        }

        boolean wasApproved = review.getStatus() == ReviewStatus.APPROVED;
        reviewRepository.delete(review);

        if (wasApproved) {
            updatePharmacyAverageRating(review.getPharmacy().getId());
        }
    }

    private void updatePharmacyAverageRating(UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new RuntimeException("Pharmacie non trouvée"));

        Double avg = reviewRepository.calculateAverageRating(pharmacyId);
        Integer count = reviewRepository.countApprovedReviews(pharmacyId);

        pharmacy.setAverageRating(avg != null ? avg : 0.0);
        pharmacy.setRatingCount(count);
        pharmacyRepository.save(pharmacy);
    }

    public List<ReviewDTO> getPharmacyReviews(UUID pharmacyId) {
        return getPharmacyReviews(pharmacyId, null);
    }

    public List<ReviewDTO> getPharmacyReviews(UUID pharmacyId, UUID currentPatientId) {
        // Récupérer tous les avis approuvés
        List<Review> approved = reviewRepository.findByPharmacyIdAndStatusOrderByCreatedAtDesc(pharmacyId,
                ReviewStatus.APPROVED);

        // Si utilisateur connecté, récupérer son avis (même si PENDING) et l'ajouter si
        // présent
        if (currentPatientId != null) {
            List<Review> patientReviews = reviewRepository.findByPharmacyIdAndPatientIdOrderByCreatedAtDesc(pharmacyId,
                    currentPatientId);

            // Fusionner en évitant les doublons (même id)
            for (Review r : patientReviews) {
                boolean exists = approved.stream().anyMatch(a -> a.getId().equals(r.getId()));
                if (!exists) {
                    approved.add(0, r); // ajouter en tête pour garder l'ordre récent
                }
            }
        }

        return approved.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ReviewDTO mapToDTO(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .patientId(review.getPatient().getId())
                .patientName(review.getPatient().getFirstName() + " " + review.getPatient().getLastName())
                .pharmacyName(review.getPharmacy().getName())
                .courierName(review.getCourier() != null
                        ? review.getCourier().getFirstName() + " " + review.getCourier().getLastName()
                        : "Standard")
                .pharmacyRating(review.getRating())
                .pharmacyComment(review.getComment())
                .courierRating(review.getCourierRating())
                .courierComment(review.getCourierComment())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
