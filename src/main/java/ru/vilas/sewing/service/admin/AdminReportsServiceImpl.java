package ru.vilas.sewing.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.CuttingDto;
import ru.vilas.sewing.dto.ReportsDto;
import ru.vilas.sewing.model.Customer;
import ru.vilas.sewing.model.Cutting;
import ru.vilas.sewing.model.SizeByDate;
import ru.vilas.sewing.model.Warehouse;
import ru.vilas.sewing.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminReportsServiceImpl implements AdminReportsService {
    private final CuttingRepository cuttingRepository;
    private final SizeByDateRepository sizeByDateRepository;
    private final WarehouseRepository warehouseRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AdminReportsServiceImpl(CuttingRepository cuttingRepository, SizeByDateRepository sizeByDateRepository, WarehouseRepository warehouseRepository, CustomerRepository customerRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.cuttingRepository = cuttingRepository;
        this.sizeByDateRepository = sizeByDateRepository;
        this.warehouseRepository = warehouseRepository;
        this.customerRepository = customerRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
   public List<ReportsDto> getCuttingDtos(List<Long> warehouseIds){

       List<Cutting> cuttings = new ArrayList<>();
        for (Long warehouseId : warehouseIds) {
           List<Cutting> cutting = cuttingRepository.findAllByWarehouseId(warehouseId);
           for (Cutting cutt : cutting) {
                cuttings.add(cutt);
            }
       }
       List<ReportsDto>reportsDtos = new ArrayList<>();
        for (Cutting cutting : cuttings) {
            ReportsDto reportsDto = new ReportsDto();
            reportsDto.setCuttingId(cutting.getId());
            reportsDto.setDateWork(cutting.getDateWork());
            reportsDto.setQuantity(cutting.getQuantity());
            reportsDto.setSeamstressId(cutting.getSeamstressId());

            Optional<SizeByDate> optionalSizeByDate = sizeByDateRepository.findById(cutting.getSizeByDateId());
            SizeByDate sizeByDate = optionalSizeByDate.get();
            reportsDto.setSize(sizeByDate.getSize());
            reportsDto.setHeight(sizeByDate.getHeight());

            Long customer = warehouseRepository.findCustomerIdById(cutting.getWarehouseId());
            reportsDto.setCustomerName(customerRepository.findNameById(customer));

            Long category = warehouseRepository.findCategoryIdById(cutting.getWarehouseId());
            reportsDto.setCategoryName(categoryRepository.findNameById(category));

            reportsDto.setNameMaterial(warehouseRepository.findNameMaterialById(cutting.getWarehouseId()));

            reportsDto.setSeamstressName(userRepository.findNameById(cutting.getSeamstressId()));

            reportsDtos.add(reportsDto);
        }
        return reportsDtos;
    }

    public List<Long> getWarehouseId(Long customerId, Long categoryId) {

        List<Warehouse> warehouses = warehouseRepository.searchByActiveWarehouseArchiveAndCustomerIdAndCategoryId(customerId, categoryId);
        // Используйте поток и метод distinct() для удаления дубликатов
        // Затем используйте метод map() для преобразования объектов Warehouse в их идентификаторы
        List<Long> warehouseId = warehouses.stream()
                .map(Warehouse::getId) // Получаем идентификатор каждого объекта Warehouse
                .distinct()            // Удаляем дубликаты
                .collect(Collectors.toList()); // Собираем результат в список

        return warehouseId;
    }
    @Override
    public ReportsDto editCutting(Long cuttingId){
    ReportsDto reportsDto = new ReportsDto();
    Optional<Cutting> optionalCutting = cuttingRepository.findById(cuttingId);
    Cutting cutting = optionalCutting.get();
    reportsDto.setCuttingId(cutting.getId());
    reportsDto.setWarehouseId(cutting.getWarehouseId());
    reportsDto.setDateWork(cutting.getDateWork());
    reportsDto.setQuantity(cutting.getQuantity());
    Optional<SizeByDate> optionalSizeByDate = sizeByDateRepository.findById(cutting.getSizeByDateId());
    SizeByDate sizeByDate = optionalSizeByDate.get();
    reportsDto.setSize(sizeByDate.getSize());
    reportsDto.setHeight(sizeByDate.getHeight());
    reportsDto.setSeamstressName(userRepository.findNameById(cutting.getSeamstressId()));
    Long customer = warehouseRepository.findCustomerIdById(cutting.getWarehouseId());
    reportsDto.setCustomerName(customerRepository.findNameById(customer));
    Long category = warehouseRepository.findCategoryIdById(cutting.getWarehouseId());
    reportsDto.setCategoryName(categoryRepository.findNameById(category));
    reportsDto.setNameMaterial(warehouseRepository.findNameMaterialById(cutting.getWarehouseId()));

    List<Cutting> cuttings = cuttingRepository.findBySizeByDateId(sizeByDate.getId());
    int sum = 0;
    for (Cutting cutting1 : cuttings) {
        sum += cutting1.getQuantity();
    }
    // общее выполнено минус выполненоое в этом отчёте. нужно для ограничения ввода числа превышаещего остаток
        sum = sum - cutting.getQuantity();
    int quantity = sizeByDate.getQuantity();
    // остаток. Это максимальной число для ввода в поле выполнено
        int done = quantity - sum;
    reportsDto.setDone(done);

        return reportsDto;
    }
}
