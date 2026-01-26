package com.bhaskar.theatre.service;


import com.bhaskar.theatre.entity.Reservation;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "theatre-activity" ,groupId = "theatre-monitoring-group")
    public void consumeBookingActivity(Reservation reservation){

    }


}
