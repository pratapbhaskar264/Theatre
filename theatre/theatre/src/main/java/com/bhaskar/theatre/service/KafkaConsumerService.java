package com.bhaskar.theatre.service;

import com.bhaskar.theatre.dto.BookingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "theatre-activity", groupId = "theatre-monitoring-group")
    public void consumeBookingActivity(BookingEvent event) {
        System.out.println("\n--- KAFKA MESSAGE RECEIVED ---");
        System.out.println("Booking Confirmed for User: " + event.getUsername());
        System.out.println("Movie: " + event.getMovieName());
        System.out.println("Seats: " + event.getSeatIds());
        System.out.println("Total Amount: ₹" + event.getAmount());
        System.out.println("-------------------------------\n");
    }
}