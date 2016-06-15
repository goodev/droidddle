package org.goodev.droidddle.pojo;

import java.util.ArrayList;
import java.util.List;

public class Rebound {
    public Long id;
    public String title;
    public String description;
    public Integer width;
    public Integer height;
    public Image images;
    public Integer viewsCount;
    public Integer likesCount;
    public Integer commentsCount;
    public Integer attachmentsCount;
    public Integer reboundsCount;
    public Integer bucketsCount;
    public String createdAt;
    public String updatedAt;
    public String htmlUrl;
    public String attachmentsUrl;
    public String commentsUrl;
    public String likesUrl;
    public String projectsUrl;
    public String reboundsUrl;
    public String reboundSourceUrl;
    public List<String> tags = new ArrayList<String>();
    public User user;
    public Team team;
}
