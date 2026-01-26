package com.bhaskar.theatre.service;


import com.bhaskar.theatre.entity.Reservation;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "theatre-activity" ,groupId = "theatre-monitoring-group")
    public void consumeBookingActivity(Reservation reservation){
        System.out.println("LOGGING: Received new activity from Kafka!");
//        System.out.println("User: " + reservation.getUser().getUsername() + " booked seats for Show ID: " + reservation.getShow().getId());
        // In the future, you could save this to a 'History' table or an Analytics DB
    }


}
