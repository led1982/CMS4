package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.Bookmark;
import com.company.cms.api.dto.CmsDtos.BookmarkCreateRequest;
import com.company.cms.application.portal.BookmarkService;
import com.company.cms.security.CmsSecurityContext;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<Bookmark> list(CmsSecurityContext context) {
        return bookmarkService.list(context);
    }

    @PostMapping
    public ResponseEntity<Bookmark> create(@Valid @RequestBody BookmarkCreateRequest request, CmsSecurityContext context) {
        var bookmark = bookmarkService.create(request.contentId(), context);
        return ResponseEntity.created(URI.create("/api/v1/bookmarks/" + request.contentId())).body(bookmark);
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> delete(@PathVariable String contentId, CmsSecurityContext context) {
        bookmarkService.delete(contentId, context);
        return ResponseEntity.noContent().build();
    }
}
