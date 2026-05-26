package com.scanner.demo.file.controller;

import com.scanner.demo.file.entity.AttachedFile;
import com.scanner.demo.file.repository.AttachedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final AttachedFileRepository attachedFileRepository;

    @GetMapping("/download/{fileSeq}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Integer fileSeq) throws Exception {

        // 1. DB에서 파일 메타데이터 조회
        AttachedFile attachedFile = attachedFileRepository.findById(fileSeq)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        // 2. 로컬 디스크의 실제 파일 경로를 Resource 객체로 매핑
        Path filePath = Paths.get(attachedFile.getFilePath()).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("서버에 실제 파일이 존재하지 않습니다.");
        }

        // 💡 3. 한글 파일명 깨짐 방지 인코딩 (아주 중요!)
        String encodedFileName = UriUtils.encode(attachedFile.getFileName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";

        // 4. 응답 헤더에 세팅하여 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}