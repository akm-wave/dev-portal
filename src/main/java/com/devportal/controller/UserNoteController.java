package com.devportal.controller;

import com.devportal.dto.request.UserNoteRequest;
import com.devportal.dto.response.ApiResponse;
import com.devportal.dto.response.PagedResponse;
import com.devportal.dto.response.UserNoteResponse;
import com.devportal.service.UserNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workspace/notes")
@RequiredArgsConstructor
public class UserNoteController {

    private final UserNoteService noteService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserNoteResponse>>> getMyNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean archived) {
        return ResponseEntity.ok(ApiResponse.success(noteService.getMyNotes(page, size, search, archived)));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<UserNoteResponse>>> getPinnedNotes() {
        return ResponseEntity.ok(ApiResponse.success(noteService.getPinnedNotes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserNoteResponse>> getNoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(noteService.getNoteById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserNoteResponse>> createNote(@Valid @RequestBody UserNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(noteService.createNote(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserNoteResponse>> updateNote(
            @PathVariable UUID id,
            @Valid @RequestBody UserNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(noteService.updateNote(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/toggle-pin")
    public ResponseEntity<ApiResponse<UserNoteResponse>> togglePin(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(noteService.togglePin(id)));
    }

    @PostMapping("/{id}/toggle-archive")
    public ResponseEntity<ApiResponse<UserNoteResponse>> toggleArchive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(noteService.toggleArchive(id)));
    }
}
