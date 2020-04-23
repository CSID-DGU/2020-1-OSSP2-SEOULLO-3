package com.instagramclone.Models;

import java.util.HashMap;
import java.util.Map;

public class BookmarkDTO {
    public String currentUser;
    public Map<String, Boolean> bookmarks = new HashMap<>();
    public ContentDTO contentDTO;
}
