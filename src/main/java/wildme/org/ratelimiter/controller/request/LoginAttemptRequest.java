package wildme.org.ratelimiter.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginAttemptRequest {
    @NotBlank
    private String ip;
    private String cookieId;
    @NotBlank
    private String username;
}
