package com.utc.ec.service.impl;

import com.utc.ec.dto.ProductDTO;
import com.utc.ec.entity.Product;
import com.utc.ec.entity.ProductVariant;
import com.utc.ec.exception.BusinessException;
import com.utc.ec.exception.ResourceNotFoundException;
import com.utc.ec.mapper.ProductMapper;
import com.utc.ec.repository.CategoryRepository;
import com.utc.ec.repository.ProductRepository;
import com.utc.ec.repository.ProductVariantRepository;
import com.utc.ec.repository.spec.ProductSpecification;
import com.utc.ec.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto) {
        if (dto.getCategoryId() != null && !categoryRepository.existsById(dto.getCategoryId())) {
            throw new ResourceNotFoundException("product.categoryNotFound", dto.getCategoryId());
        }
        if (repository.existsBySlug(dto.getSlug())) {
            throw new BusinessException("product.slugExists", dto.getSlug());
        }

        Product entity = mapper.toEntity(dto);
        entity.setId(null);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public ProductDTO update(Integer id, ProductDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", id));

        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategoryId())) {
            if (!categoryRepository.existsById(dto.getCategoryId())) {
                throw new ResourceNotFoundException("product.categoryNotFound", dto.getCategoryId());
            }
        }
        if (dto.getSlug() != null && !dto.getSlug().equals(entity.getSlug())
                && repository.existsBySlugAndIdNot(dto.getSlug(), id)) {
            throw new BusinessException("product.slugExists", dto.getSlug());
        }

        mapper.updateEntityFromDto(dto, entity);
        entity.setId(id);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("product.notFound", id);
        }
        if (variantRepository.existsByProductId(id)) {
            throw new BusinessException("product.hasVariants");
        }
        repository.deleteById(id);
    }

    @Override
    public ProductDTO getById(Integer id) {
        ProductDTO dto = mapper.toDto(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound", id)));
        enrichThumbnail(dto);
        return dto;
    }

    @Override
    public ProductDTO getBySlug(String slug) {
        ProductDTO dto = mapper.toDto(repository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("product.notFound")));
        enrichThumbnail(dto);
        return dto;
    }

    @Override
    public List<ProductDTO> getAll() {
        List<ProductDTO> dtos = mapper.toDtoList(repository.findAll());
        enrichThumbnails(dtos);
        return dtos;
    }

    @Override
    public List<ProductDTO> getByCategoryId(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("product.categoryNotFound", categoryId);
        }
        List<ProductDTO> dtos = mapper.toDtoList(repository.findByCategoryId(categoryId));
        enrichThumbnails(dtos);
        return dtos;
    }

    @Override
    public Page<ProductDTO> getAllPaged(Pageable pageable) {
        Page<ProductDTO> page = repository.findAll(pageable).map(mapper::toDto);
        enrichThumbnails(page.getContent());
        return page;
    }

    @Override
    public Page<ProductDTO> searchProducts(String name, List<Integer> categoryIds, BigDecimal minPrice,
                                           BigDecimal maxPrice, List<Integer> colorIds, Boolean isActive,
                                           Pageable pageable) {
        Page<ProductDTO> page = repository.findAll(
                ProductSpecification.withFilters(name, categoryIds, minPrice, maxPrice, colorIds, isActive),
                pageable
        ).map(mapper::toDto);
        enrichThumbnails(page.getContent());
        return page;
    }

    // ===================== Thumbnail Enrichment =====================

    /**
     * Enrich a single ProductDTO with thumbnailUrl from its default variant.
     */
    private void enrichThumbnail(ProductDTO dto) {
        if (dto == null || dto.getId() == null) return;
        variantRepository.findByProductIdAndIsDefaultTrue(dto.getId())
                .or(() -> variantRepository.findFirstByProductIdOrderByIdAsc(dto.getId()))
                .ifPresent(v -> dto.setThumbnailUrl(v.getColorImageUrl()));
    }

    /**
     * Batch enrich a list of ProductDTOs with thumbnailUrl (avoids N+1 queries).
     */
    private void enrichThumbnails(List<ProductDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<Integer> productIds = dtos.stream()
                .map(ProductDTO::getId)
                .collect(Collectors.toList());

        // Batch fetch default variants
        Map<Integer, String> thumbnailMap = variantRepository
                .findByProductIdInAndIsDefaultTrue(productIds)
                .stream()
                .collect(Collectors.toMap(
                        ProductVariant::getProductId,
                        ProductVariant::getColorImageUrl,
                        (existing, replacement) -> existing // keep first if duplicate
                ));

        for (ProductDTO dto : dtos) {
            String url = thumbnailMap.get(dto.getId());
            if (url != null) {
                dto.setThumbnailUrl(url);
            } else {
                // Fallback: fetch first variant for products without a default
                variantRepository.findFirstByProductIdOrderByIdAsc(dto.getId())
                        .ifPresent(v -> dto.setThumbnailUrl(v.getColorImageUrl()));
            }
        }
    }
}
