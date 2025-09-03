package com.eCommerce.addressService;


import com.eCommerce.addressService.dto.AddressRequest;
import com.eCommerce.addressService.dto.AddressResponse;
import com.eCommerce.addressService.dto.ShippingAddressDTO;
import com.eCommerce.addressService.entity.Address;
import com.eCommerce.addressService.repository.AddressRepository;
import com.eCommerce.addressService.service.AddressManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressManagerTest {

    @Mock private AddressRepository repo;

    @InjectMocks private AddressManager svc;

    private Address a1;
    private Address a2;

    @BeforeEach
    void setUp() {
        a1 = new Address();
        a1.setId(1L);
        a1.setUserId(99L);
        a1.setFullName("Alice");
        a1.setPhone("111");
        a1.setLine1("Main");
        a1.setCity("Pune");
        a1.setState("MH");
        a1.setDefaultShipping(false);
        a1.setCreatedAt(LocalDateTime.now());

        a2 = new Address();
        a2.setId(2L);
        a2.setUserId(99L);
        a2.setFullName("Bob");
        a2.setPhone("222");
        a2.setLine1("Second");
        a2.setCity("Mumbai");
        a2.setState("MH");
        a2.setDefaultShipping(true);
        a2.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void listMine_mapsEntitiesToDtos() {
        when(repo.findByUserIdOrderByCreatedAtDesc(99L)).thenReturn(List.of(a1, a2));

        List<AddressResponse> out = svc.listMine(99L);

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getId()).isEqualTo(1L);
        assertThat(out.get(1).isDefaultShipping()).isTrue();
        verify(repo).findByUserIdOrderByCreatedAtDesc(99L);
    }

    @Test
    void getMine_returnsMappedDto() {
        when(repo.findByIdAndUserId(1L, 99L)).thenReturn(Optional.of(a1));

        AddressResponse out = svc.getMine(99L, 1L);

        assertThat(out.getFullName()).isEqualTo("Alice");
        verify(repo).findByIdAndUserId(1L, 99L);
    }

    @Test
    void create_setsDefaultAndClearsPreviousWhenRequested() {
        AddressRequest req = new AddressRequest();
        req.setFullName("New");
        req.setPhone("333");
        req.setLine1("Third");
        req.setCity("Pune");
        req.setState("MH");
        req.setDefaultShipping(true);

        // capture saved entity
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        when(repo.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AddressResponse out = svc.create(99L, req);

        verify(repo).clearDefaultShipping(99L);
        verify(repo).save(captor.capture());
        Address saved = captor.getValue();
        assertThat(saved.isDefaultShipping()).isTrue();
        assertThat(saved.getUserId()).isEqualTo(99L);
        assertThat(out.getId()).isEqualTo(10L);
    }

    @Test
    void update_appliesFields_andCanSetDefault() {
        when(repo.findByIdAndUserId(1L, 99L)).thenReturn(Optional.of(a1));
        when(repo.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressRequest req = new AddressRequest();
        req.setFullName("Alice Updated");
        req.setPhone("999");
        req.setLine1("New Ln");
        req.setCity("Pune");
        req.setState("MH");
        req.setDefaultShipping(true);

        AddressResponse out = svc.update(99L, 1L, req);

        verify(repo).clearDefaultShipping(99L);
        assertThat(out.getFullName()).isEqualTo("Alice Updated");
        assertThat(out.isDefaultShipping()).isTrue();
    }

    @Test
    void delete_removesOwnedAddress() {
        when(repo.findByIdAndUserId(1L, 99L)).thenReturn(Optional.of(a1));

        svc.delete(99L, 1L);

        verify(repo).delete(a1);
    }

    @Test
    void setDefault_clearsOthersAndSetsFlag() {
        when(repo.findByIdAndUserId(2L, 99L)).thenReturn(Optional.of(a2));
        when(repo.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse out = svc.setDefault(99L, 2L);

        verify(repo).clearDefaultShipping(99L);
        assertThat(out.isDefaultShipping()).isTrue();
    }

    @Test
    void toShipping_buildsMinimalDto() {
        ShippingAddressDTO s = svc.toShipping(a1);
        assertThat(s.getFullName()).isEqualTo("Alice");
        assertThat(s.getLine1()).isEqualTo("Main");
        assertThat(s.getPhone()).isEqualTo("111");
    }

    @Test
    void adminListAll_returnsPagedDtos() {
        when(repo.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(a1, a2)));

        Page<AddressResponse> page = svc.adminListAll(0, 20);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void adminListByUser_filtersByUser() {
        when(repo.findByUserId(eq(99L), any()))
                .thenReturn(new PageImpl<>(List.of(a1)));

        Page<AddressResponse> page = svc.adminListByUser(99L, 0, 10);

        assertThat(page.getTotalElements()).isEqualTo(1);
        verify(repo).findByUserId(eq(99L), any());
    }

    @Test
    void adminGet_mapsDto() {
        when(repo.findById(2L)).thenReturn(Optional.of(a2));

        AddressResponse out = svc.adminGet(2L);

        assertThat(out.getId()).isEqualTo(2L);
        assertThat(out.isDefaultShipping()).isTrue();
    }

    @Test
    void adminCreate_respectsDefaultShippingFlag() {
        AddressRequest req = new AddressRequest();
        req.setFullName("X");
        req.setPhone("1");
        req.setLine1("L");
        req.setCity("C");
        req.setState("S");
        req.setDefaultShipping(true);

        when(repo.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0); a.setId(77L); return a;
        });

        AddressResponse out = svc.adminCreate(42L, req);

        verify(repo).clearDefaultShipping(42L);
        assertThat(out.getId()).isEqualTo(77L);
        assertThat(out.isDefaultShipping()).isTrue();
    }

    @Test
    void adminUpdate_updatesAndCanSetDefault() {
        when(repo.findById(2L)).thenReturn(Optional.of(a2));
        when(repo.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressRequest req = new AddressRequest();
        req.setFullName("Z");
        req.setPhone("9");
        req.setLine1("New");
        req.setCity("City");
        req.setState("ST");
        req.setDefaultShipping(true);

        AddressResponse out = svc.adminUpdate(2L, req);

        verify(repo).clearDefaultShipping(a2.getUserId());
        assertThat(out.getFullName()).isEqualTo("Z");
        assertThat(out.isDefaultShipping()).isTrue();
    }

    @Test
    void adminDelete_removesById() {
        when(repo.findById(1L)).thenReturn(Optional.of(a1));

        svc.adminDelete(1L);

        verify(repo).delete(a1);
    }

    @Test
    void adminSetDefault_updatesDefault() {
        when(repo.findById(2L)).thenReturn(Optional.of(a2));
        when(repo.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponse out = svc.adminSetDefault(2L);

        verify(repo).clearDefaultShipping(a2.getUserId());
        assertThat(out.isDefaultShipping()).isTrue();
    }
}
