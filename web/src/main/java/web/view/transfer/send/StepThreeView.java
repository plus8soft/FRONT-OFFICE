/*
 * Copyright AxiomaSoft LLC (d/b/a Plus8Soft)
 */

package web.view.transfer.send;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.primefaces.component.datatable.DataTable;
import web.entity.dict.PaymentSystemName;

@Getter
@Setter
@Log4j2
public class StepThreeView implements Serializable {

    private PaymentTransfer paymentTransfer;

    private PaymentPointFilter filter;

    private PaymentPointModel model;

    public void init(PaymentPointModel paymentPointModel, PaymentTransfer paymentTransfer) {
        this.paymentTransfer = paymentTransfer;
        this.paymentTransfer.setPaymentPoint(null);
        if (PaymentSystemName.MONEY_TRANSFER.equals(this.paymentTransfer.getPaymentSystem().getId())) {
            this.paymentTransfer.setAddressed(false);
        }
        filter.setPaymentSystem(paymentTransfer.getPaymentSystem());
        filter.setCurrency(paymentTransfer.getTransferCurrency());
        filter.setRegion(paymentTransfer.getDestinationRegion());
        filter.setCountry(paymentTransfer.getDestinationCountry());
        model = paymentPointModel;
        model.setFilter(filter.clone());
    }

    public void updateFilter() {
        paymentTransfer.setPaymentPoint(null);
        model.setFilter(filter.clone());
        model.reset();
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":content:paymentPoints")).reset();
    }

    public void clearFilter() {
        if (this.paymentTransfer.isAddressed()) {
            filter.setAddress(null);
            updateFilter();
        }
    }
}
