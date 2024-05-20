package ru.vilas.sewing.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.SizeByDateDto;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.dto.WarehouseDto;
import ru.vilas.sewing.repository.SizeByDateRepository;
import ru.vilas.sewing.repository.WarehouseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AdminWarehouseServiceImpl implements AdminWarehouseService {
    private final WarehouseRepository warehouseRepository;

    private final SizeByDateRepository sizeByDateRepository;

    @Autowired
    public AdminWarehouseServiceImpl(WarehouseRepository warehouseRepository, SizeByDateRepository sizeByDateRepository) {
        this.warehouseRepository = warehouseRepository;
        this.sizeByDateRepository = sizeByDateRepository;
    }
    @Override
    public List<WarehouseDto> showAllActiveTasksCut(Long customerId, Long categoryId, String nameMaterial) {
      List<WarehouseDto> warehouseDtos = new ArrayList<>();
      List<Warehouse> warehouses;
      if(nameMaterial != "" && customerId != 0 && categoryId != 0){
           warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerIdAndCategoryIdAndNameMaterial(customerId, categoryId, nameMaterial);
      }
      else if(customerId != 0 && categoryId !=0 && nameMaterial == ""){
            warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerIdAndCategoryId(customerId, categoryId);
      }
      else if(customerId != 0 && categoryId == 0 && nameMaterial == ""){
          warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerId(customerId);
      }
      else if (categoryId != 0 && customerId == 0 && nameMaterial == ""){
          warehouses = warehouseRepository.findAllByCategoryId(categoryId);
      }
      else  if(nameMaterial !=""){
          warehouses = warehouseRepository.findAllByNameMaterial(nameMaterial);
      }
      else {
          warehouses = warehouseRepository.findAllByArchiveIsFalse();
      }
        for (Warehouse warehouse: warehouses){
            WarehouseDto warehouseDto = new WarehouseDto();
            warehouseDto.setId(warehouse.getId());
            warehouseDto.setCustomerName(warehouse.getCustomer().getName());
            warehouseDto.setCategoryName(warehouse.getCategory().getName());
            warehouseDto.setNameMaterial(warehouse.getNameMaterial());
            List<SizeByDate> sizeByDateDtos = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouse.getId());
            Integer sum = 0;
            for (SizeByDate sizeByDate : sizeByDateDtos) {
                sum += sizeByDate.getQuantity();
            }
            warehouseDto.setDone(sum);
            warehouseDtos.add(warehouseDto);
        }
           return warehouseDtos;
    }

    @Override
    public Warehouse convertToEntity(WarehouseDto warehouseDto) {
        Warehouse warehouse = new Warehouse();
        warehouse.setNameMaterial(warehouseDto.getNameMaterial());
        warehouse.setUnitsOfMeasurement(warehouseDto.getUnitsOfMeasurement());
        warehouse.setQuantityOfMaterial(warehouseDto.getQuantityOfMaterial());
        warehouse.setExpenditure(warehouseDto.getExpenditure());
        warehouse.setNumberOfProducts(warehouseDto.getNumberOfProducts());
        warehouse.setTarget(warehouseDto.getTarget());
        warehouse.setBalance(warehouseDto.getBalance());
        warehouse.setNormPerDay(warehouseDto.getNormPerDay());
        warehouse.setInTotal(warehouseDto.getInTotal());
        warehouse.setRemains(warehouseDto.getRemains());
        warehouse.setStartWork(warehouseDto.getStartWork());
        warehouse.setEndWork(warehouseDto.getEndWork());
        warehouse.setCustomer(warehouse.getCustomer());
        warehouse.setCategory(warehouse.getCategory());
        // Преобразуем категорию и заказчика из DTO в сущности, если они присутствуют
        if (warehouseDto.getCustomer() != null) {
            Customer customer = new Customer();
            customer.setId(warehouseDto.getCustomer().getId()); // предполагается, что getId() возвращает идентификатор заказчика
            warehouse.setCustomer(customer);
        }
        if (warehouseDto.getCategory() != null) {
            Category category = new Category();
            category.setId(warehouseDto.getCategory().getId()); // предполагается, что getId() возвращает идентификатор категории
            warehouse.setCategory(category);
        }
        return warehouse;
    }

    @Override
    public void saveSizeByDate(WarehouseDto warehouseDto, Long warehouseId) {

        List<String> height = warehouseDto.getHeight();
        List<String> size = warehouseDto.getSize();
        List<Integer> quantity = warehouseDto.getQuantity();


        for (int i = 0; i < size.size(); i++) {
            SizeByDate sizeByDate = new SizeByDate();
            sizeByDate.setSize(size.get(i));
            sizeByDate.setHeight(height.get(i));
            sizeByDate.setQuantity(quantity.get(i));
            sizeByDate.setWarehouseId(warehouseId);
           // sizeByDate.setCutOut(quantity.get(i));
            sizeByDateRepository.save(sizeByDate);
        }

    }

