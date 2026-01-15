package com.bhaskar.theatre.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class AmountNotMatchException extends CustomException{

        private Double amountToBePaid; // This field "holds" the value

        public AmountNotMatchException(String message, HttpStatus httpStatus, Double amount) {
            super(message, httpStatus);
            this.amountToBePaid = amount; // Assign the value here
        }

}
