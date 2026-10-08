package com.bluesky.pos_system.controllers;

import com.bluesky.pos_system.domains.StoreStatus;
import com.bluesky.pos_system.mappers.StoreMapper;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.StoreDTO;
import com.bluesky.pos_system.payload.responses.ApiResponse;
import com.bluesky.pos_system.services.StoreService;
import com.bluesky.pos_system.services.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/stores", "/api/stores"})
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StoreController {
    StoreService storeService;
    UserService userService;

    @PostMapping
    public ResponseEntity<StoreDTO> createStore(@RequestBody StoreDTO storeDTO, @RequestHeader("Authorization") String token) {
        User user = userService.getUserFromJwtToken(token);
        StoreDTO response = storeService.createStore(storeDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStore() {
        List<StoreDTO> response = storeService.getAllStores();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/admin")
    public ResponseEntity<StoreDTO> getStoreByAdmin() {
        Store response = storeService.getStoreByAdmin();
        return ResponseEntity.status(HttpStatus.OK).body(StoreMapper.toDTO(response));
    }

    @GetMapping("/employee")
    public ResponseEntity<StoreDTO> getStoreByEmployee() {
        StoreDTO response = storeService.getStoreByEmployee();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable UUID id) {
        StoreDTO response = storeService.getStoreById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreDTO> updateStore(@RequestBody StoreDTO storeDTO, @PathVariable UUID id) {
        StoreDTO response = storeService.updateStore(storeDTO, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStore(@PathVariable UUID id) {
        storeService.deleteStore(id);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Xóa cửa hàng thành công");
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PutMapping("/{id}/moderate")
    public ResponseEntity<StoreDTO> moderateStore(@RequestBody StoreStatus storeStatus, @PathVariable UUID id) {
        StoreDTO response = storeService.moderateStore(id, storeStatus);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
