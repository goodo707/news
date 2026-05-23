package com.yonhap.news.push.filter;

import com.yonhap.news.core.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFilterService {

    public List<UserProfile> filterTargets(List<UserProfile> users, String category) {
        return List.of();
    }
}
