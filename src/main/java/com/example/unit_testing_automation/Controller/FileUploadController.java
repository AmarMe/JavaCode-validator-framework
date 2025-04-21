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



    @PostMapping("/uploadexcel")
    public ResponseEntity<List<Map<String, Object>>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            // Process the uploaded Excel file using readExcelSimple method
            List<Map<String, Object>> result = fileUploadService.readExcelSimple(file);

            // Return the processed data as a response
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }





//    private void generateJUnitTestFromFile(File file) throws IOException, IOException {
//        JavaParser javaParser = new JavaParser();
//        CompilationUnit cu = new CompilationUnit();
//
//        ParseResult<CompilationUnit> parseResult = javaParser.parse(file);
//        if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
//             cu = parseResult.getResult().get();
//        } else {
//            System.out.println("Parsing failed: " + parseResult.getProblems());
//        }
//        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(file.getName().replace(".java", ""));
//
//        if (classOpt.isEmpty()) return;
//        ClassOrInterfaceDeclaration clazz = classOpt.get();
//
//        String className = clazz.getNameAsString();
//        String testClassName = className + "Test";
//
//        StringBuilder testCode = new StringBuilder();
//        testCode.append("package com.example.unittesttool.generated;\n\n")
//                .append("import org.junit.jupiter.api.Test;\n")
//                .append("import static org.junit.jupiter.api.Assertions.*;\n")
//                .append("\n")
//                .append("public class ").append(testClassName).append(" {\n\n")
//                .append("    ").append(className).append(" obj = new ").append(className).append("();\n\n");
//
//        List<MethodDeclaration> methods = clazz.getMethods();
//        for (MethodDeclaration method : methods) {
//            testCode.append("    @Test\n")
//                    .append("    public void test").append(capitalize(method.getNameAsString())).append("() {\n")
//                    .append("        // Arrange\n")
//                    .append("        // TODO: Add parameters\n")
//                    .append("\n        // Act\n")
//                    .append("        var result = obj.").append(method.getNameAsString()).append("();\n")
//                    .append("\n        // Assert\n")
//                    .append("        // TODO: Replace with expected result\n")
//                    .append("        assertEquals(null, result);\n")
//                    .append("    }\n\n");
//        }
//
//        testCode.append("}\n");
//
//        Files.createDirectories(Paths.get(GENERATED_TESTS));
//        Path testFilePath = Paths.get(GENERATED_TESTS + testClassName + ".java");
//        Files.write(testFilePath, testCode.toString().getBytes());
//    }
//
//    private String capitalize(String str) {
//        if (str == null || str.isEmpty()) return str;
//        return str.substring(0, 1).toUpperCase() + str.substring(1);
//    }

}
