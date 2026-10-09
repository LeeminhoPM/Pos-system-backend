package com.bluesky.pos_system;

import com.bluesky.pos_system.models.Branch;
import com.bluesky.pos_system.models.Store;
import com.bluesky.pos_system.models.User;
import com.bluesky.pos_system.payload.dto.BranchDTO;
import com.bluesky.pos_system.repositories.BranchRepository;
import com.bluesky.pos_system.repositories.StoreRepository;
import com.bluesky.pos_system.services.UserService;
import com.bluesky.pos_system.services.impl.BranchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private BranchServiceImpl branchService;

    private User adminUser;
    private Store store;
    private Branch branch;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(UUID.randomUUID())
                .fullName("Admin Test")
                .email("admin@test.com")
                .build();

        store = Store.builder()
                .id(UUID.randomUUID())
                .branch("Cửa hàng Tổng")
                .storeAdmin(adminUser)
                .build();

        branch = Branch.builder()
                .id(UUID.randomUUID())
                .name("Chi nhánh Cầu Giấy")
                .address("123 Cầu Giấy, Hà Nội")
                .phone("0987654321")
                .store(store)
                .build();
    }

    @Test
    @DisplayName("Tạo chi nhánh mới thành công liên kết với store của admin")
    void testCreateBranch_Success() {
        BranchDTO dto = BranchDTO.builder()
                .name("Chi nhánh Ba Đình")
                .address("45 Kim Mã, Hà Nội")
                .phone("0912345678")
                .build();

        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(storeRepository.findByStoreAdminId(adminUser.getId())).thenReturn(store);
        when(branchRepository.save(any(Branch.class))).thenAnswer(invocation -> {
            Branch b = invocation.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BranchDTO created = branchService.createBranch(dto);

        assertNotNull(created);
        assertEquals("Chi nhánh Ba Đình", created.getName());
        verify(branchRepository, times(1)).save(any(Branch.class));
    }

    @Test
    @DisplayName("Lấy thông tin chi nhánh theo ID thành công")
    void testGetBranchById_Success() {
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));

        BranchDTO result = branchService.getBranchById(branch.getId());

        assertNotNull(result);
        assertEquals("Chi nhánh Cầu Giấy", result.getName());
        assertEquals("123 Cầu Giấy, Hà Nội", result.getAddress());
    }

    @Test
    @DisplayName("Ném ngoại lệ RuntimeException khi không tìm thấy chi nhánh")
    void testGetBranchById_NotFound() {
        UUID randomId = UUID.randomUUID();
        when(branchRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> branchService.getBranchById(randomId));
    }

    @Test
    @DisplayName("Lấy danh sách tất cả các chi nhánh theo Store ID")
    void testGetBranchesByStoreId() {
        when(branchRepository.findByStoreId(store.getId())).thenReturn(List.of(branch));

        List<BranchDTO> list = branchService.getAllBranchesByStoreId(store.getId());

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Chi nhánh Cầu Giấy", list.get(0).getName());
    }
}
