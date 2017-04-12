package com.miw.hotel.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.miw.hotel.model.Booking;

public interface BookingRepository extends MongoRepository<Booking, String> {

    public Booking findById(String id);
    
    
}
