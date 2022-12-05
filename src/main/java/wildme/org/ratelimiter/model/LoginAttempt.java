package wildme.org.ratelimiter.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("LOGIN_ATTEMPT")
public class LoginAttempt {

    @Id
    private UUID id;

    private String ip;
    private String cookieId;
    private String username;

    @CreatedDate
    private LocalDateTime createdAt;

}
