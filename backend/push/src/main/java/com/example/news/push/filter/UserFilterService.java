package com.example.news.push.filter;

import com.example.news.core.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFilterService {

    public List<User> filterTargets(List<User> users, String category) {
        return List.of();
    }
}
