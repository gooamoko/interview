package ru.gooamoko;

import java.math.BigDecimal;
import java.util.Date;

public class PaymentInfo {
    private Date paymentDate;
    private BigDecimal paymentAmount;

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
}
