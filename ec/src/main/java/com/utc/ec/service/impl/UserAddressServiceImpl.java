package com.utc.ec.service.impl;

import com.utc.ec.dto.AddressDTO;
import com.utc.ec.entity.Address;
import com.utc.ec.entity.SiteUser;
import com.utc.ec.entity.UserAddress;
import com.utc.ec.entity.UserAddressId;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.repository.AddressRepository;
import com.utc.ec.repository.SiteUserRepository;
import com.utc.ec.repository.UserAddressRepository;
import com.utc.ec.service.UserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl implements UserAddressService {

    private final AddressRepository addressRepository;
    private final UserAddressRepository userAddressRepository;
    private final SiteUserRepository userRepository;

    @Override
    public List<AddressDTO> getMyAddresses(String username) {
        SiteUser user = getUser(username);
        List<UserAddress> links = userAddressRepository.findByUserId(user.getId());

        List<Integer> addressIds = links.stream()
                .map(UserAddress::getAddressId)
                .collect(Collectors.toList());

        return addressRepository.findAllById(addressIds).stream()
                .map(addr -> {
                    AddressDTO dto = toDto(addr);
                    // Tìm isDefault từ link
                    links.stream()
                            .filter(l -> l.getAddressId().equals(addr.getId()))
                            .findFirst()
                            .ifPresent(l -> dto.setIsDefault(
                                    l.getIsDefault() != null && l.getIsDefault() == 1));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDTO addAddress(String username, AddressDTO dto) {
        SiteUser user = getUser(username);

        // Tạo address
        Address addr = new Address();
        addr.setUnitNumber(dto.getUnitNumber());
        addr.setStreetNumber(dto.getStreetNumber());
        addr.setAddressLine1(dto.getAddressLine1());
        addr.setAddressLine2(dto.getAddressLine2());
        addr.setCity(dto.getCity());
        addr.setRegion(dto.getRegion());
        addr.setPostalCode(dto.getPostalCode());
        addr.setCountryId(dto.getCountryId());
        addr = addressRepository.save(addr);

        // Link user_address
        boolean isFirst = userAddressRepository.findByUserId(user.getId()).isEmpty();

        UserAddress link = new UserAddress();
        link.setUserId(user.getId());
        link.setAddressId(addr.getId());
        link.setIsDefault(isFirst ? 1 : 0); // Địa chỉ đầu tiên → mặc định
        userAddressRepository.save(link);

        AddressDTO result = toDto(addr);
        result.setIsDefault(isFirst);
        return result;
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(String username, Integer addressId, AddressDTO dto) {
        SiteUser user = getUser(username);

        // Kiểm tra địa chỉ thuộc về user
        if (!userAddressRepository.existsByUserIdAndAddressId(user.getId(), addressId)) {
            throw new ResourceNotFoundException("address.notBelongToUser", addressId);
        }

        Address addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address.notFound", addressId));

        addr.setUnitNumber(dto.getUnitNumber());
        addr.setStreetNumber(dto.getStreetNumber());
        addr.setAddressLine1(dto.getAddressLine1());
        addr.setAddressLine2(dto.getAddressLine2());
        addr.setCity(dto.getCity());
        addr.setRegion(dto.getRegion());
        addr.setPostalCode(dto.getPostalCode());
        addr.setCountryId(dto.getCountryId());

        return toDto(addressRepository.save(addr));
    }

    @Override
    @Transactional
    public void deleteAddress(String username, Integer addressId) {
        SiteUser user = getUser(username);

        UserAddressId linkId = new UserAddressId();
        linkId.setUserId(user.getId());
        linkId.setAddressId(addressId);

        if (!userAddressRepository.existsById(linkId)) {
            throw new ResourceNotFoundException("address.notBelongToUser", addressId);
        }

        UserAddress link = userAddressRepository.findById(linkId).orElse(null);
        userAddressRepository.deleteById(linkId);
        addressRepository.deleteById(addressId);

        // Nếu xóa cái mặc định → set cái đầu tiên còn lại làm mặc định
        if (link != null && link.getIsDefault() != null && link.getIsDefault() == 1) {
            userAddressRepository.findByUserId(user.getId()).stream()
                    .findFirst()
                    .ifPresent(first -> {
                        first.setIsDefault(1);
                        userAddressRepository.save(first);
                    });
        }
    }

    @Override
    @Transactional
    public AddressDTO setDefault(String username, Integer addressId) {
        SiteUser user = getUser(username);

        if (!userAddressRepository.existsByUserIdAndAddressId(user.getId(), addressId)) {
            throw new ResourceNotFoundException("address.notBelongToUser", addressId);
        }

        // Bỏ mặc định cũ
        userAddressRepository.findByUserId(user.getId()).forEach(link -> {
            if (link.getIsDefault() != null && link.getIsDefault() == 1) {
                link.setIsDefault(0);
                userAddressRepository.save(link);
            }
        });

        // Set mặc định mới
        UserAddressId linkId = new UserAddressId();
        linkId.setUserId(user.getId());
        linkId.setAddressId(addressId);
        userAddressRepository.findById(linkId).ifPresent(link -> {
            link.setIsDefault(1);
            userAddressRepository.save(link);
        });

        Address addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("address.notFound", addressId));
        AddressDTO result = toDto(addr);
        result.setIsDefault(true);
        return result;
    }

    // ==================== Helpers ====================

    private SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("auth.user.notFound"));
    }

    private AddressDTO toDto(Address addr) {
        AddressDTO dto = new AddressDTO();
        dto.setId(addr.getId());
        dto.setUnitNumber(addr.getUnitNumber());
        dto.setStreetNumber(addr.getStreetNumber());
        dto.setAddressLine1(addr.getAddressLine1());
        dto.setAddressLine2(addr.getAddressLine2());
        dto.setCity(addr.getCity());
        dto.setRegion(addr.getRegion());
        dto.setPostalCode(addr.getPostalCode());
        dto.setCountryId(addr.getCountryId());
        return dto;
    }
}

