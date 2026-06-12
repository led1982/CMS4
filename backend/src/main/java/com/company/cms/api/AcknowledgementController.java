package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.Acknowledgement;
import com.company.cms.api.dto.CmsDtos.AcknowledgementReport;
import com.company.cms.api.dto.CmsDtos.ContentSummary;
import com.company.cms.api.dto.CmsDtos.UserAcknowledgementItem;
import com.company.cms.api.dto.CmsMapper;
import com.company.cms.application.acknowledgement.AcknowledgementService;
import com.company.cms.security.CmsSecurityContext;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AcknowledgementController {
    private final AcknowledgementService acknowledgementService;
    private final CmsMapper mapper;

    public AcknowledgementController(AcknowledgementService acknowledgementService, CmsMapper mapper) {
        this.acknowledgementService = acknowledgementService;
        this.mapper = mapper;
    }

    @PostMapping("/content/{contentId}/acknowledgements")
    public ResponseEntity<Acknowledgement> acknowledge(@PathVariable String contentId, CmsSecurityContext context) {
        var acknowledgement = acknowledgementService.acknowledge(contentId, context);
        return ResponseEntity.created(URI.create("/api/v1/content/" + contentId + "/acknowledgements/" + acknowledgement.id()))
            .body(mapper.toAcknowledgement(acknowledgement));
    }

    @PostMapping("/notices/{noticeId}/acknowledgements")
    public ResponseEntity<Acknowledgement> acknowledgeNotice(@PathVariable String noticeId, CmsSecurityContext context) {
        var acknowledgement = acknowledgementService.acknowledge(noticeId, context);
        return ResponseEntity.created(URI.create("/api/v1/notices/" + noticeId + "/acknowledgements/" + acknowledgement.id()))
            .body(mapper.toAcknowledgement(acknowledgement));
    }

    @GetMapping("/me/acknowledgements")
    public List<UserAcknowledgementItem> myAcknowledgements(@RequestParam(required = false) String status, CmsSecurityContext context) {
        return acknowledgementService.myAcknowledgements(status, context);
    }

    @GetMapping("/notices/pending")
    public List<ContentSummary> pendingNotices(CmsSecurityContext context) {
        return acknowledgementService.myAcknowledgements("PENDING", context).stream()
            .map(UserAcknowledgementItem::content)
            .toList();
    }

    @GetMapping("/editor/acknowledgements")
    public AcknowledgementReport report(
        @RequestParam String contentId,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String status,
        CmsSecurityContext context
    ) {
        return acknowledgementService.report(contentId, department, status, context);
    }

    @GetMapping("/notices/{noticeId}/acknowledgements/report")
    public AcknowledgementReport noticeReport(
        @PathVariable String noticeId,
        @RequestParam(required = false) String department,
        @RequestParam(required = false) String status,
        CmsSecurityContext context
    ) {
        return acknowledgementService.report(noticeId, department, status, context);
    }
}
