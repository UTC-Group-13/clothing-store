package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductItemDTO;
import com.utc.ec.entity.ProductItem;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.ProductItemMapper;
import com.utc.ec.repository.ProductConfigurationRepository;
import com.utc.ec.repository.ProductItemRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.service.ProductItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductItemServiceImpl implements ProductItemService {

    private final ProductItemRepository repository;
    private final ProductRepository productRepository;
    private final ProductConfigurationRepository configurationRepository;
    private final ProductItemMapper mapper;

    @Override
    @Transactional
    public ProductItemDTO create(ProductItemDTO dto) {
        // Validate product tồn tại
        if (!productRepository.existsById(dto.getProductId())) {
            throw new ResourceNotFoundException("productItem.productNotFound", dto.getProductId());
        }
        // Validate SKU không trùng
        if (repository.existsBySku(dto.getSku())) {
            throw new BusinessException("productItem.skuExists", dto.getSku());
        }

        ProductItem entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductItemDTO update(Integer id, ProductItemDTO dto) {
        ProductItem entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productItem.notFound", id));

        // Validate product tồn tại nếu thay đổi
        if (dto.getProductId() != null && !dto.getProductId().equals(entity.getProductId())) {
            if (!productRepository.existsById(dto.getProductId())) {
                throw new ResourceNotFoundException("productItem.productNotFound", dto.getProductId());
            }
        }
        // Validate SKU không trùng (bỏ qua chính nó)
        if (dto.getSku() != null && !dto.getSku().equals(entity.getSku())
                && repository.existsBySkuAndIdNot(dto.getSku(), id)) {
            throw new BusinessException("productItem.skuExists", dto.getSku());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("productItem.notFound", id);
        }
        // Không xóa nếu có configuration đang dùng
        if (configurationRepository.existsByProductItemId(id)) {
            throw new BusinessException("productItem.hasConfigurations");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductItemDTO getById(Integer id) {
        return mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("productItem.notFound", id)));
    }

    @Override
    public List<ProductItemDTO> getAll() {
        return mapper.toDtoList(repository.findAll());
    }

    @Override
    public List<ProductItemDTO> getByProductId(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("productItem.productNotFound", productId);
        }
        return mapper.toDtoList(repository.findByProductId(productId));
    }

    @Override
    public Page<ProductItemDTO> searchProductItems(String keyword, Integer productId,
                                                   Integer minPrice, Integer maxPrice, Pageable pageable) {
        return repository.searchProductItems(keyword, productId, minPrice, maxPrice, pageable)
                .map(mapper::toDto);
    }
}
