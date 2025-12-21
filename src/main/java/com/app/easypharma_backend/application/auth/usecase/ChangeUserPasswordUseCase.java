package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.request.ChangePasswordRequest;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChangeUserPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void execute(String email, ChangePasswordRequest request) {
        log.info("Tentative de changement de mot de passe pour: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("L'ancien mot de passe est incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword()) && request.getConfirmPassword() != null) {
            // Optionnel : validation si le champ existe, mais backend peut se fier au
            // newPassword
            // Ici on ignore confirmPassword si le DTO ne le force pas, mais s'il est là on
            // peut vérifier
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Mot de passe mis à jour avec succès pour: {}", email);
    }
}
