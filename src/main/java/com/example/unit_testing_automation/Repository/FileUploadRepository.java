package com.example.unit_testing_automation.Repository;

import com.example.unit_testing_automation.Model.TestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepository extends JpaRepository<TestFile,Integer> {
}
