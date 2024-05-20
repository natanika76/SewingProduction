package ru.vilas.sewing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vilas.sewing.dto.*;
import ru.vilas.sewing.model.*;
import ru.vilas.sewing.repository.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OperationsListSpecialServiceImpl implements OperationsListSpecialService{
    private final OperationDataRepository operationDataRepository;
    private final CategoryService categoryService;
    private final CustomUserDetailsService customUserDetailsService;
    private final WarehouseRepository warehouseRepository;
    private final SizeByDateRepository sizeByDateRepository;
    private final CuttingRepository cuttingRepository;
    private final PackagingRepository packagingRepository;
    @Autowired
    public OperationsListSpecialServiceImpl(OperationDataRepository operationDataRepository, CategoryService categoryService, CustomUserDetailsService customUserDetailsService, WarehouseRepository warehouseRepository, SizeByDateRepository sizeByDateRepository, CuttingRepository cuttingRepository, PackagingRepository packagingRepository) {
        this.operationDataRepository = operationDataRepository;
        this.categoryService = categoryService;
        this.customUserDetailsService = customUserDetailsService;
        this.warehouseRepository = warehouseRepository;
        this.sizeByDateRepository = sizeByDateRepository;
        this.cuttingRepository = cuttingRepository;
        this.packagingRepository = packagingRepository;
    }
    @Override
    public List<WorkedDto> getWorkedDtosList(LocalDate startDate, LocalDate endDate) {

        List<User> users = customUserDetailsService.getAllUsers();
        List<WorkedDto> workedDtos = new ArrayList<>();
        List<Category> categories = categoryService.getAllCategories()
                .stream()
                .filter(Category::isActive) // Фильтрация по активным категориям
                .toList();
        for (User user: users) {
            if (user.getRoles().stream().anyMatch(role -> !role.getName().equals("ROLE_USER"))) {
                continue;
            }

            for (Category category : categories) {
                WorkedDto workedDto = new WorkedDto();

                workedDto.setSeamstressId(user.getId());
                workedDto.setSeamstressName(user.getName());
                workedDto.setCategory(category);
                workedDto.setWorkedByDateList(getWorkedByDateList(startDate, endDate, user.getId(), category));
                workedDto.setTotalWorked(workedSum(workedDto.getWorkedByDateList()));

                workedDtos.add(workedDto);
            }
        }
        return workedDtos;
    }
@Override
    public List<WorkedByDate> getWorkedByDateList(LocalDate startDate, LocalDate endDate, Long seamstressId,
                                              Category category) {
        List<OperationData> operationDataList = operationDataRepository.findBetweenDatesAndBySeamstressAndCategory(startDate, endDate, seamstressId, category);

        List<WorkedByDate> workedByDateList = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            WorkedByDate workedByDate = new WorkedByDate();

            workedByDate.setDate(currentDate);

            LocalDate finalCurrentDate = currentDate;

            workedByDate.setQuantitativeWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.QUANTITATIVE.equals(operationData.getTaskType()))
                            .mapToInt(OperationData::getCompletedOperations)
                            .sum()
            );

            workedByDate.setHourlyWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.HOURLY.equals(operationData.getTaskType()))
                            .map(OperationData::getHoursWorked)
                            .reduce(Duration.ZERO, Duration::plus)
            );

            workedByDate.setHourlyWorkedToString(String.format("%02d:%02d",
                    workedByDate.getHourlyWorked().toHours(),
                    workedByDate.getHourlyWorked().toMinutesPart()
            ));

            workedByDate.setPackagingWorked(
                    operationDataList.stream()
                            .filter(operationData -> operationData.getDate().equals(finalCurrentDate))
                            .filter(operationData -> TaskTypes.PACKAGING.equals(operationData.getTaskType()))
                            .mapToInt(OperationData::getCompletedOperations)
                            .sum()
            );

            workedByDateList.add(workedByDate);
            currentDate = currentDate.plusDays(1);
        }

        return workedByDateList;
    }
    public static WorkedByDate workedSum(List<WorkedByDate> workedByDateList) {
        WorkedByDate sumWorkedByDate = new WorkedByDate();
        sumWorkedByDate.setDate(null);
        sumWorkedByDate.setQuantitativeWorked(
                workedByDateList.stream()
                        .mapToInt(WorkedByDate::getQuantitativeWorked)
                        .sum()
        );
        sumWorkedByDate.setHourlyWorked(
                workedByDateList.stream()
                        .map(WorkedByDate::getHourlyWorked)
                        .reduce(Duration.ZERO, Duration::plus)
        );

        sumWorkedByDate.setHourlyWorkedToString(String.format("%02d:%02d",
                sumWorkedByDate.getHourlyWorked().toHours(),
                sumWorkedByDate.getHourlyWorked().toMinutesPart()
        ));


        sumWorkedByDate.setPackagingWorked(
                workedByDateList.stream()
                        .mapToInt(WorkedByDate::getPackagingWorked)
                        .sum()
        );
        return sumWorkedByDate;
    }

    @Override
    public WarehouseDto showCuttingWarehouse(Long warehouseId) {
        Optional<Warehouse> warehouses = warehouseRepository.findById(warehouseId);
        Warehouse warehouseAllDate = warehouses.get();
        WarehouseDto warehouseDto = new WarehouseDto();
        warehouseDto.setId(warehouseAllDate.getId());
        warehouseDto.setNormPerDay(warehouseAllDate.getNormPerDay());
        warehouseDto.setInTotal(warehouseAllDate.getInTotal());
        List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouseId);
        Integer sum = 0;
        for(Cutting cutting:cuttings){
            sum += cutting.getQuantity();
        }
        warehouseDto.setDone(sum);


        return warehouseDto;
    }
