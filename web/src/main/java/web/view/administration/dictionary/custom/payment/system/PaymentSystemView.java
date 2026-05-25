/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.administration.dictionary.custom.payment.system;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import web.entity.dict.DictionaryParameter;

@Getter
@Setter
@Log4j2
public class PaymentSystemView implements Serializable {

    private PaymentSystemModel model;

    private DictionaryParameter dictionary;
}
