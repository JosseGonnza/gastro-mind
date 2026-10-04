package com.gastromind.infrastructure.web.supplier;

import com.gastromind.application.supplier.CreateSupplier;
import com.gastromind.application.supplier.GetSupplier;
import com.gastromind.application.supplier.ListSuppliers;
import com.gastromind.domain.entity.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final CreateSupplier createSupplier;
    private final GetSupplier getSupplier;
    private final ListSuppliers listSuppliers;

    public SupplierController(CreateSupplier createSupplier, GetSupplier getSupplier, ListSuppliers listSuppliers) {
        this.createSupplier = createSupplier;
        this.getSupplier = getSupplier;
        this.listSuppliers = listSuppliers;
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@RequestBody CreateSupplierRequest request) {
        Supplier supplier = createSupplier.execute(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(supplier.getId())
                .toUri();
        return ResponseEntity.created(location).body(SupplierResponse.from(supplier));
    }

    @GetMapping
    public List<SupplierResponse> list() {
        return listSuppliers.execute().stream()
                .map(SupplierResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public SupplierResponse getById(@PathVariable UUID id) {
        return SupplierResponse.from(getSupplier.execute(id));
    }
}
