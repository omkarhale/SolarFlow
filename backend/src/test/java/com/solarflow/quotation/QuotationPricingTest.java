package com.solarflow.quotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.solarflow.common.exception.BadRequestException;
import com.solarflow.quotation.domain.QuotationPricing;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuotationPricingTest {

  @Test
  void appliesDiscountBeforeTax() {
    var result = QuotationPricing.price(
        List.of(new QuotationPricing.Line(new BigDecimal("6"), new BigDecimal("14500"))),
        new BigDecimal("2000"),
        new BigDecimal("18")
    );
    assertThat(result.subtotal()).isEqualByComparingTo("87000.00");
    assertThat(result.taxAmount()).isEqualByComparingTo("15300.00");
    assertThat(result.total()).isEqualByComparingTo("100300.00");
  }

  @Test
  void rejectsADiscountAboveTheSubtotal() {
    assertThatThrownBy(() -> QuotationPricing.price(
        List.of(new QuotationPricing.Line(BigDecimal.ONE, new BigDecimal("100"))),
        new BigDecimal("150"),
        BigDecimal.ZERO
    )).isInstanceOf(BadRequestException.class);
  }
}
