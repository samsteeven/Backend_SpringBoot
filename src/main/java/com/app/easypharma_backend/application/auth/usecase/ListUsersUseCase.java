package com.app.easypharma_backend.application.auth.usecase;

import com.app.easypharma_backend.application.auth.dto.response.UserResponse;
import com.app.easypharma_backend.application.auth.mapper.UserMapper;
import com.app.easypharma_backend.application.common.dto.PageResponse;
import com.app.easypharma_backend.domain.auth.entity.User;
import com.app.easypharma_backend.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListUsersUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Liste les utilisateurs avec pagination
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(Pageable pageable) {
        log.info("Récupération de la liste des utilisateurs avec pagination");

        Page<User> usersPage = userRepository.findAll(pageable);

        return PageResponse.<UserResponse>builder()
                .content(usersPage.getContent().stream()
                        .map(userMapper::toResponse)
                        .collect(Collectors.toList()))
                .pageNumber(usersPage.getNumber())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .isFirst(usersPage.isFirst())
                .isLast(usersPage.isLast())
                .hasNext(usersPage.hasNext())
                .hasPrevious(usersPage.hasPrevious())
                .build();
    }
}