@Override
public Boolean checkingForExistence(WarehouseDto warehouseDto){
       //проверяем есть ли такая не архивная запись(заказчик, категория, материал)
        boolean isSave;
        Long customerId = warehouseDto.getCustomer().getId();
        Long categoryId = warehouseDto.getCategory().getId();
        String nameMaterial = warehouseDto.getNameMaterial();
        Long warehouseId = warehouseRepository.findIdByCustomerCategoryMaterial(customerId, categoryId,nameMaterial);

        if(warehouseId == null || warehouseId == 0 ){
            isSave = true;  // задачи нет
            return isSave;
        } else {
            isSave = false; // задача есть
            return isSave;
        }
    }
@Override
public Boolean checkingForEdit(WarehouseDto warehouseDto, Long id) {
    boolean isSave;
    Long customerId = warehouseDto.getCategory().getCustomer().getId();
    Long categoryId = warehouseDto.getCategory().getId();
    String nameMaterial = warehouseDto.getNameMaterial();
    Long warehouseId = warehouseRepository.findIdByCustomerCategoryMaterialAndWarehouseIdNot(customerId, categoryId, nameMaterial, id);
    if (warehouseId == null || warehouseId == 0) {
        isSave = true;  // задачи нет
        return isSave;
    } else {
        isSave = false; // задача есть
        return isSave;
    }
}
    @Override
    public Warehouse getWarehouseById(Long id){return warehouseRepository.findById(id).orElse(null);}
    @Override
    public List<SizeByDateDto> getSizeByDateById(Long id){
        List<SizeByDateDto>sizeByDateDtos = new ArrayList<>();
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(id);

        for (SizeByDate sizeByDate: sizeByDates){
            SizeByDateDto sizeByDateDto = new SizeByDateDto();
            sizeByDateDto.setSize(sizeByDate.getSize());
            sizeByDateDto.setHeight(sizeByDate.getHeight());
            sizeByDateDto.setQuantity(sizeByDate.getQuantity());
            sizeByDateDtos.add(sizeByDateDto);
        }
        return sizeByDateDtos;
    }
    @Override
    public void updatedWarehouse(WarehouseDto warehouseDto){
        Warehouse updatedWarehouse = warehouseRepository.findById(warehouseDto.getId()).orElse(null);

        if (updatedWarehouse  != null) {
            updatedWarehouse.setId(warehouseDto.getId());
            updatedWarehouse.setNameMaterial(warehouseDto.getNameMaterial());
            updatedWarehouse.setQuantityOfMaterial(warehouseDto.getQuantityOfMaterial());
            updatedWarehouse.setExpenditure(warehouseDto.getExpenditure());
            updatedWarehouse.setNumberOfProducts(warehouseDto.getNumberOfProducts());
            updatedWarehouse.setTarget(warehouseDto.getTarget());
            updatedWarehouse.setBalance(warehouseDto.getBalance());
            updatedWarehouse.setNormPerDay(warehouseDto.getNormPerDay());
            updatedWarehouse.setInTotal(warehouseDto.getInTotal());
            updatedWarehouse.setRemains(warehouseDto.getRemains());
            updatedWarehouse.setStartWork(warehouseDto.getStartWork());
            updatedWarehouse.setEndWork(warehouseDto.getEndWork());
            updatedWarehouse.setUnitsOfMeasurement(warehouseDto.getUnitsOfMeasurement());
            updatedWarehouse.setCategory(warehouseDto.getCategory());
            warehouseRepository.save(updatedWarehouse);

            Long id =warehouseDto.getId();
            List<String> height = warehouseDto.getHeight();
            List<String> size = warehouseDto.getSize();
            List<Integer> quantity = warehouseDto.getQuantity();

            List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(id);

            if(sizeByDates.size() == size.size()){
            for (int i = 0; i < size.size(); i++){
                sizeByDates.get(i).setSize(size.get(i));
                sizeByDates.get(i).setHeight(height.get(i));
                sizeByDates.get(i).setQuantity(quantity.get(i));
                sizeByDateRepository.save(sizeByDates.get(i));;
            }
            } else {
                for (int i = 0; i < sizeByDates.size(); i++){
                    sizeByDates.get(i).setSize(size.get(i));
                    sizeByDates.get(i).setHeight(height.get(i));
                    sizeByDates.get(i).setQuantity(quantity.get(i));
                    sizeByDateRepository.save(sizeByDates.get(i));;
                }
            for (int i = sizeByDates.size() ; i< size.size(); i++){
                SizeByDate sizeByDate = new SizeByDate();
                sizeByDate.setSize(size.get(i));
                sizeByDate.setHeight(height.get(i));
                sizeByDate.setQuantity(quantity.get(i));
                sizeByDate.setWarehouseId(id);
                sizeByDateRepository.save(sizeByDate);
            }
            }
        }
    }
    @Override
    public List<String> showNameMaterialByCategory(Long categoryId){
        List nameMaterial = warehouseRepository.findNameMaterialByArchiveFalseAndCategoryId(categoryId);
        return nameMaterial;
    }
    @Override
    public List<String> showNameMaterial(){
        List nameMaterial = warehouseRepository.findNameMaterialByArchiveFalse();
        return nameMaterial;
    }
   }

