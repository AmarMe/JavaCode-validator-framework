package com.example.unit_testing_automation.TestDataFile;

public class BankAccount {

    public String checkMinBalance(double currentBalance, String customerName){
        double minBalance = 5000.00;
        if(currentBalance >= minBalance){
            return "You have sufficient balance amount";
        }else{
            return "Your account balance amount is lesser than minimum balance";
        }
    }
}
