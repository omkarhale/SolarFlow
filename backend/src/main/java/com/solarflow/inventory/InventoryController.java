package com.solarflow.inventory;

import com.solarflow.common.exception.BadRequestException;
import com.solarflow.common.exception.NotFoundException;
import com.solarflow.common.security.CurrentUser;
import com.solarflow.inventory.domain.Product;
import com.solarflow.inventory.domain.ProductRepository;
import com.solarflow.inventory.domain.StockMovement;
import com.solarflow.inventory.domain.StockMovementRepository;
import com.solarflow.quotation.domain.QuotationPricing;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

  private final ProductRepository products;
  private final StockMovementRepository movements;
  private final CurrentUser currentUser;

  public InventoryController(ProductRepository products, StockMovementRepository movements, CurrentUser currentUser) {
    this.products = products;
    this.movements = movements;
    this.currentUser = currentUser;
  }

  @GetMapping("/products")
  @Transactional(readOnly = true)
  public List<ProductView> products() {
    return products.findByCompanyIdOrderByNameAsc(companyId()).stream().map(ProductView::from).toList();
  }

  @PostMapping("/products")
  @Transactional
  public ProductView create(@Valid @RequestBody ProductInput input) {
    Product product = new Product();
    product.setCompanyId(companyId());
    apply(product, input);
    product.setStockQuantity(input.stockQuantity() == null ? BigDecimal.ZERO : input.stockQuantity());
    return ProductView.from(products.save(product));
  }

  @GetMapping("/products/{id}/movements")
  @Transactional(readOnly = true)
  public List<MovementView> movements(@PathVariable UUID id) {
    require(id);
    return movements.findByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId(), id).stream().map(MovementView::from).toList();
  }

  @PostMapping("/products/{id}/movements")
  @Transactional
  public ProductView move(@PathVariable UUID id, @Valid @RequestBody MovementInput input) {
    Product product = require(id);
    BigDecimal quantity = input.quantity();
    if (quantity.compareTo(BigDecimal.ZERO) == 0) {
      throw new BadRequestException("Quantity cannot be zero");
    }
    BigDecimal next = switch (input.movementType()) {
      case IN -> product.getStockQuantity().add(quantity.abs());
      case OUT, ALLOCATION -> product.getStockQuantity().subtract(quantity.abs());
      case ADJUSTMENT -> product.getStockQuantity().add(quantity);
    };
    if (next.compareTo(BigDecimal.ZERO) < 0) {
      throw new BadRequestException("That movement would take stock below zero");
    }
    BigDecimal storedQuantity = input.movementType() == StockMovement.Type.IN ? quantity.abs() : quantity;
    if (input.movementType() == StockMovement.Type.OUT || input.movementType() == StockMovement.Type.ALLOCATION) {
      storedQuantity = quantity.abs().negate();
    }
    StockMovement movement = new StockMovement();
    movement.setCompanyId(product.getCompanyId());
    movement.setProductId(product.getId());
    movement.setMovementType(input.movementType());
    movement.setQuantity(storedQuantity);
    movement.setNotes(input.notes());
    movement.setCreatedBy(currentUser.require().id());
    movements.save(movement);
    product.setStockQuantity(QuotationPricing.money(next));
    return ProductView.from(product);
  }

  private void apply(Product product, ProductInput input) {
    product.setSku(input.sku().trim());
    product.setName(input.name().trim());
    product.setCategory(input.category());
    product.setBrand(input.brand());
    product.setModel(input.model());
    product.setUnit(input.unit().trim());
    product.setUnitPrice(QuotationPricing.money(input.unitPrice()));
    product.setWattage(input.wattage());
    product.setWarrantyYears(input.warrantyYears());
    product.setReorderLevel(input.reorderLevel() == null ? BigDecimal.ZERO : input.reorderLevel());
    product.setActive(input.active() == null || input.active());
  }

  private Product require(UUID id) {
    return products.findByIdAndCompanyId(id, companyId()).orElseThrow(NotFoundException::new);
  }

  private UUID companyId() {
    return currentUser.require().companyId();
  }

  public record ProductInput(
      @NotBlank @Size(max = 64) String sku,
      @NotBlank @Size(max = 160) String name,
      @NotNull Product.Category category,
      @Size(max = 80) String brand,
      @Size(max = 80) String model,
      @NotBlank @Size(max = 24) String unit,
      @NotNull BigDecimal unitPrice,
      Integer wattage,
      Integer warrantyYears,
      BigDecimal stockQuantity,
      BigDecimal reorderLevel,
      Boolean active
  ) {
  }

  public record MovementInput(@NotNull StockMovement.Type movementType, @NotNull BigDecimal quantity, @Size(max = 500) String notes) {
  }

  public record ProductView(
      UUID id, String sku, String name, Product.Category category, String brand, String model, String unit,
      BigDecimal unitPrice, Integer wattage, Integer warrantyYears, BigDecimal stockQuantity, BigDecimal reorderLevel,
      boolean active, boolean lowStock
  ) {
    static ProductView from(Product product) {
      boolean low = product.getStockQuantity().compareTo(product.getReorderLevel()) <= 0;
      return new ProductView(
          product.getId(), product.getSku(), product.getName(), product.getCategory(), product.getBrand(), product.getModel(),
          product.getUnit(), product.getUnitPrice(), product.getWattage(), product.getWarrantyYears(), product.getStockQuantity(),
          product.getReorderLevel(), product.isActive(), low
      );
    }
  }

  public record MovementView(UUID id, StockMovement.Type movementType, BigDecimal quantity, String notes) {
    static MovementView from(StockMovement movement) {
      return new MovementView(movement.getId(), movement.getMovementType(), movement.getQuantity(), movement.getNotes());
    }
  }
}
