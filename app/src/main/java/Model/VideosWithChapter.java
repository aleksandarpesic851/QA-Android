package Model;

import java.io.Serializable;

public class VideosWithChapter implements Serializable {

    public int id;

    public int VideoID;

    public int ChapterID;

    public String Title;

    public String Type;

    public String VideoURL;

    public String StreamingURL;

    public String DownloadURL;

    public String Image;

    public String Description;

    public String Duration;

    public String Size;

    public String SearchTags;

    public int Status = 1;

    public String Rno = "";

    public int video_Order = 1;

    public String TagID = "";

    public String Tag = "";

    public String ChapterName;

    public String CategoryID;

    public String ChapterImage;

    public String VideoCount;

    public String CreatedDate;

    public String TotalDuration;

    public String TotalSize;

    public int ChapterOrder;

    public int ChapterStatus = 1;

    public String ChapterTagID = "";

    public boolean isChapter = false;
}