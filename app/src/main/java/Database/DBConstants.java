package Database;

public interface DBConstants {

	int DATABASE_VERSION =1;
	String DATABASE_NAME = "DJAppDatabase";
	
	//State Table
	String STATES_TABLE_NAME = "States";
	
	//Country Table
	String COUNTRY_TABLE_NAME = "dj_country";
	String COUNTRY_COUNTRY_NAME = "Country_Name";
	String COUNTRY_COUNTRY_ID = "Country_ID";
	
	//State Table
	String STATE_TABLE_NAME = "dj_state";
	String STATE_STATE_NAME = "State_Name";
	String STATE_STATE_ID = "State_ID";
	String STATE_COUNTRY_ID = "Country_ID";

	String CHAPTER_TAG_ID = "ChapterTagID";
	String CHAPTER_ORDER = "ChapterOrder";
	String CHAPTER_STATUS = "ChapterStatus";
	String TOTAL_SIZE = "TotalSize";
	String TOTAL_DURATION = "TotalDuration";
	String CREATED_DATE = "CreatedDate";
	String VIDEO_COUNT = "VideoCount";
	String CHAPTER_IMAGE = "ChapterImage";
	String CATEGORY_ID = "CategoryID";
	String CHAPTER_NAME = "ChapterName";
	String TAG = "Tag";
	String TAG_ID = "TagID";
	String VIDEO_ORDER = "VideoOrder";
	String RNO = "Rno";
	String STATUS = "Status";
	String SEARCH_TAGS = "SearchTags";
	String SIZE = "Size";
	String DURATION = "Duration";
	String DESCRIPTION = "Description";
	String IMAGE = "Image";
	String DOWNLOAD_URL = "DownloadURL";
	String STREAMING_URL = "StreamingURL";
	String VIDEO_URL = "VideoURL";
	String TYPE = "Type";
	String TITLE = "Title";
	String CHAPTER_ID = "ChapterID";
	String VIDEO_ID = "VideoID";
	String ID = "Id";
	
}
