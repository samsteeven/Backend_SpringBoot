package com.app.easypharma_backend.domain.order.mapper;

import com.app.easypharma_backend.domain.order.dto.OrderDTO;
import com.app.easypharma_backend.domain.order.dto.OrderItemDTO;
import com.app.easypharma_backend.domain.order.entity.Order;
import com.app.easypharma_backend.domain.order.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(target = "patientName", expression = "java(order.getPatient().getFirstName() + \" \" + order.getPatient().getLastName())")
    @Mapping(target = "pharmacyId", source = "pharmacy.id")
    @Mapping(target = "pharmacyName", source = "pharmacy.name")
    @Mapping(target = "created", source = "createdAt")
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOList(List<Order> orders);

    @Mapping(target = "medicationId", source = "medication.id")
    @Mapping(target = "medicationName", source = "medication.name")
    @Mapping(target = "totalPrice", expression = "java(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    OrderItemDTO toItemDTO(OrderItem item);
}
