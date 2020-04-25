package com.seoullo.seoullotour.Models;

import java.util.HashMap;
import java.util.Map;

public class Bookmark {
    public String currentUser;
    public Map<String, Boolean> bookmarks = new HashMap<>();
    public User user;
}
