package com.yonhap.news.push.filter;

import com.yonhap.news.core.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFilterService {

    public List<User> filterTargets(List<User> users, String category) {
        return List.of();
    }
}
