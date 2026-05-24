package com.example.news.push.filter;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.User;
import com.example.news.core.repository.UserCategoryRepository;
import com.example.news.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFilterService {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final DndChecker dndChecker;

    public List<User> findTargetsForArticle(Article article) {
        List<Long> userIds = userCategoryRepository
            .findById_CategoryId(article.getCategoryId()).stream()
            .map(uc -> uc.getId().getUserId())
            .toList();
        List<User> subscribers = userRepository.findAllById(userIds);
        return subscribers.stream()
            .filter(u -> !dndChecker.isInDnd(u))
            .toList();
    }
}
