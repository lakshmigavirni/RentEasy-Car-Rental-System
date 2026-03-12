package com.CustomerService.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class AadhaarVerificationController {

    @PostMapping("/aadhaar/verify")
    public ResponseEntity<?> verifyAadhaar(@RequestBody AadhaarVerificationRequest request) {
        try {
            // MOCK VERIFICATION LOGIC
            // Validates that required fields are present and Aadhaar number is 12 digits.

            boolean aadhaarNumberMatch = request.entered_aadhaar_number != null
                    && request.entered_aadhaar_number.trim().replaceAll("\\s+", "").matches("\\d{12}");

            boolean nameMatch = request.customerName != null && !request.customerName.trim().isEmpty();
            boolean dobMatch = request.dateOfBirth != null && !request.dateOfBirth.trim().isEmpty();
            boolean faceMatch = request.aadhaar_image_url != null && request.selfie_image_url != null;

            String status = (aadhaarNumberMatch && nameMatch && dobMatch && faceMatch) ? "verified" : "rejected";
            String message = status.equals("verified")
                    ? "Aadhaar number, name, DOB, and face all match (MOCKED). Status: verified."
                    : "Aadhaar verification failed (MOCKED). " +
                      (!aadhaarNumberMatch ? "Invalid Aadhaar number (must be 12 digits). " : "") +
                      (!nameMatch ? "Missing name. " : "") +
                      (!dobMatch ? "Missing date of birth. " : "") +
                      (!faceMatch ? "Missing images. " : "");

            org.json.JSONObject result = new org.json.JSONObject();
            result.put("aadhaarNumberMatch", aadhaarNumberMatch);
            result.put("nameMatch", nameMatch);
            result.put("dobMatch", dobMatch);
            result.put("faceMatch", faceMatch);
            result.put("status", status);
            result.put("message", message);

            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("{\"status\":\"error\",\"message\":\"Aadhaar verification failed: " + e.getMessage() + "\"}");
        }
    }

    static class AadhaarVerificationRequest {
        public String aadhaar_image_url;
        public String entered_aadhaar_number;
        public String selfie_image_url;
        public String customerName;
        public String dateOfBirth;
    }
}
