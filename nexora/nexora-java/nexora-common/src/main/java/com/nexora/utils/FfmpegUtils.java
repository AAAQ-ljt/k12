package com.nexora.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class FfmpegUtils {

    private static final String THUMBNAIL_SCALE_FILTER = "scale=if(gt(iw\\,300)\\,300\\,iw):-2";

    // 视频上传完成后统一切成 HLS，主播放文件为 m3u8，分片文件为 ts。
    public void convertToTsSegments(String sourcePath, String playlistPath) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        String segmentPattern = playlistPath.replace("index.m3u8", "%05d.ts");
        execute(List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "error",
                "-y",
                "-i",
                sourcePath,
                "-c:v",
                "libx264",
                "-c:a",
                "aac",
                "-hls_time",
                "10",
                "-hls_list_size",
                "0",
                "-start_number",
                "0",
                "-hls_segment_type",
                "mpegts",
                "-hls_segment_filename",
                segmentPattern,
                "-f",
                "hls",
                playlistPath
        ));
        log.info("ffmpeg切片耗时：{}ms", System.currentTimeMillis() - start);
    }

    // 使用 ffprobe 获取媒体时长，向上取整为秒，便于列表直接展示。
    public Integer probeDurationSeconds(String sourcePath) throws IOException, InterruptedException {
        String durationText = executeForOutput(List.of(
                "ffprobe",
                "-v",
                "error",
                "-show_entries",
                "format=duration",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                sourcePath
        ));
        if (durationText.isBlank()) {
            return 0;
        }
        double duration = Double.parseDouble(durationText.trim());
        return (int) Math.ceil(duration);
    }

    // HLS 播放优先要求 H.264 + AAC，编码不兼容时先转成标准 MP4 再切片。
    public boolean requiresMp4Transcode(String sourcePath) throws IOException, InterruptedException {
        String videoCodec = probeCodec(sourcePath, "v:0");
        String audioCodec = probeCodec(sourcePath, "a:0");
        boolean videoUnsupported = videoCodec.isBlank() || !"h264".equalsIgnoreCase(videoCodec);
        boolean audioUnsupported = !audioCodec.isBlank() && !"aac".equalsIgnoreCase(audioCodec);
        return videoUnsupported || audioUnsupported;
    }

    public void convertToMp4(String sourcePath, String targetPath) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        execute(List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "error",
                "-y",
                "-i",
                sourcePath,
                "-c:v",
                "libx264",
                "-c:a",
                "aac",
                "-movflags",
                "+faststart",
                targetPath
        ));
        log.info("ffmpeg转码耗时：{}ms", System.currentTimeMillis() - start);
    }

    public void generateVideoCover(String sourcePath, String targetPath) throws IOException, InterruptedException {
        execute(List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "error",
                "-y",
                "-i",
                sourcePath,
                "-ss",
                "1",
                "-frames:v",
                "1",
                "-vf",
                THUMBNAIL_SCALE_FILTER,
                "-q:v",
                "4",
                targetPath
        ));
    }

    public void generateImageThumbnail(String sourcePath, String targetPath) throws IOException, InterruptedException {
        execute(List.of(
                "ffmpeg",
                "-hide_banner",
                "-loglevel",
                "error",
                "-y",
                "-i",
                sourcePath,
                "-frames:v",
                "1",
                "-vf",
                THUMBNAIL_SCALE_FILTER,
                "-q:v",
                "4",
                targetPath
        ));
    }

    private String probeCodec(String sourcePath, String streamSelector) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        String output = executeForOutput(List.of(
                "ffprobe",
                "-v",
                "error",
                "-select_streams",
                streamSelector,
                "-show_entries",
                "stream=codec_name",
                "-of",
                "default=noprint_wrappers=1:nokey=1",
                sourcePath
        ));
        log.info("ffprobe耗时：{}ms", System.currentTimeMillis() - start);
        return output.trim().toLowerCase(Locale.ROOT);
    }

    private void execute(List<String> command) throws IOException, InterruptedException {
        String output = executeForOutput(command);
        if (output == null) {
            throw new IOException("ffmpeg执行失败");
        }
    }

    private String executeForOutput(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        List<String> outputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                outputLines.add(line);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException(String.join(System.lineSeparator(), outputLines));
        }
        return String.join(System.lineSeparator(), outputLines).trim();
    }
}
