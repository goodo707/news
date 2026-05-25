package com.example.news.push.filter;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.User;
import com.example.news.core.domain.UserCategory;
import com.example.news.core.domain.UserCategoryId;
import com.example.news.core.repository.UserCategoryRepository;
import com.example.news.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserFilterServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserCategoryRepository userCategoryRepository;
    @Mock DndChecker dndChecker;
    @InjectMocks UserFilterService userFilterService;

    private Article articleInCategory(long categoryId) {
        return new Article(
            "AKR-test", "title", "https://www.yna.co.kr/view/AKR-test",
            "author", categoryId, "2026-05-25T12:00:00", "2026-05-25T12:00:00"
        );
    }

    private User user(long id) {
        return new User(id, "name-" + id, "device-" + id, "APNS", null, null);
    }

    @Test
    void 구독자가_없으면_빈_리스트() {
        Article article = articleInCategory(1L);
        given(userCategoryRepository.findById_CategoryId(1L)).willReturn(List.of());
        given(userRepository.findAllById(List.of())).willReturn(List.of());

        List<User> result = userFilterService.findTargetsForArticle(article);

        assertThat(result).isEmpty();
    }

    @Test
    void 구독자_모두_DND_외_전원_반환() {
        Article article = articleInCategory(1L);
        User u1 = user(1L), u2 = user(2L), u3 = user(3L);
        given(userCategoryRepository.findById_CategoryId(1L)).willReturn(List.of(
            new UserCategory(new UserCategoryId(1L, 1L)),
            new UserCategory(new UserCategoryId(2L, 1L)),
            new UserCategory(new UserCategoryId(3L, 1L))
        ));
        given(userRepository.findAllById(List.of(1L, 2L, 3L))).willReturn(List.of(u1, u2, u3));
        given(dndChecker.isInDnd(any())).willReturn(false);

        List<User> result = userFilterService.findTargetsForArticle(article);

        assertThat(result).containsExactly(u1, u2, u3);
    }

    @Test
    void 일부가_DND_시간대면_제외되어_반환() {
        Article article = articleInCategory(1L);
        User u1 = user(1L), u2 = user(2L), u3 = user(3L);
        given(userCategoryRepository.findById_CategoryId(1L)).willReturn(List.of(
            new UserCategory(new UserCategoryId(1L, 1L)),
            new UserCategory(new UserCategoryId(2L, 1L)),
            new UserCategory(new UserCategoryId(3L, 1L))
        ));
        given(userRepository.findAllById(List.of(1L, 2L, 3L))).willReturn(List.of(u1, u2, u3));
        given(dndChecker.isInDnd(u1)).willReturn(false);
        given(dndChecker.isInDnd(u2)).willReturn(true);
        given(dndChecker.isInDnd(u3)).willReturn(false);

        List<User> result = userFilterService.findTargetsForArticle(article);

        assertThat(result).containsExactly(u1, u3);
    }

    @Test
    void 모두_DND_시간대면_빈_리스트() {
        Article article = articleInCategory(2L);
        User u1 = user(1L);
        given(userCategoryRepository.findById_CategoryId(2L)).willReturn(List.of(
            new UserCategory(new UserCategoryId(1L, 2L))
        ));
        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(u1));
        given(dndChecker.isInDnd(u1)).willReturn(true);

        List<User> result = userFilterService.findTargetsForArticle(article);

        assertThat(result).isEmpty();
    }
}
