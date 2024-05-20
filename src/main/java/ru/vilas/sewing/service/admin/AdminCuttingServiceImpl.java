package ru.vilas.sewing.service.admin;

import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.*;
import ru.vilas.sewing.service.OperationsListSpecialService;

import java.time.LocalDate;
import java.util.*;

@Service
public class AdminCuttingServiceImpl implements AdminCuttingService {
    private final CuttingRepository cuttingRepository;
    private final WarehouseRepository warehouseRepository;

    private final SizeByDateRepository sizeByDateRepository;
    private final UserRepository userRepository;
    private final SeamstressService seamstressService;
    private final PackagingRepository packagingRepository;


    public AdminCuttingServiceImpl(CuttingRepository cuttingRepository, WarehouseRepository warehouseRepository, SizeByDateRepository sizeByDateRepository, UserRepository userRepository, SeamstressService seamstressService, PackagingRepository packagingRepository) {
        this.cuttingRepository = cuttingRepository;

        this.warehouseRepository = warehouseRepository;
        this.sizeByDateRepository = sizeByDateRepository;
        this.userRepository = userRepository;
        this.seamstressService = seamstressService;
        this.packagingRepository = packagingRepository;
    }

    @Override
    public boolean saveCuttingFromDto(CuttingDto cuttingDto) {
        LocalDate date = cuttingDto.getDateWork();
        Long seamstressId = cuttingDto.getSeamstressId();
        Long warehouseId = cuttingDto.getWarehouseId();
        List<Long> sizByDates = cuttingDto.getSizeByDatesId();
        List<Integer> quantityFull = cuttingDto.getQuantityFull();

        Boolean record = cuttingRepository.existsByDateWorkAndSeamstressIdAndWarehouseId(date, seamstressId, warehouseId);
        if(record){
            try {
                for (int i =0; i < sizByDates.size(); i++) {
                    Optional<Cutting> optionalCutting= cuttingRepository.findByDateWorkAndSizeByDateIdAndSeamstressIdAndWarehouseId(date, sizByDates.get(i), seamstressId, warehouseId);
                    Cutting cutting = optionalCutting.get();
                    Long cuttingId = cutting.getId();
                    Integer quantity = quantityFull.get(i);
                    quantity += cutting.getQuantity();
                    cutting.setQuantity(quantity);
                    cuttingRepository.updateQuantityById(quantity, cuttingId); // Сохраняем объект Cutting в базу данных
                }
                return true; // успешное сохранения

            } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
            }
        } else {
            try {
               for (int i =0; i < sizByDates.size(); i++) {
                    Cutting cutting = new Cutting();
                    cutting.setWarehouseId(warehouseId);
                    cutting.setDateWork(date);
                    cutting.setSeamstressId(seamstressId);
                    cutting.setSizeByDateId( sizByDates.get(i));
                    cutting.setQuantity(quantityFull.get(i));
                    cuttingRepository.save(cutting); // Сохраняем объект Cutting в базу данных
                }
                return true; // успешное сохранения

            } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
            }
        }
    }
    @Override
    public boolean savePackagingFromDto (CuttingDto cuttingDto){
        LocalDate date = cuttingDto.getDateWork();
        Long seamstressId = cuttingDto.getSeamstressId();
        Long warehouseId = cuttingDto.getWarehouseId();
        List<Long> sizByDates = cuttingDto.getSizeByDatesId();
        List<Integer> quantityFull = cuttingDto.getQuantityFull();

        Boolean record = packagingRepository.existsByDateWorkAndSeamstressIdAndWarehouseId(date, seamstressId, warehouseId);
        if(record) {
            try {
                for (int i =0; i < sizByDates.size(); i++) {
                    Optional<Packaging> optionalPackaging= packagingRepository.findByDateWorkAndSizeByDateIdAndSeamstressIdAndWarehouseId(date, sizByDates.get(i), seamstressId, warehouseId);
                    Packaging packaging = optionalPackaging.get();
                    Long packagingId = packaging.getId();
                    Integer quantity = quantityFull.get(i);
                    quantity += packaging.getQuantity();
                    packaging.setQuantity(quantity);
                    packagingRepository.updateQuantityById(quantity, packagingId); // Сохраняем объект packaging в базу данных
                }
                return true; // успешное сохранения
                } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
                }
        }   else {
            try {
                for (int i =0; i < sizByDates.size(); i++) {
                    Packaging packaging = new Packaging();
                    packaging.setWarehouseId(warehouseId);
                    packaging.setDateWork(date);
                    packaging.setSeamstressId(seamstressId);
                    packaging.setSizeByDateId( sizByDates.get(i));
                    packaging.setQuantity(quantityFull.get(i));
                    packagingRepository.save(packaging); // Сохраняем объект в базу данных
                }
                return true; // успешное сохранения
            } catch (Exception e) {
                // Обработка ошибок при сохранении
                return false; // неудачное сохранения
            }
        }
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
            List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouse.getId());
            Integer done = 0;
            for (Cutting cutting : cuttings) {
                done += cutting.getQuantity();
            }
            warehouseDto.setDone(done);
            List<Packaging> packagings = packagingRepository.findAllByWarehouseId(warehouse.getId());
            Integer packed = 0;
            for (Packaging packaging : packagings) {
                packed += packaging.getQuantity();
            }
            warehouseDto.setPackaging(packed);
            List<SizeByDate> sizeByDateDtos = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouse.getId());
            Integer sum = 0;
            for (SizeByDate sizeByDate : sizeByDateDtos) {
                sum += sizeByDate.getQuantity();
            }
            warehouseDto.setTarget(sum);
            warehouseDtos.add(warehouseDto);
        }
        return warehouseDtos;
    }

    @Override
    public List<SizeByDateDto> showCuttingProcess(Long warehouseId, LocalDate startDate, LocalDate endDate) {

            // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }

        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseId(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizebyDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            sizebyDateIds.add(id.getId());
        }
        // Создаем множество из списка, это автоматически удалит повторяющиеся элементы
        Set<Long> uniqueIdsSet = new HashSet<>(sizebyDateIds);
       // Очищаем исходный список
        sizebyDateIds.clear();
       // Добавляем уникальные элементы обратно в список
        sizebyDateIds.addAll(uniqueIdsSet);

        List<SizeByDateDto> sizeByDateDtos = new ArrayList<>();

        for (Long id: sizebyDateIds){
            SizeByDateDto sizeByDateDto = new SizeByDateDto();
            Integer done = 0;
            List<Cutting> dones = cuttingRepository.findBySizeByDateId(id);
            for (Cutting d: dones){
                done += d.getQuantity();
            }
            Integer pack = 0;
            List<Packaging> packs = packagingRepository.findBySizeByDateId(id);
            for (Packaging p: packs){
                pack += p.getQuantity();
            }

            SizeByDate sizeByDate = sizeByDateRepository.findById(id).orElseThrow();
            sizeByDateDto.setSize(sizeByDate.getSize());
            sizeByDateDto.setHeight(sizeByDate.getHeight());
            sizeByDateDto.setQuantity(sizeByDate.getQuantity());
            sizeByDateDto.setFullDone(sizeByDate.getQuantity() - done);
            sizeByDateDto.setFullCutOut(sizeByDate.getQuantity() - pack);
            sizeByDateDto.setCutOut(pack);
            sizeByDateDto.setDone(done);
            sizeByDateDtos.add(sizeByDateDto);
        }
        return sizeByDateDtos;
    }

    @Override
    public List<ChartCuttingDto> showChartSeamstress(Long warehouseId, LocalDate startDate, LocalDate endDate, Long seamstressId) {

        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }

        List<ChartCuttingDto> chartCuttingDtos = new ArrayList<>();
        Integer total = 0;
        if (seamstressId != 0) {
            for (LocalDate date : dateRange) {
                Integer sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();
                for (Long id : sizeByDateIds) {
                    Cutting cutting = cuttingRepository.findByDateWorkAndSizeByDateIdAndSeamstressId(date, id, seamstressId);
                    if (cutting == null) {
                        sum = 0;
                        break;
                    }
                    sum += cutting.getQuantity();
                }
                total += sum;
                chartCuttingDto.setQuantity(sum);
                chartCuttingDto.setSeamstressName(userRepository.findNameById(seamstressId));
                chartCuttingDto.setTotal(total);
                chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
        else {
            for (LocalDate date : dateRange) {
                Integer sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();

                for (Long id : sizeByDateIds) {
                    List<Cutting> cuttings = cuttingRepository.findByDateWorkAndSizeByDateId(date, id);
                    for (Cutting cutting : cuttings) {
                        if (cutting == null) {
                            sum = 0;
                            //chartCuttingDto.setQuantity(sum);
                            continue;
                        }
                        sum += cutting.getQuantity();
                    }
                }
                 total += sum;
                 chartCuttingDto.setQuantity(sum);
                 chartCuttingDto.setTotal(total);
                 chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
    }
    @Override
    public List<User> showSeamstress (Long warehouseId){

        List<Long> seamstressId = cuttingRepository.findSeamstressIdsByWarehouseId(warehouseId);

        List<User> seamstress = new ArrayList<>();
    for (Long id: seamstressId){
    User user = seamstressService.getSeamstressById(id);
    seamstress.add(user);
    }
       return seamstress;
    }

    @Override
    public List<ChartCuttingDto> showChartPackagingSeamstress(Long warehouseId, LocalDate startDate, LocalDate endDate, Long seamstressId) {

        // получаем массив дат от... до...
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);
        //Получаем массив id sizeByDate
        List<Long> sizeByDateIds = new ArrayList<>();
        for (SizeByDate id : sizeByDates) {
            Long sizeByDateId = 0L;
            sizeByDateId = id.getId();
            sizeByDateIds.add(sizeByDateId);
        }

        List<ChartCuttingDto> chartCuttingDtos = new ArrayList<>();
        Integer total = 0;
        if (seamstressId != 0) {
            for (LocalDate date : dateRange) {
                Integer sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();
                for (Long id : sizeByDateIds) {
                    Packaging packaging = packagingRepository.findByDateWorkAndSizeByDateIdAndSeamstressId(date, id, seamstressId);
                    if (packaging == null) {
                        sum = 0;
                        break;
                    }
                    sum += packaging.getQuantity();
                }
                total += sum;
                chartCuttingDto.setQuantity(sum);
                chartCuttingDto.setSeamstressName(userRepository.findNameById(seamstressId));
                chartCuttingDto.setTotal(total);
                chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
        else {
            for (LocalDate date : dateRange) {
                Integer sum = 0;
                ChartCuttingDto chartCuttingDto = new ChartCuttingDto();

                for (Long id : sizeByDateIds) {
                    List<Packaging> packagings = packagingRepository.findByDateWorkAndSizeByDateId(date, id);
                    for (Packaging packaging : packagings) {
                        if (packaging == null) {
                            sum = 0;
                            //chartCuttingDto.setQuantity(sum);
                            continue;
                        }
                        sum += packaging.getQuantity();
                    }
                }
                total += sum;
                chartCuttingDto.setQuantity(sum);
                chartCuttingDto.setTotal(total);
                chartCuttingDtos.add(chartCuttingDto);
            }
            return chartCuttingDtos;
        }
    }


}