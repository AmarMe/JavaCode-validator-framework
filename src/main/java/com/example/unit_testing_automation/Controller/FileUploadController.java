package com.example.unit_testing_automation.Controller;


import com.example.unit_testing_automation.Model.TestFile;
import com.example.unit_testing_automation.Service.FileUploadService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/unit-test-api/v1")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload-file")
    public ResponseEntity<String> uploadModule(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        return fileUploadService.uploadModule(file);
    }

    @GetMapping("/test-reports")
    public ResponseEntity<List<TestFile>> allTestReports(){
        List<TestFile> testFileList = fileUploadService.allTestReports();
        return new ResponseEntity<>(testFileList, HttpStatus.OK);
    }

    @GetMapping("/export-students")
    public void exportStudentsToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=unit-test-task1.xlsx");

        fileUploadService.exportToExcel(response);
    }

    @PostMapping("/upload-and-runTest")
    public ResponseEntity<String> uploadAndRunTest(@RequestBody MultipartFile javaFile, @RequestBody MultipartFile testcaseExcelFile){
        if(javaFile.isEmpty() || javaFile==null){
            return new ResponseEntity<>("Java file is empty: Upload a valid java project file",HttpStatus.BAD_REQUEST);
        }
        if(testcaseExcelFile.isEmpty() || testcaseExcelFile==null){
            return new ResponseEntity<>("testcase file is empty: Upload a valid testcase excel file",HttpStatus.BAD_REQUEST);
        }

        return fileUploadService.uploadAndRunTest(javaFile,testcaseExcelFile);
    }

}
