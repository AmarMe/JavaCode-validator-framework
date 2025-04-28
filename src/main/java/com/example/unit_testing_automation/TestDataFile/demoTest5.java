package com.example.unit_testing_automation.TestDataFile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class demoTest5 {

    public demoTest5(){}

    public ResponseEntity<String> testingSimulation(){

        return new ResponseEntity<>("method testingSimulation tested successfully", HttpStatus.OK);
    }

    public ResponseEntity<String> addition(int a, int b){

        return new ResponseEntity<>("Answer is: "+(a+b),HttpStatus.OK);
    }

    public ResponseEntity<String> multiplication(int a, int b){

        return new ResponseEntity<>("Answer is: "+(a*b),HttpStatus.OK);
    }
}
