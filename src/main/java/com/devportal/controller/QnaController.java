package com.devportal.controller;

import com.devportal.dto.request.QnaAnswerRequest;
import com.devportal.dto.request.QnaCommentRequest;
import com.devportal.dto.request.QnaQuestionRequest;
import com.devportal.dto.response.QnaAnswerResponse;
import com.devportal.dto.response.QnaCommentResponse;
import com.devportal.dto.response.QnaQuestionResponse;
import com.devportal.service.GridFsFileService;
import com.devportal.service.QnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;
    private final GridFsFileService gridFsFileService;

    @PostMapping("/questions")
    public ResponseEntity<QnaQuestionResponse> createQuestion(@RequestBody QnaQuestionRequest request) {
        QnaQuestionResponse response = qnaService.createQuestion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/questions")
    public ResponseEntity<Page<QnaQuestionResponse>> getQuestions(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QnaQuestionResponse> questions = qnaService.getQuestions(filter, tag, pageable);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{id}")
    public ResponseEntity<QnaQuestionResponse> getQuestion(@PathVariable UUID id) {
        QnaQuestionResponse response = qnaService.getQuestion(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<QnaQuestionResponse>> searchQuestions(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<QnaQuestionResponse> results = qnaService.searchQuestions(q, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/questions/{id}/upvote")
    public ResponseEntity<Void> upvoteQuestion(@PathVariable UUID id) {
        qnaService.upvoteQuestion(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        qnaService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/answers")
    public ResponseEntity<QnaAnswerResponse> createAnswer(@RequestBody QnaAnswerRequest request) {
        QnaAnswerResponse response = qnaService.createAnswer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/answers/{id}/upvote")
    public ResponseEntity<Void> upvoteAnswer(@PathVariable UUID id) {
        qnaService.upvoteAnswer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/answers/{id}/accept")
    public ResponseEntity<Void> acceptAnswer(@PathVariable UUID id) {
        qnaService.acceptAnswer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments")
    public ResponseEntity<QnaCommentResponse> createComment(@RequestBody QnaCommentRequest request) {
        QnaCommentResponse response = qnaService.createComment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attachments")
    public ResponseEntity<QnaQuestionResponse.AttachmentResponse> uploadAttachment(
            @RequestParam(required = false) UUID questionId,
            @RequestParam(required = false) UUID answerId,
            @RequestParam("file") MultipartFile file) throws IOException {
        QnaQuestionResponse.AttachmentResponse response = qnaService.uploadAttachment(questionId, answerId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attachments/{mongoFileId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(@PathVariable String mongoFileId) {
        InputStream inputStream = gridFsFileService.getFileAsStream(mongoFileId);
        String contentType = gridFsFileService.getFileContentType(mongoFileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                .body(new InputStreamResource(inputStream));
    }
}
