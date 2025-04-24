package com.example.unit_testing_automation.TestDataFile;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class BankAccount {

    public ResponseEntity<String> checkMinBalance(double currentBalance){
        ResponseEntity<String> response;
        try {
            if(currentBalance<0)
                throw new IllegalArgumentException("Negative balance exception: Balance amount should be a positive number");
            double minBalance = 5000.00;
            if(currentBalance >= minBalance){
                response = new ResponseEntity<>("You have sufficient balance amount", HttpStatus.OK);
            }else{
                response = new ResponseEntity<>("Your account balance amount is lesser than minimum balance",HttpStatus.OK);
            }

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            response = new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        return response;
    }
}
