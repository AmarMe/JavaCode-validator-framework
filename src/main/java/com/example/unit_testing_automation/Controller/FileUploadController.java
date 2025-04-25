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
import java.util.Map;

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

    @GetMapping("/export-data")
    public void exportStudentsToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=unit-test-task1.xlsx");

        fileUploadService.exportToExcel(response);
    }



    @PostMapping("/upload-and-runTest")
    public ResponseEntity<String> uploadAndRunTest(
            @RequestParam("javaFile") MultipartFile javaFile,
            @RequestParam("excelFile") MultipartFile testcaseExcelFile) {

        if (javaFile == null || javaFile.isEmpty()) {
            return new ResponseEntity<>("Java file is empty: Upload a valid java project file", HttpStatus.BAD_REQUEST);
        }
        if (testcaseExcelFile == null || testcaseExcelFile.isEmpty()) {
            return new ResponseEntity<>("Testcase file is empty: Upload a valid testcase Excel file", HttpStatus.BAD_REQUEST);
        }

        return fileUploadService.uploadAndRunTest(javaFile, testcaseExcelFile);
    }


    @PostMapping("/uploadexcel")
    public ResponseEntity<List<Map<String, Object>>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Map<String, Object>> result = fileUploadService.readExcelSimple(file);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