@Override
public List<SizeByDateDto> showTaskCutting(Long id){
        List<SizeByDateDto> sizeByDateDtos = new ArrayList<>();
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(id);

        for (SizeByDate sizeByDate: sizeByDates){
            SizeByDateDto sizeByDateDto = new SizeByDateDto();
            sizeByDateDto.setSize(sizeByDate.getSize());
            sizeByDateDto.setHeight(sizeByDate.getHeight());
            sizeByDateDto.setQuantity(sizeByDate.getQuantity());
            sizeByDateDto.setSizeByDateId(sizeByDate.getId());

            List<Cutting> cuttings = cuttingRepository.findBySizeByDateId(sizeByDate.getId());
            int sum = 0;
                for (Cutting cutting: cuttings){
                    sum +=cutting.getQuantity();
                }
             sizeByDateDto.setCutOut(sizeByDateDto.getQuantity() - sum);
            sizeByDateDtos.add(sizeByDateDto);
        }
        return sizeByDateDtos;
}
    @Override
    public List<SizeByDateDto> showTaskPackaging(Long warehouseId){
        List<SizeByDateDto> sizeByDateDtos = new ArrayList<>();
        List<SizeByDate> sizeByDates = sizeByDateRepository.findByWarehouseIdAndNonZeroQuantity(warehouseId);

        for (SizeByDate sizeByDate: sizeByDates){
            SizeByDateDto sizeByDateDto = new SizeByDateDto();
            sizeByDateDto.setSize(sizeByDate.getSize());
            sizeByDateDto.setHeight(sizeByDate.getHeight());
            sizeByDateDto.setQuantity(sizeByDate.getQuantity());
            sizeByDateDto.setSizeByDateId(sizeByDate.getId());
            Integer sum = 0;
            List<Cutting> cuttings = cuttingRepository.findBySizeByDateId(sizeByDate.getId());
            for (Cutting quantity: cuttings){
                sum += quantity.getQuantity();
            }
            Integer packagingCum = 0;
            List<Packaging> packagings = packagingRepository.findBySizeByDateId(sizeByDate.getId());
            for (Packaging packaging: packagings){
                packagingCum += packaging.getQuantity();
            }
            sum -= packagingCum;
            sizeByDateDto.setCutOut(sizeByDateDto.getQuantity() - sum);
            sizeByDateDtos.add(sizeByDateDto);
        }

        return sizeByDateDtos;
    }


@Override
public Integer totalCutOut (Long warehouseId){
    List<Cutting> cuttings = cuttingRepository.findAllByWarehouseId(warehouseId);
    int totalCutOut = 0;
    for (Cutting cutting: cuttings){
        totalCutOut += cutting.getQuantity();
    }
         return  totalCutOut;
}
@Override
public Integer totalPackaging(Long warehouseId){
        List<Packaging> packagings = packagingRepository.findByWarehouseId(warehouseId);
        int totalPackaging = 0;
        for (Packaging packaging: packagings){
            totalPackaging += packaging.getQuantity();
        }
        int balance = totalCutOut(warehouseId)- totalPackaging;
        return balance;
}

}
