package com.devportal.service;

import com.devportal.domain.entity.*;
import com.devportal.domain.enums.ModuleType;
import com.devportal.dto.request.QnaAnswerRequest;
import com.devportal.dto.request.QnaCommentRequest;
import com.devportal.dto.request.QnaQuestionRequest;
import com.devportal.dto.response.QnaAnswerResponse;
import com.devportal.dto.response.QnaCommentResponse;
import com.devportal.dto.response.QnaQuestionResponse;
import com.devportal.dto.response.QnaQuestionResponse.*;
import com.devportal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaQuestionRepository questionRepository;
    private final QnaAnswerRepository answerRepository;
    private final QnaCommentRepository commentRepository;
    private final QnaAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final GridFsFileService gridFsFileService;

    @Transactional
    public QnaQuestionResponse createQuestion(QnaQuestionRequest request) {
        User user = getCurrentUser();

        QnaQuestion question = QnaQuestion.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .createdBy(user)
                .build();

        if (request.getTags() != null) {
            question.setTagList(request.getTags());
        }

        question = questionRepository.save(question);

        if (request.getHyperlinks() != null) {
            for (QnaQuestionRequest.HyperlinkRequest link : request.getHyperlinks()) {
                QnaHyperlink hyperlink = QnaHyperlink.builder()
                        .question(question)
                        .url(link.getUrl())
                        .title(link.getTitle())
                        .build();
                question.getHyperlinks().add(hyperlink);
            }
            question = questionRepository.save(question);
        }

        return mapToResponse(question, false);
    }

    @Transactional
    public QnaAnswerResponse createAnswer(QnaAnswerRequest request) {
        User user = getCurrentUser();

        QnaQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QnaAnswer answer = QnaAnswer.builder()
                .question(question)
                .content(request.getContent())
                .createdBy(user)
                .build();

        answer = answerRepository.save(answer);

        if (request.getHyperlinks() != null) {
            for (QnaQuestionRequest.HyperlinkRequest link : request.getHyperlinks()) {
                QnaHyperlink hyperlink = QnaHyperlink.builder()
                        .answer(answer)
                        .url(link.getUrl())
                        .title(link.getTitle())
                        .build();
                answer.getHyperlinks().add(hyperlink);
            }
            answer = answerRepository.save(answer);
        }

        return mapAnswerToResponse(answer);
    }

    @Transactional
    public QnaCommentResponse createComment(QnaCommentRequest request) {
        User user = getCurrentUser();

        QnaAnswer answer = answerRepository.findById(request.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        QnaComment comment = QnaComment.builder()
                .answer(answer)
                .content(request.getContent())
                .createdBy(user)
                .build();

        comment = commentRepository.save(comment);
        return mapCommentToResponse(comment);
    }

    @Transactional
    public AttachmentResponse uploadAttachment(UUID questionId, UUID answerId, MultipartFile file) throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = getCurrentUser();
        String mongoFileId = gridFsFileService.uploadFile(file, ModuleType.QNA, questionId != null ? questionId : answerId, user.getId());
        String fileUrl = "/api/qna/attachments/" + mongoFileId + "/download";

        QnaAttachment attachment = QnaAttachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .mongoFileId(mongoFileId)
                .fileUrl(fileUrl)
                .uploadedBy(username)
                .build();

        if (questionId != null) {
            QnaQuestion question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));
            attachment.setQuestion(question);
        } else if (answerId != null) {
            QnaAnswer answer = answerRepository.findById(answerId)
                    .orElseThrow(() -> new RuntimeException("Answer not found"));
            attachment.setAnswer(answer);
        }

        attachment = attachmentRepository.save(attachment);

        return AttachmentResponse.builder()
                .id(attachment.getId().toString())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .fileUrl(attachment.getFileUrl())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<QnaQuestionResponse> getQuestions(String filter, String tag, Pageable pageable) {
        Page<QnaQuestion> questions;

        if ("unanswered".equals(filter)) {
            questions = questionRepository.findUnanswered(pageable);
        } else if ("active".equals(filter)) {
            questions = questionRepository.findMostActive(pageable);
        } else if (tag != null && !tag.isEmpty()) {
            questions = questionRepository.findByTag(tag, pageable);
        } else {
            questions = questionRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return questions.map(q -> mapToResponse(q, false));
    }

    @Transactional
    public QnaQuestionResponse getQuestion(UUID id) {
        QnaQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setViewCount(question.getViewCount() + 1);
        questionRepository.save(question);

        return mapToResponse(question, true);
    }

    @Transactional(readOnly = true)
    public Page<QnaQuestionResponse> searchQuestions(String keyword, Pageable pageable) {
        return questionRepository.search(keyword, pageable)
                .map(q -> mapToResponse(q, false));
    }

    @Transactional
    public void upvoteQuestion(UUID id) {
        QnaQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setUpvotes(question.getUpvotes() + 1);
        questionRepository.save(question);
    }

    @Transactional
    public void upvoteAnswer(UUID id) {
        QnaAnswer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        answer.setUpvotes(answer.getUpvotes() + 1);
        answerRepository.save(answer);
    }

    @Transactional
    public void acceptAnswer(UUID answerId) {
        QnaAnswer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));

        answer.getQuestion().getAnswers().forEach(a -> a.setIsAccepted(false));
        answer.setIsAccepted(true);
        answer.getQuestion().setIsResolved(true);

        answerRepository.save(answer);
        questionRepository.save(answer.getQuestion());
    }

    @Transactional
    public void deleteQuestion(UUID id) {
        QnaQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        for (QnaAttachment attachment : question.getAttachments()) {
            if (attachment.getMongoFileId() != null) {
                gridFsFileService.deleteFile(attachment.getMongoFileId());
            }
        }

        questionRepository.delete(question);
    }

    private QnaQuestionResponse mapToResponse(QnaQuestion question, boolean includeAnswers) {
        QnaQuestionResponse response = QnaQuestionResponse.builder()
                .id(question.getId().toString())
                .title(question.getTitle())
                .content(question.getContent())
                .tags(question.getTagList())
                .viewCount(question.getViewCount())
                .upvotes(question.getUpvotes())
                .isResolved(question.getIsResolved())
                .answerCount(question.getAnswers().size())
                .createdBy(mapUserSummary(question.getCreatedBy()))
                .createdAt(question.getCreatedAt())
                .updatedAt(question.getUpdatedAt())
                .attachments(question.getAttachments().stream()
                        .map(this::mapAttachmentToResponse)
                        .collect(Collectors.toList()))
                .hyperlinks(question.getHyperlinks().stream()
                        .map(this::mapHyperlinkToResponse)
                        .collect(Collectors.toList()))
                .build();

        if (includeAnswers) {
            response.setAnswers(question.getAnswers().stream()
                    .map(this::mapAnswerToResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private QnaAnswerResponse mapAnswerToResponse(QnaAnswer answer) {
        return QnaAnswerResponse.builder()
                .id(answer.getId().toString())
                .questionId(answer.getQuestion().getId().toString())
                .content(answer.getContent())
                .upvotes(answer.getUpvotes())
                .isAccepted(answer.getIsAccepted())
                .createdBy(mapUserSummary(answer.getCreatedBy()))
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .attachments(answer.getAttachments().stream()
                        .map(this::mapAttachmentToResponse)
                        .collect(Collectors.toList()))
                .hyperlinks(answer.getHyperlinks().stream()
                        .map(this::mapHyperlinkToResponse)
                        .collect(Collectors.toList()))
                .comments(answer.getComments().stream()
                        .map(this::mapCommentToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private QnaCommentResponse mapCommentToResponse(QnaComment comment) {
        return QnaCommentResponse.builder()
                .id(comment.getId().toString())
                .answerId(comment.getAnswer().getId().toString())
                .content(comment.getContent())
                .createdBy(mapUserSummary(comment.getCreatedBy()))
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private UserSummary mapUserSummary(User user) {
        if (user == null) return null;
        return UserSummary.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .build();
    }

    private AttachmentResponse mapAttachmentToResponse(QnaAttachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId().toString())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .fileUrl(attachment.getFileUrl())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }

    private HyperlinkResponse mapHyperlinkToResponse(QnaHyperlink hyperlink) {
        return HyperlinkResponse.builder()
                .id(hyperlink.getId().toString())
                .url(hyperlink.getUrl())
                .title(hyperlink.getTitle())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
