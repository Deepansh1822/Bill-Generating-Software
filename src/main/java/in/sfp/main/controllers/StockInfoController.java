package in.sfp.main.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import in.sfp.main.models.StockInfo;
import in.sfp.main.service.serviceimpl.StockInfoServiceImpl;

@RestController
@RequestMapping("/billing-app/api")
public class StockInfoController {

    @Autowired
    private StockInfoServiceImpl stockInfoService;

    @PostMapping("/saveStock")
    public ResponseEntity<StockInfo> saveStock(@RequestBody StockInfo stock) {
        StockInfo savedStock = stockInfoService.saveStock(stock);
        return new ResponseEntity<>(savedStock, HttpStatus.CREATED);
    }

    @GetMapping("/getAllStocks")
    public ResponseEntity<List<StockInfo>> getAllStocks() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("CLIENT");

        List<StockInfo> stocks = stockInfoService.getAllStocksByUser(email, role);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/getStockById/{id}")
    public ResponseEntity<StockInfo> getStockById(@PathVariable Long id) {
        StockInfo stock = stockInfoService.getStockById(id);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/getStockByName/{itemName}")
    public ResponseEntity<StockInfo> getStockByName(@PathVariable String itemName) {
        StockInfo stock = stockInfoService.getStockByName(itemName);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    @DeleteMapping("/deleteStock/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockInfoService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/updateStock/{id}")
    public ResponseEntity<StockInfo> updateStock(@PathVariable Long id, @RequestBody StockInfo stock) {
        StockInfo updatedStock = stockInfoService.updateStock(id, stock);
        if (updatedStock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStock);
    }

}
