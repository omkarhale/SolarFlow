package com.solarflow.quotation.domain;

import com.solarflow.common.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class QuotationPricing {

  private QuotationPricing() {
  }

  public record Line(BigDecimal quantity, BigDecimal unitPrice) {
  }

  public record Result(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal taxRate, BigDecimal taxAmount, BigDecimal total) {
  }

  public static Result price(List<Line> lines, BigDecimal discountAmount, BigDecimal taxRate) {
    BigDecimal subtotal = BigDecimal.ZERO;
    for (Line line : lines) {
      if (line.quantity() == null || line.quantity().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("Each line needs a quantity greater than zero");
      }
      if (line.unitPrice() == null || line.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
        throw new BadRequestException("Unit price cannot be negative");
      }
      subtotal = subtotal.add(line.quantity().multiply(line.unitPrice()));
    }
    subtotal = money(subtotal);
    BigDecimal discount = money(discountAmount == null ? BigDecimal.ZERO : discountAmount);
    BigDecimal rate = money(taxRate == null ? BigDecimal.ZERO : taxRate);
    if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(subtotal) > 0) {
      throw new BadRequestException("Discount must be between zero and the subtotal");
    }
    if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("100")) > 0) {
      throw new BadRequestException("Tax rate must be between 0 and 100");
    }
    BigDecimal taxable = subtotal.subtract(discount);
    BigDecimal tax = money(taxable.multiply(rate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
    return new Result(subtotal, discount, rate, tax, money(taxable.add(tax)));
  }

  public static BigDecimal money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  public static BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitPrice) {
    return money(quantity.multiply(unitPrice));
  }
}
