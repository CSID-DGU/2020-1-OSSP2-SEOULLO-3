package com.instagramclone.Models;

import java.util.HashMap;
import java.util.Map;

public class FollowDTO {

    public int followerCount = 0;
    public Map<String, Boolean> followers = new HashMap<>();

    public int followingCount = 0;
    public Map<String, Boolean> followings = new HashMap<>();
}
