package com.smart.campus.entity.constants;

public final class Constants {

    private Constants() {
    }

    public static final int DEFAULT_PAGE_NO = 1;

    public static final int DEFAULT_PAGE_SIZE = 15;

    public static final int ROOT_PARENT_ID = 0;

    public static final int FIRST_SORT_ORDER = 1;

    public static final int RESOURCE_CHUNK_SIZE = 1 * 1024 * 1024;

    public static final String FILE_SUFFIX_MP4 = "mp4";

    public static final String FILE_SUFFIX_M3U8 = "m3u8";

    public static final String FFMPEG_ARG_OVERWRITE = "-y";

    public static final String FFMPEG_ARG_INPUT = "-i";

    public static final String FFMPEG_VIDEO_CODEC_ARG = "-c:v";

    public static final String FFMPEG_AUDIO_CODEC_ARG = "-c:a";

    public static final String FFMPEG_VIDEO_CODEC_LIB_X264 = "libx264";

    public static final String FFMPEG_AUDIO_CODEC_AAC = "aac";

    public static final String RESOURCE_TASK_TYPE_MERGE_UPLOAD = "MERGE_UPLOAD";

    public static final String RESOURCE_TASK_TYPE_REUPLOAD_RESOURCE = "REUPLOAD_RESOURCE";

    public static final String RESOURCE_TASK_TYPE_TRANSCODE_VIDEO = "TRANSCODE_VIDEO";

    public static final String FOLDER_FILE = "/file";
    public static final String FOLDER_TEMP = "/temp";
    public static final String FOLDER_SOURCE = "/source";

}
