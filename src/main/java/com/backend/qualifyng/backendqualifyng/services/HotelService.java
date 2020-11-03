package com.backend.qualifyng.backendqualifyng.services;

import com.backend.qualifyng.backendqualifyng.dtos.HotelDTO;
import com.backend.qualifyng.backendqualifyng.helpers.DailyCalculationHelper;
import com.backend.qualifyng.backendqualifyng.integration.HotelIntegration;
import com.backend.qualifyng.backendqualifyng.mappers.HotelMapper;
import com.backend.qualifyng.backendqualifyng.responses.Hotel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@CacheConfig(cacheNames = "hotels")
public class HotelService {

    private final HotelIntegration hotelIntegration;

    private final HotelMapper hotelMapper;

    private final DailyCalculationHelper dailyCalculationHelper;

    public HotelService(HotelIntegration hotelIntegration, HotelMapper hotelMapper,
            DailyCalculationHelper dailyCalculationHelper) {
        this.hotelIntegration = hotelIntegration;
        this.hotelMapper = hotelMapper;
        this.dailyCalculationHelper = dailyCalculationHelper;
    }

    public List<HotelDTO> getHotel(String hotelID, LocalDate checkInDate, LocalDate checkOutDate, String numberOfAdults,
            String numberOfChildren) {

        ResponseEntity<List<Hotel>> response = hotelIntegration.getHotel(hotelID);

        List<Hotel> hotels = response.getBody();

        dailyCalculationHelper.calculateDays(hotels, checkInDate, checkOutDate);

        List<HotelDTO> hotelDTOs = convertList(hotels);

        dailyCalculationHelper.calculateTotalPrice(hotelDTOs);

        return hotelDTOs;

    }

    private List<HotelDTO> convertList(List<Hotel> hotels) {

        return hotelMapper.toDtoList(hotels);

    }

    public List<HotelDTO> getHotels(List<String> cityList, LocalDate checkInDate, LocalDate checkOutDate,
            String numberOfAdults, String numberOfChildren) throws InterruptedException, ExecutionException {

        List<Hotel> hotels = getHotelsCaacheable(cityList);

        dailyCalculationHelper.calculateDays(hotels, checkInDate, checkOutDate);

        List<HotelDTO> hotelDTOs = convertList(hotels);

        dailyCalculationHelper.calculateTotalPrice(hotelDTOs);

        return hotelDTOs;

    }

    @Cacheable
    private List<Hotel> getHotelsCaacheable(List<String> cityList) throws InterruptedException, ExecutionException {
       return hotelIntegration.getHotels(cityList);
    }

    private List<HotelDTO> calcAndConvertObject(LocalDate checkInDate, LocalDate checkOutDate, List<Hotel> hotels) {
        dailyCalculationHelper.calculateDays(hotels, checkInDate, checkOutDate);

        List<HotelDTO> hotelDTOs = convertList(hotels);

        dailyCalculationHelper.calculateTotalPrice(hotelDTOs);
        return hotelDTOs;
    }

}
