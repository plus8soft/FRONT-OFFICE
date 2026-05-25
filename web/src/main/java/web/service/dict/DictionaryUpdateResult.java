/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.service.dict;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import web.entity.dict.UpdateResult;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DictionaryUpdateResult {

    private UpdateResult updateResult;

    private String message;

    private Long version;

    private LocalDateTime updateDate;
}
