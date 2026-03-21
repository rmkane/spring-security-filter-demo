package org.acme.demo.api;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.acme.demo.model.AppInfo;
import org.acme.demo.security.authentication.MtlsClientPrincipal;
import org.acme.demo.service.DefaultService;

@RestController
@RequestMapping("/api/default")
@RequiredArgsConstructor
@PreAuthorize("@apiMethodSecurityExpressions.isMtlsEnforcementDisabled() or hasRole('MTLS_CLIENT')")
public class DefaultController {

    private final DefaultService defaultService;

    @GetMapping("/info")
    public ResponseEntity<AppInfo> fetchInfo() {
        return ResponseEntity.ok(defaultService.fetchInfo());
    }

    /**
     * Confirms the mTLS-derived principal when {@code acme.security.headers.require-mtls-headers=true}.
     * With enforcement off, returns {@code anonymous}.
     */
    @GetMapping("/whoami")
    public ResponseEntity<Map<String, String>> whoami(@AuthenticationPrincipal Object principal) {
        if (principal instanceof MtlsClientPrincipal m) {
            return ResponseEntity.ok(Map.of(
                    "subjectDn", m.subjectDn(),
                    "issuerDn", m.issuerDn()
            ));
        }
        return ResponseEntity.ok(Map.of("subjectDn", "anonymous", "issuerDn", "anonymous"));
    }
}
